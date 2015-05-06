package controllers;

import helpers.AdminFilter;
import helpers.CurrentUserFilter;
import helpers.FileUpload;
import helpers.HashHelper;
import helpers.MailHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Category;
import models.Company;
import models.Coupon;
import models.EmailVerification;
import models.Pin;
import models.Post;
import models.ResetPasword;
import models.Statistic;
import models.Subscriber;
import models.SuperUser;
import models.TransactionCP;
import models.User;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.Loginpage;
import views.html.index;
import views.html.signup;
import views.html.admin.users.adminPanel;
import views.html.admin.users.buyForUser;
import views.html.admin.users.userList;
import views.html.coupon.boughtCoupons;
import views.html.coupon.coupontemplate;
import views.html.coupon.makeAGift;
import views.html.user.profile;
import views.html.user.userUpdate;
import api.JSonHelper;

public class UserController extends Controller {

	static String PATH = Play.application().configuration().getString("PATH"); 
	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");
	static final String MAIL_VERIFICATION_SUBJECT = Play.application().configuration().getString("emailVerificationSubject");
	static String name = null;
	static String defaultPicture = Play.application().configuration().getString("defaultProfilePicture");
	static Form<User> userForm = new Form<User>(User.class);
	

	/**
	 * Pulls the input form from the registration form fields and creates a new
	 * user in the Database.
	 * 
	 * @return redirects to the index page with welcome, or renders the page
	 *         repeatedly if any error occurs
	 */
	public static Result register() {

		Form<User> submit = Form.form(User.class).bindFromRequest();
		if (submit.hasErrors() || submit.hasGlobalErrors()) {
			return ok(signup.render(submit, new Form<Company>(Company.class)));
		}

		try {

			String username = userForm.bindFromRequest().get().username;
			String surname = userForm.bindFromRequest().get().surname;
			Date dob = userForm.bindFromRequest().get().dob;
			String gender = userForm.bindFromRequest().get().gender;
			String adress = userForm.bindFromRequest().get().adress;
			String city = userForm.bindFromRequest().get().city;
			String mail = userForm.bindFromRequest().get().email;
			String password = userForm.bindFromRequest().get().password;
			String hashPass = HashHelper.createPassword(password);
			String confPass = userForm.bindFromRequest()
					.field("confirmPassword").value();

			if (!password.equals(confPass)) {
				flash("error", Messages.get("password.dontMatch"));
				return badRequest(signup.render(submit, new Form<Company>(
						Company.class)));
			}

			/*
			 * Creating new user if the username or mail is free for use, and
			 * there are no errors
			 */

			else if (User.verifyRegistration(username, mail) == true) {

				long id = User.createUser(username, surname, dob, gender,
						adress, city, mail, hashPass, false, defaultPicture);
				String verificationEmail = EmailVerification.addNewRecord(id);
				MailHelper.send(MAIL_VERIFICATION_SUBJECT, mail,
						Messages.get("registration.mail.verificationLinkText")
								+ "<br>" + "http://" + PATH + "/verifyEmail/"
								+ verificationEmail);
				flash("success",
						Messages.get("registration.mail.flash.verification"));
				Logger.info("A verification mail has been sent to email address: "
						+ mail);
				return ok(Loginpage.render(" "));

			} else {
				flash("error", Messages.get("registration.emailAlreadyExists"));
				Logger.info("Email allready exists!");
				return badRequest(signup.render(submit, new Form<Company>(
						Company.class)));
			}

		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at registration: " + e.getMessage(), e);
			return badRequest(signup.render(submit, new Form<Company>(
					Company.class)));
		}

	}

