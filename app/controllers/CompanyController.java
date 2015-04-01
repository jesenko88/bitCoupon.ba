package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import views.html.*;
import views.html.user.*;
import views.html.admin.users.*;
import views.html.coupon.*;
import views.html.company.*;
import helpers.AdminFilter;
import helpers.CurrentCompanyFilter;
import helpers.FileUpload;
import helpers.HashHelper;
import helpers.MailHelper;
import models.*;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class CompanyController extends Controller {
public static final String PATH = "localhost:9000"; 
	static Form<Company> companyForm = new Form<Company>(Company.class);


	/**
	 * Pulls the input form from the registration form fields and creates a new
	 * user in the Database.
	 * 
	 * @return redirects to the index page with welcome, or renders the page
	 *         repeatedly if any error occurs
	 */
	public static Result registerC() {

		if (companyForm.hasErrors()) {
			return redirect("/companySignup");
		}

		String name = companyForm.bindFromRequest().get().name;
		String mail = companyForm.bindFromRequest().get().email;
		String logo = companyForm.bindFromRequest().get().logo;
		String password = companyForm.bindFromRequest().get().password;
		String hashPass = HashHelper.createPassword(password);
		String confPass = companyForm.bindFromRequest().field("confirmPassword")
				.value();

		if (name.length() < 4 || name.equals("Name")) {
			flash("error", "Name must be at least 4 chatacters");
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

		else if (Company.verifyRegistration(name, mail) == true) {

			long id = Company.createCompany(name, mail, hashPass, logo);
			String verificationEmail = EmailVerification.addNewRecord(id);

			MailHelper.send(mail,
					"Click on the link below to verify your e-mail adress <br>"
							+ "http://" + PATH + "/verifyEmail/"
							+ verificationEmail);
			flash("success", "A verification mail has been sent to your email address!");
			Logger.info("A verification mail has been sent to email address");
			return ok(signup.render());

		} else {
			flash("error", "Username or email allready exists!");
			Logger.info("Username or email allready exists!");
			return badRequest(signup.render());
		}
	}
	
	

	/**
	 * Method sends the current user to the userUpdate() method
	 * 
	 * @return Renders the user update view for editing profile
	 */
	//@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result companyUpdateView() {   
		Company currentCompany = Company.find(session("name"));
		return ok(userUpdate.render(currentCompany));
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
	public static Result updateCompany(long id) {
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateCompany");
		}

		String name = updateForm.data().get("name");
		String email = updateForm.data().get("email");
		String logo = updateForm.data().get("logo");
		String oldPass = updateForm.data().get("password");
		String newPass = updateForm.data().get("newPassword");

		Company company = Company.findById(id);
		company.name = name;
		// cUser.email = email;
		company.updated = new Date();

		if (!company.email.equals(email)) {
			String verificationEmail = EmailVerification.addNewRecord(company.id);
			MailHelper.send(email,
					"Click on the link below to verify your e-mail adress <br>"
							+ "http://"+ PATH + "/verifyEmailUpdate/"
							+ verificationEmail);
			company.email = email;
			company.save();
			flash("success",
					"A new verification email has been sent to this e-mail: "
							+ email);
			return ok(userUpdate.render(company));
		}
		company.email = email;
		company.save();
		flash("success", "Profile updated!");
		Logger.info(company.name + " is updated");
		return ok(userUpdate.render(company));

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
	public static Result adminEditCompanyView(long id) {

		if (Sesija.adminCheck(ctx()) != true) {
			return redirect("/");
		}
		List<User> adminList = User.findAdmins(true);
		Company companyToUpdate = Company.findById(id);
		return ok(adminEditUser
				.render(companyToUpdate, adminList));
	}

	/**
	 * Updates the user from the Admin control.
	 * 
	 * @param id
	 *            of the user to update
	 * @return Result render the vies
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result adminUpdateCompany(long id) {
		List<User> adminList = User.findAdmins(true);

		if (companyForm.hasErrors()) {
			return redirect("/@editUser/:" + id); // provjeriti
		}

		String name = companyForm.bindFromRequest().field("name").value();
		String email = companyForm.bindFromRequest().field("email").value();
		String newPass = companyForm.bindFromRequest().field("newPassword")
				.value();

		Company company = Company.findById(id);
		company.name = name;
		company.email = email;
		/*
		 * if admin doesn't explicitly change the users password, it stays
		 * intact
		 */

		if (newPass.length() > 5) {
			company.password = HashHelper.createPassword(newPass);
		}
		company.updated = new Date();
		company.save();
		flash("success", "Company " + company.name + " updated!");
		Logger.info(session("name") + " updated company: " + company.name);
		return ok(userList.render(SuperUser.allSuperUsers()));
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
	public static Result searchCompanies(String qU) {
		List<Company> companies = Company.getFind().where().ilike("username", "%" + qU + "%").findList();
		List<User> allUsers = User.all();
		List<SuperUser> merged = new ArrayList<SuperUser>();
		
		merged.addAll(allUsers);
		merged.addAll(companies);
		
		if (companies.isEmpty()) {
			flash("error", "No such company");
			return badRequest(userList.render(merged)); 
		}

		return ok(userList.render(merged));
	}

	/**
	 * Renders the profile page view
	 * 
	 * @param username
	 * @return Result
	 */
	public static Result companyProfilePage(String name) {
		Company c = Company.find(name);
		if (!c.name.equals(session("name"))) {
			return redirect("/");
		}

		return ok(profile.render(c)); 
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
	public static Result deleteCompany(long id) {
		List<User> adminList = User.findAdmins(true);
		Company currentCompany = Company.findById(id);		
		Company.delete(id);
		
		return ok(userList.render(SuperUser.allSuperUsers()));
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

	@Security.Authenticated(CurrentCompanyFilter.class)
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

	public static Result updatePhoto(long companyId) {
		Company c = Company.findById(companyId);
		String subFolder = "user_profile" + File.separator + "user_" + companyId;
		boolean checkIfDirectoryExists = new File(FileUpload.IMAGES_FOLDER
				+ subFolder).isDirectory();
		if (checkIfDirectoryExists) {
			String assetsPath = FileUpload.imageUpload(subFolder);
			Logger.debug(assetsPath);
			c.logo = assetsPath;
			c.save();
			return redirect("/profile/@" + c.name);
		} else {
			new File(FileUpload.IMAGES_FOLDER + subFolder).mkdir();
			String assetsPath = FileUpload.imageUpload(subFolder);
			Logger.debug(assetsPath);
			c.logo = assetsPath;
			c.save();
			return redirect("/profile/@" + c.name);
		}
	}	

	/**
	 * Method for changing password.
	 * @param id
	 * @return
	 */
	@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result changePass(long id) {
		
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateUser ");		}

		String oldPass = updateForm.data().get("password");
		String newPass = updateForm.data().get("newPassword");
		String confPass = updateForm.data().get("confirmPassword");
		Company company = Company.findById(id);
		company.updated = new Date();
				
		/* if only one password field is filled out */
		if (oldPass.isEmpty() && !newPass.isEmpty() || newPass.isEmpty()
				&& !oldPass.isEmpty()) {
			flash("error", "If you want to change your password,"
					+ " please fill out both fields");
			return TODO;
		}
		/* if there was a input in password fields */
		if (!oldPass.isEmpty() && !newPass.isEmpty()) {
			if (HashHelper.checkPass(oldPass, company.password) == false) {
				flash("error", "You're old password is incorrect!");
				return TODO;
			}
			if (newPass.length() < 6) {
				flash("error", "The password must be at least 6 characters");
				return TODO;
			}
			company.password = HashHelper.createPassword(newPass);
		}
		if (!newPass.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return TODO;
		}
		
		company.save();
		flash("success", "Password changed!");
		Logger.info(company.name + " is updated");
		return ok(profile.render(company));

	}

	
	
	
	//TODO security
	public static Result companyPanel(long id) {
		Company company = Company.findById(id);
		if (!company.name.equals(session("name"))) {
			return redirect("/");
		}
		return ok(companyPanel.render(company, Coupon.ownedCoupons(company.id) ) );

	}

}
