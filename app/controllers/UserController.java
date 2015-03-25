package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import helpers.CurrentUserFilter;
import helpers.AdminFilter;
import helpers.FileUpload;
import java.util.List;
import com.avaje.ebeaninternal.server.persist.BindValues.Value;
import helpers.HashHelper;
import helpers.MailHelper;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import views.html.user.*;
import views.html.admin.users.*;
import views.html.coupon.*;
import models.*;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;


public class UserController extends Controller {

	/* TODO move all messages to conf */
	static String message = "Welcome ";
	static String bitName = "bitCoupon";
	static String name = null;

	static Form<User> userForm = new Form<User>(User.class);

	/**
	 * @return Renders the registration view
	 */
	public static Result signup() {
		return ok(signup.render("Username", "Email"));
	}

	/**
	 * Pulls the input form from the registration form fields and creates a new
	 * user in the Database.
	 * 
	 * @return redirects to the index page with welcome, or renders the page
	 *         repeatedly if any error occurs
	 */
	public static Result register() {

		if (userForm.hasErrors()) {
			return redirect("/signup ");
		}

		String username = userForm.bindFromRequest().get().username;
		String mail = userForm.bindFromRequest().get().email;
		String password = userForm.bindFromRequest().get().password;
		String hashPass = HashHelper.createPassword(password);
		String confPass = userForm.bindFromRequest().field("confirmPassword")
				.value();

		if (username.length() < 4 || username.equals("Username")) {
			flash("error", "Usernam must be at least 4 chatacters");
			return badRequest(signup.render(null, mail));
		} else if (mail.equals("Email")) {
			flash("error", "Email is required for registration !");
			return badRequest(signup.render(username, null));
		} else if (password.length() < 6) {
			flash("error", "Password must be at least 6 characters!");
			return badRequest(signup.render(username, mail));
		} else if (!password.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return badRequest(signup.render(username, mail));
		}

		/*
		 * Creating new user if the username or mail is free for use, and there
		 * are no errors
		 */
		else if (User.verifyRegistration(username, mail) == true) {
			/*
			 * session().clear(); session("name", username);
			 */

			long id = User.createUser(username, mail, hashPass, false);
			String verificationEmail = EmailVerification.addNewRecord(id);

			MailHelper.send(mail,
					"Click on the link below to verify your e-mail adress <br>"
							+ "http://localhost:9000/verifyEmail/"
							+ verificationEmail);
			// User cc = User.getUser(mail);
			Logger.info("A verification mail has been sent to email address");
			return ok(Loginpage
					.render("A verification mail has been sent to your email address"));

		} else {
			flash("error", "Username or email allready exists!");
			Logger.info("Username or email allready exists!");
			return badRequest(signup.render(username, mail));
		}

	}

