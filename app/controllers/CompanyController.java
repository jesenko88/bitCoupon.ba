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
import play.db.ebean.Model.Finder;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

public class CompanyController extends Controller {
public static final String PATH = "localhost:9000"; 
	static Form<Company> companyForm = new Form<Company>(Company.class);
	public static final Company COMPANY_ADMIN = Company.findById(1);

	/**
	 * Pulls the input form from the registration form fields and creates a new
	 * user in the Database.
	 * 
	 * @return redirects to the index page with welcome, or renders the page
	 *         repeatedly if any error occurs
	 */
	public static Result registerC() {
		try{
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
			String adress = companyForm.bindFromRequest().get().adress;
			String city = companyForm.bindFromRequest().get().city;
			String contact = companyForm.bindFromRequest().get().contact;
			
			
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
				
				long id = Company.createCompany(name, mail, hashPass, logo, adress, city, contact);
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
		}catch(Exception e){
			flash("Error occured. If you are admin, please check logs.");
			Logger.error("Error at registration: " +e.getMessage());
			return redirect("companySignup");
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
	@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result updateCompany(long id) {
		try{
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
			session("name", company.name); 
			return ok(userUpdate.render(company));			
		}catch(Exception e){
			flash("error","Error while updateing company. If you're admin please check logs.");
			Logger.error("Error at company update: " +e.getMessage());
			return redirect("updateCompany");
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
	public static Result adminUpdateCompany(long id) {
		try{		
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
		}catch(Exception e){
			flash("Error occured while updating company, please check your logs.");
			Logger.error("Error at updateing company: " +e.getMessage());
			return redirect("/@editUser" +id);
		}
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
		try{
			Company currentCompany = Company.findById(id);
			if ( currentCompany == null){
				flash("error", "Company with id: " + id + " doesnt exists!");
				return badRequest(userList.render(SuperUser.allSuperUsers()));
			}
			Company.delete(id);	
			return ok(userList.render(SuperUser.allSuperUsers()));
		}catch(Exception e){
			flash("error", "Error occured while deleting company. Please check your logs.");
			Logger.error("Error at delete company: " +e.getMessage());
			return redirect("/");
		}			
		}
	

	/**
	 * Upload company profile photo
	 * @param id of the company long
	 * @return 
	 */
	@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result updatePhoto(long companyId) {
		try{
			Company c = Company.findById(companyId);
			String subFolder = "company_profile" + File.separator + "company_" + companyId;
			boolean checkIfDirectoryExists = new File(FileUpload.IMAGES_FOLDER
					+ subFolder).isDirectory();
			if (checkIfDirectoryExists) {
				String assetsPath = FileUpload.imageUpload(subFolder);
				Logger.debug(assetsPath);
				c.logo = assetsPath;
				c.save();
				return redirect("/profile/@" + c.name);
			} else {
				new File(FileUpload.IMAGES_FOLDER + subFolder).mkdirs();
				String assetsPath = FileUpload.imageUpload(subFolder);
				Logger.debug(assetsPath);
				c.logo = assetsPath;
				c.save();
				return redirect("/profile/@" + c.name);
			}			
		}catch(Exception e){
			flash("error", "Error occured while uploading photo. If you're admin please check logs.");
			Logger.error("Error at update photo: " +e.getMessage());
			return redirect("/");
		}
	}	

	@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result companyPanel(long id) {
		Company company = Company.findById(id);
		return ok(companyPanel.render(company, Coupon.ownedCoupons(company.id) ) );
	}

	/**
	 * 
	 */
	public static Result showCompanyProfile(long id) {
		Company current = Company.findById(id);
	//	Company company = Company.find(session("name"));
		List<Coupon> coupons = current.coupons;
		return ok(companyProfile.render( current, coupons));

	}

	
}
