package controllers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import helpers.AdminFilter;
import helpers.FileUpload;
import helpers.SuperUserFilter;

import java.util.List;



import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.*;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.*;
import views.html.*;
import views.html.user.*;
import views.html.admin.users.*;
import views.html.coupon.*;
import models.*;

public class UserController extends Controller {
	
	static String PATH = Play.application().configuration().getString("PATH");	/* TODO move all messages to conf */
	static String message = "Welcome ";
	static String bitName = "bitCoupon";
	static String name = null;

	static Form<User> userForm = new Form<User>(User.class);

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

			if (username.length() < 4 || username.equals("Username")) {
				flash("error", "Usernam must be at least 4 chatacters");
				return badRequest(signup.render());
			} else if (mail.equals("Email")) {
				flash("error", "Email is required for registration !");
				return badRequest(signup.render());
			} else if (password.length() < 6) {
				flash("error", "Password must be at least 6 characters!");
				return badRequest(signup.render());
			} else if (!password.equals(confPass)) {
				flash("error", "Passwords don't match, try again ");
				return badRequest(signup.render());
			}
			/*
			 * Creating new user if the username or mail is free for use, and
			 * there are no errors
			 */
			else if (User.verifyRegistration(username, mail) == true) {
				long id = User.createUser(username, surname, dob, gender,
						adress, city, mail, hashPass, false);
				String verificationEmail = EmailVerification.addNewRecord(id);
				MailHelper.send(mail,
						"Click on the link below to verify your e-mail adress <br>"
								+ "http://" + PATH + "/verifyEmail/"
								+ verificationEmail);
				flash("success",
						"A verification mail has been sent to your email address!");
				Logger.info("A verification mail has been sent to email address");
				return ok(Loginpage.render(" "));

			} else {
				flash("error", "Username or email allready exists!");
				Logger.info("Username or email allready exists!");
				return badRequest(signup.render());
			}
		} catch (Exception e) {
			flash("error", "Ooops, error has occured. Please try again later.");
			Logger.error("Error at registration: " + e.getMessage(), e);
			return redirect("/");
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
		
//		if (request().accepts("text/html")) {
			
			DynamicForm updateForm = Form.form().bindFromRequest();
			if (updateForm.hasErrors()) {
				return redirect("/updateUser ");
			}
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
				String username = updateForm.data().get("username");
				String surname = updateForm.data().get("surname");
				Date dob = null;
				try {
					dob = sdf.parse(updateForm.data().get("dob"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String gender = updateForm.data().get("gender");
				String adress = updateForm.data().get("adress");
				String city = updateForm.data().get("city");
				String email = updateForm.data().get("email");
				String oldPass = updateForm.data().get("password");
//				String newPass = updateForm.data().get("newPassword");
	
				User cUser = User.find(id);
				cUser.username = username;
				cUser.surname = surname;
				cUser.dob = dob;
				cUser.gender = gender;
				cUser.adress = adress;
				cUser.city = city;
				cUser.updated = new Date();
	
				if (!cUser.email.equals(email)) {
					String verificationEmail = EmailVerification
							.addNewRecord(cUser.id);
					MailHelper.send(email,
							"Click on the link below to verify your e-mail adress <br>"
									+ "http://" + PATH + "/verifyEmailUpdate/"
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
				session("name", cUser.username);
				return ok(userUpdate.render(cUser));
			} catch (Exception e) {
				flash("error", "Ooops, error has occured. Please try again later.");
				Logger.error("Error at updateUser: " + e.getMessage(), e);
				return redirect("/");
			}
//		} return JSonOperator.updateUser();

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

		if (userForm.hasErrors()) {
			return redirect("/@editUser/:" + id); // provjeriti
		}
		try {
			String username = userForm.bindFromRequest().field("username")
					.value();
			String surname = userForm.bindFromRequest().field("surname")
					.value();
			Date dob = userForm.bindFromRequest().get().dob;
			String gender = userForm.bindFromRequest().field("gender").value();
			String adress = userForm.bindFromRequest().field("adress").value();
			String city = userForm.bindFromRequest().field("city").value();
			String email = userForm.bindFromRequest().field("email").value();
			String newPass = userForm.bindFromRequest().field("newPassword")
					.value();
			String admin = userForm.bindFromRequest().field("isAdmin").value();

			User cUser = User.find(id);
			cUser.username = username;
			cUser.surname = surname;
			cUser.dob = dob;
			cUser.gender = gender;
			cUser.adress = adress;
			cUser.city = city;
			cUser.email = email;
			/*
			 * if admin doesn't explicitly change the users password, it stays
			 * intact
			 */
			if (newPass.length() > 5) {
				cUser.password = HashHelper.createPassword(newPass);
			}
			if (!User.isLastAdmin(cUser)) {
				cUser.isAdmin = Boolean.parseBoolean(admin);
			}
			cUser.updated = new Date();
			cUser.save();
			flash("success", "User " + cUser.username + " updated!");
			Logger.info(session("name") + " updated user: " + cUser.username);
			return ok(userList.render(SuperUser.allSuperUsers()));
		} catch (Exception e) {
			flash("error", "Ooops, error has occured. Please try again later.");
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
		User u = User.find(id);
		if (u == null) {
			flash("error", "Ooops, error occured. Please try again.");
			return redirect("/");
		}
		if (!u.username.equals(session("name"))) {
			return redirect("/");
		}

		return ok(adminPanel.render(u, null));

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
			flash("error", "Ooops, error occured. Please try again.");
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

		try{
			List<User> adminList = User.findAdmins(true);
			User currentUser = Sesija.getCurrentUser(ctx());
			
			if (adminList.size() == 1 && id == currentUser.id) {
				flash("error", "You are the last admin!");
				return ok(userList.render(SuperUser.allSuperUsers()));
			}
			if (currentUser.id == id || Sesija.adminCheck(ctx())) {
				User u = User.find(id);			
				Subscriber.unsubscribe(u);			
				User.delete(u.id);			
				
				if (currentUser.id == id) {
					session().clear();
					return redirect("/signup ");
				}
			}
			return ok(userList.render(SuperUser.allSuperUsers()));		
		}catch(Exception e){
			flash("error", "Ooops, error has occured. Please try again later.");
			Logger.error("Error at deleteUser: " +e.getMessage(), e);
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
			User u = User.find(userId);
			String subFolder = "user_profile" + File.separator + "user_"
					+ userId;
			boolean checkIfDirectoryExists = new File(FileUpload.IMAGES_FOLDER
					+ subFolder).isDirectory();
			if (checkIfDirectoryExists) {
				String assetsPath = FileUpload.imageUpload(subFolder);
				Logger.debug(assetsPath);
				u.profilePicture = assetsPath;
				u.save();
				return redirect("/profile/@" + u.username);
			} else {
				new File(FileUpload.IMAGES_FOLDER + subFolder).mkdir();
				String assetsPath = FileUpload.imageUpload(subFolder);
				Logger.debug(assetsPath);
				u.profilePicture = assetsPath;
				u.save();
				return redirect("/profile/@" + u.username);
			}
		} catch (Exception e) {
			flash("error", "Ooops, error has occured. Please try again later.");
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
			ResetPasword rp = ResetPasword.find.byId(id);
			String email = rp.userEmail;
			User u = User.findByEmail(email);
			Company c = Company.findByEmail(email);
			DynamicForm df = Form.form().bindFromRequest();

			String newPassword = df.data().get("newPassword");
			String confirmPassword = df.data().get("confirmPassword");

			// TODO CHECK IF PASSWORDS ARE EQUAL AND OTHER THINGS ABOUT PW
			if (!newPassword.equals(confirmPassword)) {
				flash("error",
						"Failed to change pw, confirm passwords dont match");
				return redirect("/newPassword/" + rp.id);
			}

			if (u != null) {
				u.password = HashHelper.createPassword(newPassword);
				u.save();
				rp.delete();
			} else if (c != null) {
				c.password = HashHelper.createPassword(newPassword);
				c.save();
				rp.delete();
			}
			flash("success", "Password has been changed, please log in.");
			return redirect("/");
		} catch (Exception e) {
			flash("error", "Ooops, error has occured. Please try again later.");
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
		if (request().accepts("text/html")) {
			return ok(boughtCoupons.render(session("name"), transactions));
		}
		return ok(JSonHelper.transactionListToJSon(transactions));
	}
	
	public static Result generatePin(long id) {
		User currentUser = User.find(id);
		Pin pin = Pin.generatePin(currentUser);
		return ok(profile.render(currentUser));
	}
		
}
