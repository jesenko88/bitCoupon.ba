package controllers;

import helpers.AdminFilter;
import helpers.CurrentCompanyFilter;
import helpers.FileUpload;
import helpers.HashHelper;
import helpers.MailHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Company;
import models.Coupon;
import models.EmailVerification;
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
import views.html.searchCompany;
import views.html.signup;
import views.html.admin.users.userList;
import views.html.company.companyPanel;
import views.html.company.companyProfile;
import views.html.company.notificationsForCompany;
import views.html.user.userUpdate;
import api.JSonHelper;

public class CompanyController extends Controller {

	static String PATH = Play.application().configuration().getString("PATH");
	static Form<Company> companyForm = new Form<Company>(Company.class);
	public static final Company COMPANY_ADMIN = Company.findById(1);
	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");


	/**
	 * Pulls the input form from the registration form fields and creates a new
	 * user in the Database.
	 * 
	 * @return redirects to the index page with welcome, or renders the page
	 *         repeatedly if any error occurs
	 */
	public static Result registerC() {

		Form<Company> registrationForm = Form.form(Company.class).bindFromRequest();

		if (companyForm.hasErrors() || registrationForm.hasGlobalErrors()) {
			return ok(signup.render(new Form<User>(User.class), registrationForm));

		}
		// Exception handling.
		try {

			String name = companyForm.bindFromRequest().get().name;
			String mail = companyForm.bindFromRequest().get().email;
			String logo = companyForm.bindFromRequest().get().logo;
			String password = companyForm.bindFromRequest().get().password;
			String hashPass = HashHelper.createPassword(password);
			String confPass = companyForm.bindFromRequest()
					.field("confirmPassword").value();
			String adress = companyForm.bindFromRequest().get().adress;
			String city = companyForm.bindFromRequest().get().city;
			String contact = companyForm.bindFromRequest().get().contact;

			if (name.length() < 4 || name.equals("Name")) {
				flash("error", Messages.get("company.longName"));
				return ok(signup.render(new Form<User>(User.class), registrationForm));
			} else if (password.length() < 6) {
				flash("error", Messages.get("password.shortPassword"));
				return ok(signup.render(new Form<User>(User.class), registrationForm));
			} else if (!password.equals(confPass)) {
				flash("error", Messages.get("password.dontMatch"));
				return ok(signup.render(new Form<User>(User.class), registrationForm));
			}

			else if (Company.verifyRegistration(name, mail) == true) {

				long id = Company.createCompany(name, mail, hashPass, logo,
						adress, city, contact);
				String verificationEmail = EmailVerification.addNewRecord(id);

				MailHelper.send(mail, Messages.get("registration.mail.verificationLinkText") + "<br>"
								+ "http://" + PATH + "/verifyEmail/"
								+ verificationEmail);
				flash("success", Messages.get("registration.mail.flash.verification"));
				Logger.info("A verification mail has been sent to email address");
				return ok(signup.render(new Form<User>(User.class), registrationForm));

			} else {
				flash("error", Messages.get("registration.emailAlreadyExists"));
				Logger.info("Username or email allready exists!");
				return ok(signup.render(new Form<User>(User.class), registrationForm));

			}
		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at registration: " + e.getMessage());
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
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			flash("error", ERROR_MSG_CLIENT);
			return redirect("/updateCompany");
		}
		try{
			
			String name = updateForm.data().get("name");
			String email = updateForm.data().get("email");
			
			Company company = Company.findById(id);
			Form<Company> companyForm = Form.form(Company.class).fill(company);
			company.name = name;
			company.updated = new Date();
			
			if (!company.email.equals(email)) {
				String verificationEmail = EmailVerification.addNewRecord(company.id);
				MailHelper.send(email,
						Messages.get("registration.mail.verificationLinkText") + "<br>"
								+ "http://"+ PATH + "/verifyEmailUpdate/"
								+ verificationEmail);
				company.email = email;
				company.save();
				flash("success", Messages.get("registration.mail.flash.verification") + email);
				return ok(userUpdate.render(null, companyForm, company));
			}
			company.email = email;
			company.save();
			flash("success", Messages.get("profile.updated"));
			Logger.info(company.name + " is updated");
			session("name", company.name); 
			return ok(userUpdate.render(null, companyForm, company));			
		}catch(Exception e){
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at company update: " +e.getMessage());
			return redirect("updateCompany");
		}

	}

	/*public static Result approveCompany(long id){
		Company c = Company.findById(id);
		c.status = true;
		c.save();
		flash("succes", "Company " +c.name +" has been approved");
		return ok(couponsAll.render( Coupon.approvedCoupons(), Coupon.nonApprovedCoupons()));
	}*/
	
