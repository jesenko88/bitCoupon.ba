package controllersJSON;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.HashHelper;
import helpers.JSonHelper;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class JSonOperator extends Controller {

	
	public static Result login() {
			
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String mail = json.findPath("email").textValue();
		String password = json.findPath("password").textValue();
		System.out.println("DEBUG ******** EMAIL *******" + mail);
		
		if (mail == null || password == null) {
			Logger.info("Login error, mail or password is null");
			return badRequest();
		}

		if (mail.isEmpty() || password.length() < 6) {
			Logger.info("Invalid login form, mail empty or short password");
			ObjectNode info = Json.newObject();
			info.put("info", "Invalid login form, mail empty or short password");
			return badRequest(info);
		}
		if (User.verifyLogin(mail, password) == true) {
			User cc = User.getUser(mail);
			Logger.info(cc.username + " logged in");
			session("name", cc.username);
			session("email", cc.email);
			System.out.println("DEBUG ********** LOGIN ");
			return ok(JSonHelper.couponListToJson(Coupon.approvedCoupons() ));
		}
		if (Company.verifyLogin(mail, password) == true) {
			Company cc = Company.findByEmail(mail);
			Logger.info(cc.name + " logged in");
			return ok(JSonHelper.companyToJSon(cc) );
		}
		Logger.info("User tried to login with invalid email or password");
		return badRequest();
	}
	
	public static Result register(){
		JsonNode json = request().body().asJson();
		String username = json.findPath("username").textValue();
		String surname = json.findPath("surname").textValue();
		String mail = json.findPath("email").textValue();
		Date dob = json.findPath("dob").textValue();
		String gender = json.findPath("gender").textValue();
		String adress = json.findPath("adress").textValue();
		String city = json.findPath("city").textValue();
		String password = json.findPath("password").textValue();
		String hashPass = HashHelper.createPassword(password);
		String confPass = json.findPath("confirmPassword").textValue();

		if (username.length() < 4 || username.equals("Username")) {
			flash("error", "Usernam must be at least 4 chatacters");
			return badRequest();
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
		 * Creating new user if the username or mail is free for use, and there
		 * are no errors
		 */
		else if (User.verifyRegistration(username, mail) == true) {
			/*
			 * session().clear(); session("name", username);
			 */

			long id = User.createUser(username, surname, dob, gender, adress, city, mail, hashPass, false);
			String verificationEmail = EmailVerification.addNewRecord(id);

			MailHelper.send(mail,
					"Click on the link below to verify your e-mail adress <br>"
							+ "http://" + PATH + "/verifyEmail/"
							+ verificationEmail);
			flash("success", "A verification mail has been sent to your email address!");
			Logger.info("A verification mail has been sent to email address");
			return ok(Loginpage
					.render(" "));

		} else {
			flash("error", "Username or email allready exists!");
			Logger.info("Username or email allready exists!");
			return badRequest(signup.render());
		}

		
	}
}
