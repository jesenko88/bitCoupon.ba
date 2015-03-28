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
			flash("success", "A verification mail has been sent to your email address!");
			Logger.info("A verification mail has been sent to email address");
			return ok(Loginpage
					.render(" "));

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

	/*
	 * 
	 * 
	 * 
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
	 * Search method for users. If search is unsuccessful a flash message is
	 * sent
	 * 
	 * @param string
	 * @return renders index with matching coupons //TODO render a different
	 *         view for search result
	 *
	 */
	public static Result searchUsers(String qU) {
		List<User> users = User.getFind().where()
				.ilike("username", "%" + qU + "%").findList();

		if (users.isEmpty()) {
			flash("error", "No such user");
			return badRequest(userList.render(null, User.all()));
		}

		return ok(userList.render(null, users));
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
	public static Result deleteUser(Long id) {
		List<User> adminList = User.findAdmins(true);
		User currentUser = Sesija.getCurrentUser(ctx());

		if (adminList.size() == 1 && id == currentUser.id) {
			flash("error", "You are the last admin!");
			return ok(userList.render(session("name"), User.all()));
		}

		if (currentUser.id == id || Sesija.adminCheck(ctx())) {
			User.delete(id);
			if (currentUser.id == id) {
				return redirect("/signup ");
			}
		}
		return ok(userList.render(session("name"), User.all()));
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
			flash("success", "Your e-mail is now verified. To login click on the button below");
			Logger.info("e-mail is now verified");
			message = " ";
		} else {
			flash("error", "Verification period is expired. If you want to receive a new verification mail, click on the button 'Resend'");
			Logger.info("Verification period is expired");
			message = " ";
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
		flash("success", "Your profile is updated. To go to the profile page click on the button below");
			message = " ";
		} else {
			flash("error", "Verification period is expired. If you want to receive a new verification mail, click on the button 'Resend'");
			message = " ";
		}
		return ok(verifyEmailUpdate.render(message, u.username));
	}

	public static Result updatePhoto(long userId) {
		User u = User.find(userId);
		String subFolder = "user_profile" + File.separator + "user_" + userId;
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
	}

	@Security.Authenticated(CurrentUserFilter.class)
	public static Result changePassView() {
		User currentUser = User.find(session("name"));
		return ok(changePassView.render(currentUser));
	}

	@Security.Authenticated(CurrentUserFilter.class)
	public static Result changePass(long id) {
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateUser ");
		}

		String oldPass = updateForm.data().get("password");
		String newPass = updateForm.data().get("newPassword");
		String confPass = updateForm.data().get("confirmPassword");
		User cUser = User.find(id);
		cUser.updated = new Date();

		/* if only one password field is filled out */
		if (oldPass.isEmpty() && !newPass.isEmpty() || newPass.isEmpty()
				&& !oldPass.isEmpty()) {
			flash("error", "If you want to change your password,"
					+ " please fill out both fields");
			return badRequest(changePassView.render(cUser));
		}
		/* if there was a input in password fields */
		if (!oldPass.isEmpty() && !newPass.isEmpty()) {
			if (HashHelper.checkPass(oldPass, cUser.password) == false) {
				flash("error", "You're old password is incorrect!");
				return badRequest(changePassView.render(cUser));
			}
			if (newPass.length() < 6) {
				flash("error", "The password must be at least 6 characters");
				return badRequest(changePassView.render(cUser));
			}
			cUser.password = HashHelper.createPassword(newPass);
		}
		if (!newPass.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return badRequest(changePassView.render(cUser));
		}
		cUser.save();
		flash("success", "Password changed!");
		Logger.info(cUser.username + " is updated");
		return ok(profile.render(cUser));

	}

	public static Result inputEmailView() {
		return ok(inputEmail.render());
	}

	public static Result newPassword() {
		DynamicForm forma = Form.form().bindFromRequest();
		if (forma.hasErrors()) {
			return redirect("/inputEmail");
		}
		String mail = forma.data().get("email");

		if (mail.equals("Email")) {
			flash("error", "Email is required for new password !");
			return badRequest(inputEmail.render());
		}
		return ok(inputEmail.render());
	}

	public static Result sendRequest() {
		DynamicForm forma = Form.form().bindFromRequest();
		String mail = forma.data().get("email");
		User u = User.getUser(mail);
		if (mail.equals("Email")) {
			flash("error", "Email is required for new password !");
			return badRequest(inputEmail.render());
		}
		if (User.getUser(mail) == null) {
			flash("error", "You are not registered!");
			return badRequest(inputEmail.render());
		}
		String verificationEmail = EmailVerification.addNewRecord(u.id);
		MailHelper.send(mail,
				"Click on the link below to set a new password <br>"
						+ "http://localhost:9000/setNewPassView/"
						+ verificationEmail);
		flash("success", "Request for password has been sent on this email: "
				+ mail);
		return ok(inputEmail.render());
	}
	
	public static Result setNewPassView(String email) {
		return ok(setNewPassView.render(email));
	}

	public static Result setNewPassword(String email) {

		DynamicForm forma = Form.form().bindFromRequest();
		String mail = forma.data().get("email");
		String newPassword = forma.data().get("newPassword");
		String confPass = forma.data().get("confirmPassword");
		if (forma.hasErrors()) {
			return redirect("/setNewPassword");
		}

		if (mail.isEmpty()) {
			flash("error", "Email is required for registration !");
			return badRequest(setNewPassView.render(mail));
		}
		if (newPassword.length() < 6) {
			flash("error", "Password must be at least 6 characters!");
			return badRequest(setNewPassView.render(mail));
		} else if (!newPassword.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return badRequest(setNewPassView.render(mail));
		}

			User u = User.getUser(mail);
			if(u == null) {
				flash("error", "Please enter the email you registered with!");
				return badRequest(setNewPassView.render(mail));
			}
			u.password = HashHelper.createPassword(newPassword);
			EmailVerification setVerified = new EmailVerification(u.id, true);
			setVerified.save();
			u.updated = new Date();
			session("name", u.username);
			u.save();
			Logger.info(u.username + " logged in. With password: " +newPassword);	
			Logger.debug(u.password);
			Logger.debug("Hash: " +  HashHelper.checkPass(newPassword,u.password) );
		return ok(profile.render(u));
	}

}