	/**
	 * Updates the user from the Admin control.
	 * 
	 * @param id
	 *            of the user to update
	 * @return Result render the vies
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result adminUpdateCompany(long id) {
		if (companyForm.hasErrors()) {
			flash("error", ERROR_MSG_CLIENT);
			return redirect("/@editUser/:" + id);
		}
		try{			
			String name = companyForm.bindFromRequest().field("name").value();
			String email = companyForm.bindFromRequest().field("email").value();
			String newPass = companyForm.bindFromRequest().field("newPassword").value();
			
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
			flash("success", Messages.get("Company") + company.name + Messages.get("updated"));
			Logger.info(session("name") + " updated company: " + company.name);
			return ok(userList.render(SuperUser.allSuperUsers()));
		}catch(Exception e){
			flash("error", ERROR_MSG_ADMIN);
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
				flash("error", Messages.get("company.notExist"));
				return badRequest(userList.render(SuperUser.allSuperUsers()));
			}
			Company.delete(id);	
			return ok(userList.render(SuperUser.allSuperUsers()));
		}catch(Exception e){
			flash("error", ERROR_MSG_ADMIN);
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
			Company company = Company.findById(companyId);
			String subFolder = "company_profile" + File.separator + "company_" + companyId;
			boolean checkIfDirectoryExists = new File(FileUpload.IMAGES_FOLDER
					+ subFolder).isDirectory();
			if (checkIfDirectoryExists) {
				String assetsPath = FileUpload.imageUpload(subFolder);
				Logger.debug(assetsPath);
				company.logo = assetsPath;
				company.save();
				flash("success", Messages.get("company.updatePhoto"));
				return redirect("/profile/@" + company.name);
			} else {
				new File(FileUpload.IMAGES_FOLDER + subFolder).mkdirs();
				String assetsPath = FileUpload.imageUpload(subFolder);
				Logger.debug(assetsPath);
				company.logo = assetsPath;
				company.save();
				flash("success", Messages.get("company.updatePhoto"));
				return redirect("/profile/@" + company.name);
			}			
		}catch(Exception e){
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at update photo: " +e.getMessage());
			return redirect("/");
		}
	}	

	/**
	 * Renders the companyPanel page
	 * @param id of the company
	 * @return
	 */
	@Security.Authenticated(CurrentCompanyFilter.class)
	public static Result companyPanel(long id) {
		Company company = Company.findById(id);
		List<Coupon> ownedCoupons = Coupon.ownedCoupons(company.id);
		//Exception handling.
		if(company == null || ownedCoupons == null){
			flash("error", ERROR_MSG_CLIENT);
			return redirect("/");
		}
		return ok(companyPanel.render(company, ownedCoupons ) );
	}

	/**
	 * Shows the company profile page
	 * @param id of the company
	 * @return
	 */
	public static Result showCompanyProfile(long id) {
		Company current = Company.findById(id);
		List<Coupon> coupons = current.coupons;
		//Exception handling.
		if(current == null || coupons == null){
			flash("error", ERROR_MSG_CLIENT);
			return redirect("/");
		}
		return ok(companyProfile.render( current, coupons));

	}
	
	/**
	 * Returns the list of all companies
	 * If the request accepts html then the searchCompany page is
	 * rendered, otherwise it returns the list as JSon  
	 * @return 
	 */
	public static Result listCompanies() {
		List<Company> companies = Company.all();
		if(companies == null)
			companies = new ArrayList<Company>();
		
		if (request().accepts("text/html")){
			return ok(searchCompany.render(companies));
		}
		return ok(JSonHelper.companyListToJSon(companies));
	}
	
	/**
	 * Search method for companies. If search is unsuccessful a flash message is
	 * sent
	 * @param string
	 * @return renders index with matching coupons //TODO render a different
	 *         view for search result
	 *
	 */
	public static Result searchCompany(String name) {
		List<Company> searchedCompanies = Company.find.where()
				.ilike("name", "%" + name + "%").findList();

		if ((searchedCompanies.isEmpty())) {
			flash("error", Messages.get("search.noResult"));
			return badRequest(searchCompany.render(new ArrayList<Company>()));
		}
		Logger.info(session("name") + " searched for: \"" + name + "\"");
		return ok(searchCompany.render(searchedCompanies));
	}
		
	/**
	 * Renders the company notifications view.
	 * Before redirecting, the notifications counter is being reset
	 * @param id of the Company
	 * @return
	 */
	public static Result notifications(long id) {
		Company c = Company.findById(id);
		c.notifications = 0;
		c.save();
		List<TransactionCP> transactions = TransactionCP.allFromCompany(id);
		return ok(notificationsForCompany.render(transactions));
	}
}
