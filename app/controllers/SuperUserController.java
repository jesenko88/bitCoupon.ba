package controllers;

import helpers.AdminFilter;
import helpers.HashHelper;
import helpers.MailHelper;
import helpers.SuperUserFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Company;
import models.EmailVerification;
import models.ResetPasword;
import models.SuperUser;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.verifyEmail;
import views.html.verifyEmailUpdate;
import views.html.admin.users.adminEditUser;
import views.html.admin.users.userList;
import views.html.user.newPassword;
import views.html.user.profile;
import views.html.user.userUpdate;

public class SuperUserController extends Controller {
	
	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");

	@Security.Authenticated(SuperUserFilter.class)
	public static Result changePass(String email) {
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateUser ");
		}
		try{
			String oldPass = updateForm.data().get("password");
			String newPass = updateForm.data().get("newPassword");
			String confPass = updateForm.data().get("confirmPassword");
			SuperUser superUser = SuperUser.getSuperUser(email);
			
			/* if only one password field is filled out */
			if (oldPass.isEmpty() && !newPass.isEmpty() || newPass.isEmpty()
					&& !oldPass.isEmpty()) {
				flash("error", Messages.get("password.change.emptyField"));
				return redirect("/updateUser ");
			}
			/* if there was a input in password fields */
			if (!oldPass.isEmpty() && !newPass.isEmpty()) {
				if (HashHelper.checkPass(oldPass, superUser.password) == false) {
					flash("error", Messages.get("password.old.incorrect"));
					return redirect("/updateUser ");				}
				if (newPass.length() < 6) {
					flash("error", Messages.get("password.shortPassword"));
					return redirect("/updateUser ");				}
				superUser.password = HashHelper.createPassword(newPass);
			}
			if (!newPass.equals(confPass)) {
				flash("error", Messages.get("password.dontMatch"));
				return redirect("/updateUser ");			
			}
			User user;
			Company company;
			if (superUser.isUser()) {
				user = superUser.getUser();
				user.updated = new Date();
				user.save();
				flash("success", Messages.get("password.changed"));
				Logger.info(user.username + " is updated");
				return ok(profile.render(user));
			}
			company = superUser.getCompany();
			company.updated = new Date();
			company.save();
			flash("success", Messages.get("password.changed"));
			Logger.info(company.name + " is updated");
			return ok(profile.render(company));			
		}catch(Exception e){
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at changePass: " +e.getMessage(), e);
			return redirect("/");
		}

	}
	
	/**
	 * Search method for users. If search is unsuccessful a flash message is
	 * sent
	 * TODO: Find way to handle exceptions...
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
			flash("error", Messages.get("search.noResult"));
			return badRequest(userList.render( SuperUser.allSuperUsers()));
		}
		/* content negotiation */
		return ok(userList.render(merged));

	}
	
	
	/**
	 * Method sends the current user to the userUpdate() method
	 * 
	 * @return Renders the user update view for editing profile
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result userUpdateView() {
		User currentUser = User.findByEmail(session("email"));
		Company currentCompany = Company.findByEmail(session("email"));
		if(currentUser == null){
			Form<Company> companyForm = Form.form(Company.class).fill(currentCompany);
			return ok(userUpdate.render(null, companyForm, currentCompany));
		}else if(currentCompany == null){
			Form<User> userForm = Form.form(User.class).fill(currentUser);
			return ok(userUpdate.render(userForm, null, currentUser));
		}else{
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at userUpdateView");
			return redirect("/");
		}
	}
	
	
	/**
	 * Receives a user id, initializes the user, and renders the adminEditUser
	 * passing @_updateUserForm(userForm, superUser) the user to the view
	 * TODO: Handle exceptions.
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
			Form<User> userForm = Form.form(User.class).fill(userToUpdate);
			return ok(adminEditUser.render(userToUpdate, adminList, userForm, null));
		}else if(companyToUpdate != null){
			Form<Company> companyForm = Form.form(Company.class).fill(companyToUpdate);
			return ok(adminEditUser.render(companyToUpdate, adminList, null, companyForm));
		}else{
			Logger.debug("In company edit");
			flash("error", ERROR_MSG_ADMIN);
			return redirect("/");
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
			User user = User.find(username);
			Company company = Company.find(username);
			if (user != null) {
				return ok(profile.render(user));
			} else if (company != null) {
				return ok(profile.render(company));
			}
			flash("error", ERROR_MSG_ADMIN);
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
		try{
			EmailVerification recordToUpdate = EmailVerification.find(id);
			String message = "";
			if (recordToUpdate.createdOn.compareTo(new Date()) < 0) {
				EmailVerification.updateRecord(recordToUpdate);
				flash("success", Messages.get("registration.mail.verified"));
				Logger.info("e-mail is now verified");
				message = " ";
			} else {
				flash("error", Messages.get("registration.mail.expired"));
				Logger.info("Verification period is expired");
				message = " ";
			}
			return ok(verifyEmail.render(message));			
		}catch(Exception e){
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at verfyEmail: " +e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result verifyEmailUpdate(String id) {
		try{
			Company c = Company.find(session("name"));
			EmailVerification recordToUpdate = EmailVerification.find(id);
			String message = "";
			if (recordToUpdate.createdOn.compareTo(new Date()) < 0) {
				EmailVerification.updateRecord(recordToUpdate);
				flash("success", Messages.get("profile.email.updated"));
				message = " ";
			} else {
				flash("error", Messages.get("registration.mail.expired"));
				message = " ";
			}
			return ok(verifyEmailUpdate.render(message, c.name));			
		}catch(Exception e){
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at verfyEmailUpdate: " +e.getMessage(), e);
			return redirect("/");
		}
	}

	
	/**
	 * TODO check if method works properly.
	 * @return
	 */
	public static Result newPassword(String id) {
		String userEmail = ResetPasword.findByID(id);
		if ( userEmail == null) {
			flash("error", Messages.get("password.reset.expired"));
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
		try{
			User user = User.getUser(email);
			Company company = Company.findByEmail(email);
			SuperUser superuser;
			
			if (user == null && company == null) {
				flash("error", Messages.get("password.reset.invalidEmail"));
				return redirect("/loginpage");
			}
			
			if(user == null){
				superuser = company;
			}else{
				superuser = user;
			}
			
			String verificationEmail = ResetPasword.createRequest(superuser.email);
			MailHelper.send(
					Messages.get("newPasswordSubject"), 
					email,
					Messages.get("clickOnLinkForNewPassword")
					+ "<br>"
					+ "http://" + UserController.PATH + "/newPassword/"
					+ verificationEmail);
			flash("success", Messages.get("password.reset.requestSuccess") + email);
			return  redirect("/");			
		}catch(Exception e){
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at sendRequest: " +e.getMessage(), e);
			return redirect("/");
		}
	}
}
