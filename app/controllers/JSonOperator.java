package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.internet.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.*;
import models.*;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class JSonOperator extends Controller {
	
	static String PATH = Play.application().configuration().getString("PATH");
	
	/**
	 * TODO comments
	 * @return
	 */
	public static Result login() {
		JsonNode json = request().body().asJson();
		String mail = json.findPath("email").textValue();
		String password = json.findPath("password").textValue();

		if (mail == null) {
			Logger.info("Login error, mail is null");
			return badRequest(JSonHelper.messageToJSon("error", "Email is null"));
			
		} else if (password == null) {
			Logger.info("Login error, password is null");
			return badRequest(JSonHelper.messageToJSon("error", "Password is null"));
			
		} else if (mail.isEmpty()) {
			Logger.info("Invalid login form, Email is empty ");
			return badRequest(JSonHelper.messageToJSon("error", "Email is empty"));
			
		} else if (password.length() < 6) {
			Logger.info("Invalid login form, Short password");
			return badRequest(JSonHelper.messageToJSon("error", "Password length less then 6 chars"));
			
		} else if (User.verifyLogin(mail, password) == true) {
			User cc = User.getUser(mail);
			Logger.info(cc.username + " logged in");
			session("name", cc.username);
			session("email", cc.email);
			System.out.println("DEBUG ********** LOGIN ");
			return ok(JSonHelper.couponListToJson(Coupon.approvedCoupons()));
			
		} else if (Company.verifyLogin(mail, password) == true) {
			Company cc = Company.findByEmail(mail);
			Logger.info(cc.name + " logged in");
			return ok(JSonHelper.companyToJSon(cc));
		}
		Logger.info("User tried to login with invalid email or password");
		return badRequest(JSonHelper.messageToJSon("error", "Invalid email or password"));
	}
	
	/**
	 * This method registers a new user. It parses all data from a JsonNode and registers the new user
	 * with the provided details. It expects a valid email, that is already registered for the android device,
	 * and sets the email to 'verified' in the database without sending a verification email.
	 * The date provided in json has to be in this form: dd/mm/yyyy  
	 * @return
	 */
	public static Result register() {
		JsonNode json = request().body().asJson();
		String username = json.findPath("username").textValue();
		String surname = json.findPath("surname").textValue();
		String email = json.findPath("email").textValue();
		String dob = json.findPath("dob").textValue();
		String password = json.findPath("password").textValue();
		String confPass = json.findPath("confirmPassword").textValue();

		if (username.length() < 4 || username.equals("Username")) {
			return badRequest(JSonHelper.messageToJSon("error","Usernam must be at least 4 chatacters"));
		} else if (email.isEmpty()) {
			return badRequest(JSonHelper.messageToJSon("error","Email is required for registration !"));
		} else if (password.length() < 6) {
			return badRequest(JSonHelper.messageToJSon("error","Password must be at least 6 characters!"));
		} else if (!password.equals(confPass)) {
			return badRequest(JSonHelper.messageToJSon("error","Passwords don't match, try again "));
		}
		else if (User.verifyRegistration(username, email) == true) {
			try {
				Date dayOfBirth = DateHelper.getDate(dob);
				String hashPass = HashHelper.createPassword(password);
				long id = User.createUser(username, surname, dayOfBirth,"", "", "", email, hashPass, false);
				EmailVerification.makeNewRecord(id, true);
				return ok(JSonHelper.messageToJSon("info","You are successfuly registered! "
												+ "You can now login with the following email: " + email));
			} catch (Exception e) {
				Logger.error("error", "Registration error" + e.getMessage(), e);
				return badRequest(JSonHelper.messageToJSon("error","Internal server error"));
			}
		}
		Logger.info("Username or email allready exists!");
		return badRequest(JSonHelper.messageToJSon("error","Username or email allready exists!"));

	}
	
	
	
	/* GET requests */
	
	/**
	 * Method returns the profile page data for user as JSon data.
	 * It receives an id as String, parses the id to long, and finds the user by id.
	 * Finally it returns the object user in JSon format
	 * @param id String
	 * @return 
	 */
	public static Result userProfile() {
		JsonNode json = request().body().asJson();
		String id = json.findPath("id").textValue();
		User user = User.find(Long.parseLong(id));
		if (user != null)
			return ok(JSonHelper.userToJSon(user));
		return badRequest(JSonHelper.messageToJSon("erorr", "An error occured"));
	}
	
	/**
	 * Method returns the profile page data for company as JSon data.
	 * It receives an id as String, parses the id to long, and finds the company by id.
	 * Finally it returns the object company in JSon format
	 * @param company id String
	 * @return 
	 */
	public static Result companyProfile() {
		JsonNode json = request().body().asJson();
		String id = json.findPath("id").textValue();
		Company company = Company.findById(Long.parseLong(id));
		if (company != null)
			return ok(JSonHelper.companyToJSon(company));
		return badRequest(JSonHelper.messageToJSon("erorr", "An error occured"));
	}
	
	/**
	 * Method for company search.
	 * Returns the search result as list of companies that match or are similar 
	 * to the provided input.
	 * If the search has no result a info message is returned.
	 * @param company name or part of it
	 * @return JSon (ArrayNode)
	 */
	public static Result searchCompany() {
		JsonNode json = request().body().asJson();
		String name = json.findPath("name").textValue();
		List<Company> searchedCompanies = Company.find.where().ilike("name", "%" + name + "%").findList();
		if (searchedCompanies.isEmpty()) {
			return badRequest(JSonHelper.messageToJSon("info","No result for this search"));
		}
		return ok(JSonHelper.companyListToJSon(searchedCompanies));

	}
	
	public static Result searchCoupon() {
		JsonNode json = request().body().asJson();
		String name = json.findPath("name").textValue();
		List<Coupon> searchedCoupons = Coupon.find.where().ilike("name", "%" + name + "%").findList();
		if (searchedCoupons.isEmpty()) {
			return badRequest(JSonHelper.messageToJSon("info","No result for this search"));
		}
		return ok(JSonHelper.couponListToJson(searchedCoupons));

	}
	
	/**
	 * Method returns a specific coupon in JSon format.
	 * It receives an id as String and returns the coupon by provided id.
	 * @return
	 */
	public static Result showCoupon() {
		JsonNode json = request().body().asJson();
		long id = Long.parseLong(json.findPath("id").textValue());
		Coupon coupon = Coupon.find(id);
		if(coupon == null ){
			Logger.error("error", "Coupon null at showCoupon()");
			flash("error", "Ooops, error has occured.");
			return badRequest(JSonHelper.messageToJSon("error", "Internal server error"));
		}
		return ok(JSonHelper.couponToJSon(coupon));

	}

	/**
	 * Method for updating user profile. It finds the user by
	 * provided id. Parses the data from the received JsonNode
	 * and saves the new values to the database.
	 * Returns a JSon message with a 'info' tag if the update is successful,
	 * or an error message if update fails
	 * @param id of the user
	 * @return 
	 */
	public static Result updateUser() {
			JsonNode json = request().body().asJson();
			String id = json.findPath("id").textValue();
			String username = json.findPath("username").textValue();
			String surname = json.findPath("surname").textValue();
			String email = json.findPath("email").textValue();
			String adress = json.findPath("adress").textValue();
			String city = json.findPath("city").textValue();

			try {
				User cUser = User.find(Long.parseLong(id));
				cUser.username = username;
				cUser.surname = surname;
				cUser.adress = adress;
				cUser.city = city;
				cUser.updated = new Date();

				if (!cUser.email.equals(email)) {
					String verificationEmail = EmailVerification
							.addNewRecord(cUser.id);
					MailHelper.send(email,"Click on the link below to verify your e-mail adress <br>"
							+ "http://" + PATH
							+ "/verifyEmail/" + verificationEmail);
					cUser.email = email;
					cUser.save();
					Logger.info("A verification mail has been sent to this email: " + email);
					return ok(JSonHelper.messageToJSon("info","Update successful, a verification mail has been sent to this address: " + email));
				}
				cUser.email = email;
				cUser.save();
				flash("success", "Profile updated!");
				Logger.info(cUser.username + " updated profile from mobile app");
				session("name", cUser.username);
				return ok(JSonHelper.messageToJSon("info","Update successful"));
			} catch (Exception e) {
				Logger.error("Error at updateUser: " + e.getMessage(), e);
				return badRequest(JSonHelper.messageToJSon("error","Internal server error"));
			}		
	}
	
}