	/**
	 * Update user by getting the values from the form in the userUpdate view.
	 * This method is for every user that is editing his/her own profile.
	 * 
	 * @param useName
	 *            received from the userUpdateView() method
	 * @return Result renders the update view with info messages according to
	 *         update success or fail
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result updateUser(long id) {

		DynamicForm updateForm = Form.form().bindFromRequest();
		Form<User> userForm = Form.form(User.class).bindFromRequest();
		if (userForm.hasGlobalErrors() ) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.debug("Error at update user");	
			return redirect("/updateUser ");
		}
		try {
			String username = updateForm.data().get("username");
			String surname = updateForm.data().get("surname");
			String dobString = updateForm.data().get("dob");
			String adress = updateForm.data().get("adress");
			String city = updateForm.data().get("city");
			String email = updateForm.data().get("email");
			Date dob = null;
			User cUser = User.find(id);
			
			if (!dobString.isEmpty()){
				dob = new SimpleDateFormat("yy-mm-dd").parse(dobString);
				cUser.dob = dob;
			}			
			cUser.username = username;
			cUser.surname = surname;
			cUser.adress = adress;
			cUser.city = city;
			cUser.updated = new Date();

			if (!cUser.email.equals(email)) {
				String verificationEmail = EmailVerification
						.addNewRecord(cUser.id);
				MailHelper.send(MAIL_VERIFICATION_SUBJECT, email,
						Messages.get("registration.mail.verificationLinkText ")
								+ "<br>" + "http://" + PATH
								+ "/verifyEmailUpdate/" + verificationEmail);
				cUser.email = email;
				cUser.save();
				flash("success", Messages.get("registration.mail.flash.verification") + " " + email);
				return ok(userUpdate.render(userForm, null, cUser));
			}
			cUser.email = email;
			cUser.save();
			flash("success", Messages.get("profile.updated"));
			Logger.info(cUser.username + " is updated");
			session("name", cUser.username);
			return ok(userUpdate.render(userForm, null, cUser));
		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at updateUser: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Updates the user from the Admin control.
	 * 
	 * @param id
	 *            of the user to update
	 * @return Result render the vies
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result adminUpdateUser(long id) {

		DynamicForm updateForm = Form.form().bindFromRequest();
		if (userForm.hasErrors() || userForm.hasGlobalErrors()) {
			return redirect("/@editUser/:" + id); // provjeriti
		}
		try {
			String username = updateForm.data().get("username");
			String surname = updateForm.data().get("surname");
			String dobString = updateForm.data().get("dob");
			String gender = updateForm.data().get("gender");
			String adress = updateForm.data().get("adress");
			String city = updateForm.data().get("city");
			String email = updateForm.data().get("email");
			String admin = updateForm.data().get("isAdmin");
			Date dob = null;
			User cUser = User.find(id);
			
			if (!dobString.isEmpty()){
				dob = new SimpleDateFormat("yy-mm-dd").parse(dobString);
				cUser.dob = dob;
			}			

			cUser.username = username;
			cUser.surname = surname;
			cUser.dob = dob;
			cUser.gender = gender;
			cUser.adress = adress;
			cUser.city = city;
			cUser.email = email;

			if (!User.isLastAdmin(cUser)) {
				cUser.isAdmin = Boolean.parseBoolean(admin);
			}
			cUser.updated = new Date();
			cUser.save();
			flash("success", cUser.username + Messages.get("updatedSuccessfully"));
			Logger.info(session("name") + " updated user: " + cUser.username);
			return ok(userList.render(SuperUser.allSuperUsers()));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at adminUpdateUser: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Renders the admin panel view
	 * 
	 * @param id
	 *            of the current user
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result controlPanel(long id) {
		User user = User.find(id);
		if (user == null) {
			flash("error", ERROR_MSG_ADMIN);
			return redirect("/");
		}
		if (!user.username.equals(session("name"))) {
			return redirect("/");
		}

		return ok(adminPanel.render(user, null));

	}

	/**
	 * Renders the user list view. Lists all user from the database
	 *
	 * @return Result
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result listUsers() {
		List<SuperUser> all = SuperUser.allSuperUsers();
		if (all == null) {
			flash("error", ERROR_MSG_ADMIN);
			return redirect("/");
		}
		/* content negotiation */
		if (request().accepts("text/html")) {
			return ok(userList.render(SuperUser.allSuperUsers()));
		}
		return ok(JSonHelper.superUserListToJson(SuperUser.allSuperUsers()));
	}

	/**
	 * Delete user by id. Delete is possible only for own deletion, or if it's
	 * done by Admin.
	 * 
	 * @param id
	 *            Long
	 * @return Result renders the same view
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deleteUser(long id) {

		try {
			List<User> adminList = User.findAdmins(true);
			User currentUser = Sesija.getCurrentUser(ctx());

			if (adminList.size() == 1 && id == currentUser.id) {
				flash("error", Messages.get("admin.last.info"));
				return ok(userList.render(SuperUser.allSuperUsers()));
			}
			if (currentUser.id == id || Sesija.adminCheck(ctx())) {
				User u = User.find(id);
				if(Subscriber.isSubscribed(u)){
					Subscriber.unsubscribe(u);
				}
//				List<TransactionCP> transactions = TransactionCP.allFromBuyer(id);
//				if(transactions != null) {
//					for(TransactionCP t : transactions){
//						t.buyer = null;
//						t.save();
//					}
//				}
//				List<Post> posts = Post.allFromCreator(id);
//				if(posts != null){
//					for(Post p : posts){
//						p.creator = null;
//						p.save();
//					}
//				}
				User.delete(u.id);

				if (currentUser.id == id) {
					session().clear();
					return redirect("/signup ");
				}
			}
			return ok(userList.render(SuperUser.allSuperUsers()));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at deleteUser: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Upload user profile photo
	 * 
	 * @param id
	 *            of the user long
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result updatePhoto(long userId) {
		try {
			User user = User.find(userId);
			String imageUrl = FileUpload.imageUpload();
			user.profilePicture = imageUrl;
			user.save();
			flash("success", "user.photo.uploaded");
			return redirect("/profile/@" + user.username);
		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at updatePhoto: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Method creating new password for user or company. As parameter it takes
	 * id of ResetPasword object which contains users email.
	 * 
	 * @param id
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result createNewPassword(String id) {
		try {
			ResetPasword ressetPassword = ResetPasword.find.byId(id);
			String email = ressetPassword.userEmail;
			User user = User.findByEmail(email);
			Company company = Company.findByEmail(email);
			DynamicForm newPasswordForm = Form.form().bindFromRequest();

			String newPassword = newPasswordForm.data().get("newPassword");
			String confirmPassword = newPasswordForm.data().get("confirmPassword");
			
			if (!newPassword.equals(confirmPassword)) {
				flash("error", Messages.get("password.dontMatch") );
				return redirect("/newPassword/" + ressetPassword.id);
			}

			if (user != null) {
				user.password = HashHelper.createPassword(newPassword);
				user.save();
				ressetPassword.delete();
			} else if (company != null) {
				company.password = HashHelper.createPassword(newPassword);
				company.save();
				ressetPassword.delete();
			}
			flash("success", Messages.get("password.changed.login"));
			return redirect("/");
		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at createNewPassword: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Return a list of bought coupons
	 * 
	 * @param id
	 *            of the buyer
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result showBoughtCoupons(long userId) {
		List<TransactionCP> transactions = TransactionCP.allFromBuyer(userId);
		if (transactions == null) {
			transactions = new ArrayList<TransactionCP>();
		}
		return ok(boughtCoupons.render(session("name"), transactions));
	}

	/**
	 * Method genereates a new random pin for a user.
	 * The pin is valid for 5 minutes.
	 * The generated pin can be used by the administrator to
	 * buy a coupon for a user.
	 * @param user id  long
	 * @return renders the profile page with the generated pin
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result newPin(long id) {
		User currentUser = User.find(id);
		Pin.generatePin(currentUser);
		return ok(profile.render(currentUser));
	}

	/**
	 * Renders the administrator 'buy for user' page.
	 * It gets the pin from the form and renders the 
	 * checkout page where the admin can see the user details 
	 * and choose the number of coupon to buy
	 * @return 
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result buyForUserPage() {
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		long id = Long.parseLong(dynamicForm.data().get("coupon_id"));
		Coupon coupon = Coupon.find(id);
		String pinCode = dynamicForm.data().get("pin");
		User user = Pin.getPinUser(pinCode);
		Pin pin = Pin.getPin(pinCode);
		if (user == null || !Pin.isValid(pin.date)) {
			flash("error", Messages.get("pin.code.invalid"));
			return badRequest(coupontemplate.render(coupon));
		}
		return ok(buyForUser.render(coupon, user));
	}

	/**
	 * Method finalizes the transaction. The execution process excludes
	 * paypal in this case. The transaction details are going in the 
	 * TransactionCP table in the database.
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result buyForUserExecute() {
		String paymentId = Play.application().configuration().getString("bitPaymentId");
		String saleId = Play.application().configuration().getString("bitSaleId");
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		long id = Long.parseLong(dynamicForm.data().get("coupon_id"));
		int quantity = Integer.parseInt(dynamicForm.data().get("quantity"));
		Coupon coupon = Coupon.find(id);
		User client = User.find(Long.parseLong(dynamicForm.data().get("user_id")));
		if (client == null) {
			flash("error", ERROR_MSG_CLIENT);
			return badRequest(coupontemplate.render(coupon));
		}
		double totalPrice = coupon.price * quantity;
		TransactionCP.createTransaction(paymentId, saleId, coupon.price,
				quantity, totalPrice, "", client, coupon);
		Coupon c = Coupon.find(id);
		c.seller.notifications++;
		c.maxOrder = c.maxOrder - quantity;
		c.seller.save();
		c.save();
		MailHelper.sendPurchaseInfo(
				client.username,
				client.surname, 
				client.email, 
				coupon.price, 
				quantity, 
				saleId, paymentId, 
				"http://" + PATH + "/coupon/" + coupon.id);
		flash("success", Messages.get("transaction.complete"));
		return ok(index.render(Coupon.all(), Category.all()));
	}
	
	/**
	 * Method returns statistic excel file as download.
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result getStatistic(){
		File stats = Statistic.createStatisticsFile();
		response().setContentType("application/x-download");  
		response().setHeader("Content-disposition","attachment; filename=statistics.xls");
		return ok(stats);
	
	}
	
	
	/**
	 * Renders the page for buying a coupon for another user
	 * as a gift
	 * @param id of the coupon to buy
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result makeAGiftPage(long couponID) {
		Coupon coupon = Coupon.find(couponID);
		if (coupon != null)
			return ok(makeAGift.render(coupon));
		flash("error", ERROR_MSG_CLIENT);
		return redirect("/");
	}
	
	/**
	 * Method binds data from request that is submitted from the
	 * make a gift page. It searches the database for a user by checking
	 * the provided email. If the user is found, method redirects to the buyForUser page.
	 * If no user us found, the makeAGift page is reloaded again. 
	 * @param id of the coupon as a parameter
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result giftCheckoutPage(long couponId) {
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		String email = dynamicForm.data().get("email");
		User user = User.findByEmail(email);
		if (user == null) {
			flash("info", Messages.get("user.with.email") + " " + email + " " + Messages.get("user.noExists"));
			return badRequest(makeAGift.render(coupon));
		}
		return ok(buyForUser.render(coupon, user));
	}
	
	/**
	 * Renders the checkout page for unregistered users.
	 * @param id of the coupon to buy
	 * @return
	 */
	public static Result checkoutPageUnregistered(long id) {
		Coupon coupon = Coupon.find(id);
		return ok(buyForUser.render(coupon, null));
	}
	
	
}
