package controllersJSON;

import java.util.Date;

import javax.mail.internet.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.*;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class JSonOperatorPOST extends Controller {
	

	
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
	 * TODO comments
	 * @return
	 */
	public static Result register() {
		JsonNode json = request().body().asJson();
		String username = json.findPath("username").textValue();
		String surname = json.findPath("surname").textValue();
		String mail = json.findPath("email").textValue();
		String dob = json.findPath("dob").textValue();
		String gender = json.findPath("gender").textValue();
		String adress = json.findPath("adress").textValue();
		String city = json.findPath("city").textValue();
		String password = json.findPath("password").textValue();
		String confPass = json.findPath("confirmPassword").textValue();

		if (username.length() < 4 || username.equals("Username")) {
			return badRequest(JSonHelper.messageToJSon("error","Usernam must be at least 4 chatacters"));
		} else if (mail.isEmpty()) {
			return badRequest(JSonHelper.messageToJSon("error","Email is required for registration !"));
		} else if (password.length() < 6) {
			return badRequest(JSonHelper.messageToJSon("error","Password must be at least 6 characters!"));
		} else if (!password.equals(confPass)) {
			return badRequest(JSonHelper.messageToJSon("error","Passwords don't match, try again "));
		}
		/* Creating new user */
		else if (User.verifyRegistration(username, mail) == true) {
			try {
				/* parsing date from string */
				Date dayOfBirth = DateHelper.getDate(dob);
				String hashPass = HashHelper.createPassword(password);
				long id = User.createUser(username, surname, dayOfBirth,gender, adress, city, mail, hashPass, false);
				String verificationEmail = EmailVerification.addNewRecord(id);
				/* temporary hard coded */
				MailHelper.send(mail,"Click on the link below to verify your e-mail adress <br>"
								+ "http://" + "localhost:9000"
								+ "/verifyEmail/" + verificationEmail);
				Logger.info("A verification mail has been sent to email address");
				return ok(JSonHelper.messageToJSon("info","A verification mail has been sent to this address: " + mail));
			} catch (Exception e) {
				Logger.error("error", "Registration error" + e.getMessage(), e);
				return badRequest(JSonHelper.messageToJSon("error","Ann error occured, please try again later"));
			}
		}
		Logger.info("Username or email allready exists!");
		return badRequest(JSonHelper.messageToJSon("error","Username or email allready exists!"));

	}
}