	/**
	 * Method sends the current user to the userUpdate() method
	 * 
	 * @return Renders the user update view for editing profile
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result userUpdateView() {
		User currentUser = User.find(session("name"));
		return ok(userUpdate.render(currentUser));
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
	public static Result updateUser(long id) {
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateUser ");
		}

		String username = updateForm.data().get("username");
		String email = updateForm.data().get("email");
		String oldPass = updateForm.data().get("password");
		String newPass = updateForm.data().get("newPassword");

		User cUser = User.find(id);
		cUser.username = username;
		// cUser.email = email;
		cUser.updated = new Date();

		/* if only one password field is filled out */
		if (oldPass.isEmpty() && !newPass.isEmpty() || newPass.isEmpty()
				&& !oldPass.isEmpty()) {
			flash("error", "If you want to change your password,"
					+ " please fill out both fields");
			return badRequest(userUpdate.render(cUser));
		}
		/* if there was a input in password fields */
		if (!oldPass.isEmpty() && !newPass.isEmpty()) {
			if (HashHelper.checkPass(oldPass, cUser.password) == false) {
				flash("error", "You're old password is incorrect!");
				return badRequest(userUpdate.render(cUser));
			}
			if (newPass.length() < 6) {
				flash("error", "The password must be at least 6 characters");
				return badRequest(userUpdate.render(cUser));
			}
			cUser.password = HashHelper.createPassword(newPass);
		}
		if (!cUser.email.equals(email)) {
			String verificationEmail = EmailVerification.addNewRecord(cUser.id);
			MailHelper.send(email,
					"Click on the link below to verify your e-mail adress <br>"
							+ "http://localhost:9000/verifyEmailUpdate/"
							+ verificationEmail);
			cUser.email = email;
			cUser.save();
			flash("success",
					"A new verification email has been sent to this e-mail: "
							+ email);
			return ok(userUpdate.render(cUser));
		}
		cUser.email = email;
		cUser.save();
		flash("success", "Profile updated!");
		Logger.info(cUser.username + " is updated");
		return ok(userUpdate.render(cUser));

	}

	/**
	 * Receives a user id, initializes the user, and renders the adminEditUser
	 * passing the user to the view
	 * 
	 * @param id
	 *            of the User (long)
	 * @return Result render adminEditUser
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result adminEditUserView(long id) {

		if (Sesija.adminCheck(ctx()) != true) {
			return redirect("/");
		}
		List<User> adminList = User.findAdmins(true);
		User userToUpdate = User.find(id);
		return ok(adminEditUser
				.render(session("name"), userToUpdate, adminList));
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
		List<User> adminList = User.findAdmins(true);

		if (Sesija.adminCheck(ctx()) != true) {
			return redirect("/");
		}

		if (userForm.hasErrors()) {
			return redirect("/@editUser/:" + id); // provjeriti
		}

		String username = userForm.bindFromRequest().field("username").value();
		String email = userForm.bindFromRequest().field("email").value();
		String newPass = userForm.bindFromRequest().field("newPassword")
				.value();
		String admin = userForm.bindFromRequest().field("isAdmin").value();

		User cUser = User.find(id);
		cUser.username = username;
		cUser.email = email;
		/*
		 * if admin doesn't explicitly change the users password, it stays
		 * intact
		 */

		if (newPass.length() > 0) {
			cUser.password = HashHelper.createPassword(newPass);
		}
		if (!User.isLastAdmin(cUser)) {
			cUser.isAdmin = Boolean.parseBoolean(admin);
		}
		cUser.updated = new Date();
		cUser.save();
		flash("success", "User " + cUser.username + " updated!");
		Logger.info(session("name") + " updated user: " + cUser.username);
		return ok(adminEditUser.render(session("name"), cUser, adminList));
	}

	/**
	 * Renders the admin panel page
	 * @param id of the current user
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result controlPanel(long id) {

		User u = User.find(id);
		if (!u.username.equals(session("name"))) {
			return redirect("/");
		}

		return ok(adminPanel.render(u, null));

	}

	/**
	 * Renders the profile page view
	 * 
	 * @param username
	 * @return Result
	 */
	public static Result profilePage(String username) {
		User u = User.find(username);
		if (!u.username.equals(session("name"))) {
			return redirect("/");
		}

		return ok(profile.render(u));
	}

	/**
	 * Renders the user list view. Lists all user from the database
	 *
	 * @return Result
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result listUsers() {

		return ok(userList.render(session("name"), User.all()));
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
	public static Result deleteUser(Long id){
	    List<User> adminList=User.findAdmins(true);
	    User currentUser = Sesija.getCurrentUser(ctx());
	    
	    /* checking if the current admin is the last admin in the DB */
	    if( adminList.size()== 1 && id == currentUser.id ){
	    	flash("error", "You are the last admin!");
	    	return ok( userList.render(session("name"),User.all()) );
	    }
    
		if (currentUser.id == id || Sesija.adminCheck(ctx())){	
			User.delete(id);
			if(currentUser.id == id){
				return redirect("/signup ");
			}		
		}	
		return ok( userList.render(session("name"),User.all()) );
	}

	/**
	 * Compare if the verification period is expired and send verification mail
	 * to user e-mail adress
	 * 
	 * @param id
	 *            - verification mail
	 * @return redirect to the login view
	 */
	public static Result verifyEmail(String id) {
		EmailVerification recordToUpdate = EmailVerification.find(id);
		String message = "";
		if (recordToUpdate.createdOn.compareTo(new Date()) < 0) {
			EmailVerification.updateRecord(recordToUpdate);
			Logger.info("e-mail is now verified");
			message = "You're e-mail is now verified. To login click on the button below";
		} else {
			Logger.info("Verification period is expired");
			message = "Verification period is expired. If you want to receive a new verification mail, click on the button 'Resend'";
		}
		return ok(verifyEmail.render(message));
	}

	@Security.Authenticated(CurrentUserFilter.class)
	public static Result verifyEmailUpdate(String id) {
		User u = User.find(session("name"));
		EmailVerification recordToUpdate = EmailVerification.find(id);
		String message = "";
		if (recordToUpdate.createdOn.compareTo(new Date()) < 0) {
			EmailVerification.updateRecord(recordToUpdate);
			message = "Your profile is updated. To go to the profile page click on the button below";
		} else {
			message = "Verification period is expired. If you want to receive a new verification mail, click on the button 'Resend'";
		}
		return ok(verifyEmailUpdate.render(message, u.username));
	}
	
	public static Result updatePhoto(long userId){
		User u = User.find(userId);
		String subFolder = "user_profile" +File.separator +"user_" +userId;
		boolean checkIfDirectoryExists = new File(FileUpload.IMAGES_FOLDER + subFolder).isDirectory();
		if(checkIfDirectoryExists){
			String assetsPath = FileUpload.imageUpload(subFolder);
			Logger.debug(assetsPath);
			u.profilePicture = assetsPath;
			u.save();
			return redirect("/profile/@" +u.username);
		}else{
			new File(FileUpload.IMAGES_FOLDER + subFolder).mkdir();
			String assetsPath = FileUpload.imageUpload(subFolder);
			Logger.debug(assetsPath);
			u.profilePicture = assetsPath;
			u.save();
			return redirect("/profile/@" +u.username);
		}
	}
	
	/*  paypal  */
	
	//get route for purchase //ovo nam ne treba, koristit cemo buy na coupon template-u
	public static Result showPurchase(){
		return TODO;
	}

	/**
	 * 
	 * @return
	 */
	public static Result purchaseProcessing(){

		try { 
			String accessToken = new OAuthTokenCredential(
					"AXefj_ltBrqquxwtgvio9GBcfFMxFDh7GP8FTfXi489Vt0xCL7OmnKq6IRyXISYVKD98bVutaHBMwN9h",
					"EPlX3tMGxjQYv0Wf2de9c-QeMlc8PT22jqWDAnexpFTbk1WJNlOgvS2ZQXfhrlQ_7DCbPYl1ElEDYDH9")
					.getAccessToken();
			
			Map<String, String> sdkConfig = new HashMap<String, String>();
			sdkConfig.put("mode", "sandbox");
				
			APIContext apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);
			
			Amount amount = new Amount();
			
			DynamicForm buyForm = Form.form().bindFromRequest();
			
			Coupon coupon = Coupon.find(Long.parseLong((buyForm.data().get("coupon_id"))));
			int quantity = Integer.parseInt(buyForm.data().get("quantity"));
			Double couponPrice = coupon.price;
			Double totalPrice = couponPrice * quantity;
			String price = String.format("%1.2f",totalPrice);
			
			amount.setTotal(price);
			amount.setCurrency("USD");
			
			Transaction transaction = new Transaction();
			transaction.setDescription("So we have a really cool description here");
			transaction.setAmount(amount);
			
			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);
			
			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");
			
			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);
			RedirectUrls redirectUrls = new RedirectUrls();
			redirectUrls.
						setCancelUrl("http://localhost:9000/couponfail");
			redirectUrls.
						setReturnUrl("http://localhost:9000/couponsuccess");
			payment.setRedirectUrls(redirectUrls);
			
			Payment createdPayment = payment.create(apiContext);
			
			Iterator<Links> itr = createdPayment.getLinks().iterator();
			while(itr.hasNext()){
				Links link = itr.next();
				if (link.getRel().equals("approval_url"))
					return redirect(link.getHref());
			}
			
			Logger.debug(createdPayment.toJSON());
			
			/* napravit sta bude ako nesto nije proslo kako treba s paypalom */
			return TODO;
			
			
		} catch (PayPalRESTException e){
			Logger.warn(e.getMessage());
		}
		
		flash("error", "Something went wrong, please try again later");
		User currentUser = User.find(name);
		return ok(index.render(currentUser, Coupon.all()));
	}
	
	/**
	 * 
	 * @return
	 */
	public static Result couponSuccess(){
		
		DynamicForm paypalReturn = Form.form().bindFromRequest();
		
		String paymentID;
		String payerID;
		String token;
		
		paymentID = paypalReturn.get("paymentId");
		payerID = paypalReturn.get("PayerID");
		token = paypalReturn.get("token");
	try{
		String accessToken = new OAuthTokenCredential(
				"AXefj_ltBrqquxwtgvio9GBcfFMxFDh7GP8FTfXi489Vt0xCL7OmnKq6IRyXISYVKD98bVutaHBMwN9h",
				"EPlX3tMGxjQYv0Wf2de9c-QeMlc8PT22jqWDAnexpFTbk1WJNlOgvS2ZQXfhrlQ_7DCbPYl1ElEDYDH9")
				.getAccessToken();
		
		Map<String, String> sdkConfig = new HashMap<String, String>();
		sdkConfig.put("mode", "sandbox");		
		APIContext apiContext = new APIContext(accessToken);
		apiContext.setConfigurationMap(sdkConfig);
		
		Payment payment = Payment.get(accessToken, paymentID);
		
		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerID);
		
		Payment newPayment = payment.execute(apiContext, paymentExecution);
		
	} catch(Exception e){
		Logger.debug(e.getMessage());
	}
		
		return ok(couponResult.render("Proslo"));
	}
	
	
	public static Result couponFail(){
		return ok(couponResult.render("Nije proslo"));
	}
	
	
	
	
	
	
	
}
