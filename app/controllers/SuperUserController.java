package controllers;

import helpers.AdminFilter;
import helpers.CurrentCompanyFilter;
import helpers.HashHelper;
import helpers.MailHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.*;
import helpers.*;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;
import views.html.user.*;
import views.html.admin.users.*;

public class SuperUserController extends Controller {

	@Security.Authenticated(SuperUserFilter.class)
	public static Result changePass(String email) {
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateUser ");
		}
		String oldPass = updateForm.data().get("password");
		String newPass = updateForm.data().get("newPassword");
		String confPass = updateForm.data().get("confirmPassword");
		SuperUser superUser = SuperUser.getSuperUser(email);

		/* if only one password field is filled out */
		if (oldPass.isEmpty() && !newPass.isEmpty() || newPass.isEmpty()
				&& !oldPass.isEmpty()) {
			flash("error", "If you want to change your password,"
					+ " please fill out both fields");
			return TODO;
		}
		/* if there was a input in password fields */
		if (!oldPass.isEmpty() && !newPass.isEmpty()) {
			if (HashHelper.checkPass(oldPass, superUser.password) == false) {
				flash("error", "You're old password is incorrect!");
				return TODO;
			}
			if (newPass.length() < 6) {
				flash("error", "The password must be at least 6 characters");
				return TODO;
			}
			superUser.password = HashHelper.createPassword(newPass);
		}
		if (!newPass.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return TODO;
		}
		User user;
		Company company;
		if (superUser.isUser()) {
			user = superUser.getUser();
			user.updated = new Date();
			user.save();
			flash("success", "Password changed!");
			Logger.info(user.username + " is updated");
			return ok(profile.render(user));
		}
		company = superUser.getCompany();
		company.updated = new Date();
		company.save();
		flash("success", "Password changed!");
		Logger.info(company.name + " is updated");
		return ok(profile.render(company));

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
	@Security.Authenticated(SuperUserFilter.class)
	public static Result searchUsers(String qU) {
		List<User> users = User.getFind().where()
				.ilike("username", "%" + qU + "%").findList();
		List<Company> allCompanies = Company.getFind().where().ilike("name", "%" +qU +"%").findList();
		List<SuperUser> merged = new ArrayList<SuperUser>();
		merged.addAll(users);
		merged.addAll(allCompanies);
		
		if (merged.isEmpty()) {
			flash("error", "No such user or company");
			return badRequest(userList.render( SuperUser.allSuperUsers()));
		}

		return ok(userList.render(merged));
	}
	
	
	/**
	 * Method sends the current user to the userUpdate() method
	 * 
	 * @return Renders the user update view for editing profile
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result userUpdateView() {
		User currentUser = User.find(session("name"));
		Company currentCompany = Company.find(session("name"));
		
		if(currentUser == null){
			return ok(userUpdate.render(currentCompany));
		}else if(currentCompany == null){
			return ok(userUpdate.render(currentUser));
		}else{
			return TODO;
		}
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
	public static Result adminEditUserView(String email) {

		List<User> adminList = User.findAdmins(true);
		
		User userToUpdate = User.findByEmail(email);
		Company companyToUpdate = Company.findByEmail(email);
		
		if(userToUpdate != null){
			Logger.debug("In user edit");
			return ok(adminEditUser
					.render(userToUpdate, adminList));
		}else if(companyToUpdate != null){
			Logger.debug("In company edit");
			return ok(adminEditUser
					.render(companyToUpdate, adminList));
		}else{
			return TODO;
		}
		
	}

	/**
	 * Renders the profile page view
	 * 
	 * @param username
	 * @return Result
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result profilePage(String username) {
		User u = User.find(username);
		Company c = Company.find(username);
		
		if (u != null) {
			return ok(profile.render(u));
		}else if(c != null){
			return ok(profile.render(c));
		}
		return redirect("/");
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

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result verifyEmailUpdate(String id) {
		Company c = Company.find(session("name"));
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
		return ok(verifyEmailUpdate.render(message, c.name));
	}

	
	/**
	 * TODO finish this method
	 * @return
	 */
	public static Result newPassword(String id) {
		String userEmail = ResetPasword.findByID(id);
		if ( userEmail == null) {
			flash("error", "Password reset expired or invalid request");
			redirect("/");
		}	
		return ok(newPassword.render(id));
	}
	
	
	/**
	 * TODO Method for sending reset password email.
	 * @return
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result sendRequest(String email) {
		User user = User.getUser(email);
		Company company = Company.findByEmail(email);
		SuperUser superuser;
		
		if (user == null && company == null) {
			flash("error", "User with email you sent does not exist.");
			return redirect("/loginpage");
		}
		
		if(user == null){
			superuser = company;
		}else{
			superuser = user;
		}
			
		String verificationEmail = ResetPasword.createRequest(superuser.email);
		MailHelper.send(email,
				"Click on the link below to set a new password <br>"
						+ "http://" + UserController.PATH + "/newPassword/"
						+ verificationEmail);
		flash("success", "Request for password has been sent on this email: "
				+ email);
		return  redirect("/");
	}
}
