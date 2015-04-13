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

		Form<Company> submit = Form.form(Company.class).bindFromRequest();

		
		
		if (companyForm.hasErrors()|| submit.hasGlobalErrors()) {
			return ok(signup.render(new Form<User>(User.class), submit));
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
			return ok(signup.render(new Form<User>(User.class), submit));
		} else if (mail.equals("Email")) {
			flash("error", "Email is required for registration !");
			return ok(signup.render(new Form<User>(User.class), submit));
		} else if (password.length() < 6) {
			flash("error", "Password must be at least 6 characters!");
			return ok(signup.render(new Form<User>(User.class), submit));
		} else if (!password.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return ok(signup.render(new Form<User>(User.class), submit));
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
			return ok(signup.render(new Form<User>(User.class), submit));

		} else {
			flash("error", "Username or email allready exists!");
			Logger.info("Username or email allready exists!");
			return ok(signup.render(new Form<User>(User.class), submit));
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
	 * Delete user by id. Delete is possible only for own deletion, or if it's
	 * done by Admin.
	 * 
	 * @param id
	 *            Long
	 * @return Result renders the same view
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deleteCompany(long id) {
		Company currentCompany = Company.findById(id);
		if ( currentCompany == null){
			flash("error", "Company with id: " + id + " doesnt exists!");
			return badRequest(userList.render(SuperUser.allSuperUsers()));
		}
		Company.delete(id);	
		return ok(userList.render(SuperUser.allSuperUsers()));
	}
	

	/**
	 * Upload company profile photo
	 * @param id of the company long
	 * @return 
	 */
	@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result updatePhoto(long companyId) {
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

	
	public static Result searchCompanyView() {
		List<Company> companys = Company.all();
		return ok(searchCompany.render(companys));
	}
	
	
	/**
	 * Search method for coupons. If search is unsuccessful a flash message is
	 * sent
	 * 
	 * @param string
	 * @return renders index with matching coupons //TODO render a different
	 *         view for search result
	 *
	 */
	public static Result searchCompany(String qc) {
		
		List<Company> allCompany = Company.find.where().ilike("name", "%" + qc + "%")
				.findList();
		
		//Getting only activated coupons from search result.
		
		List<Company> companys = new ArrayList<Company>();
		for(Company company: allCompany){
			companys.add(company);		
		}
	
		if((companys.isEmpty())){
			flash("error", "No resoult for this search");
			return badRequest(searchCompany.render(companys));
		}

		Logger.info(session("name") + " searched for: \"" + qc + "\"");
		return ok(searchCompany.render(companys));
	}
	
}
