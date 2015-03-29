package controllers;

import models.*;
import play.*;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.mvc.*;
import views.html.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import helpers.MailHelper;
import models.User;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import models.*;
import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	static String loginMsg = "Login to your account";
	static String name = null;

	public static class Contact {

		@Required
		@Email
		public String email;
		@Required
		public String message;

		public String phone;
		public String name;
	}

	/**
	 * @return render the index page
	 */
	public static Result index() {
		name = session("name");
		if (name == null) {
			return ok(index.render(null, Coupon.all()));
		}
		User currentUser = User.find(name);
		return ok(index.render(currentUser, Coupon.all()));

	}

	public static Result chooseRegistration() {
		return ok(chooseRegistration.render());
	}

	/**
	 * Pulls the value from two login fields and verifies if the mail exists and
	 * the password is valid by calling the verifyLogin() method from the User
	 * class.
	 * 
	 * @return redirects to index if the verification is ok, or reloads the
	 *         login page with a warning message.
	 */
	public static Result login() {
		Form<Login> login = new Form<Login>(Login.class);

		if (login.hasGlobalErrors()) {
			flash("error", "Invalid email or password");
			Logger.info("Login global error");
			return badRequest(Loginpage.render(" "));
		}

		String mail = login.bindFromRequest().get().email;
		String password = login.bindFromRequest().get().password;

		if (mail.isEmpty() || password.length() < 6) {
			flash("error", "Invalid login form, mail empty or short password");
			Logger.info("Invalid login form, mail empty or short password");
			return badRequest(Loginpage.render(" "));
		}
		Logger.debug("Mail: " + mail + " Pass: " + password);
		if (User.verifyLogin(mail, password) == true) {
			User cc = User.getUser(mail);
			session().clear();
			session("name", cc.username);
			flash("success", "You are logged in as: " + mail);
			Logger.info(cc.username + " logged in");
			return ok(index.render(cc, Coupon.all()));
		}
		flash("error", "Invalid email or password");
		Logger.info("User tried to login with invalid email or password");
		return badRequest(Loginpage.render(" "));
	}

	/**
	 * @return renders the loginpage view
	 */
	public static Result loginpage() {
		Logger.info(loginMsg);
		return ok(Loginpage.render(loginMsg));
	}

	/**
	 * Logs out the User and clears the session
	 * 
	 * @return redirects to index
	 */
	public static Result logout() {

		Logger.info(session("name") + " has logged out");
		session().clear();
		return redirect("/");
	}

	/**
	 * Renders the info page if user tries to access a page without needed
	 * permission
	 * 
	 * @return
	 */
	public static Result loginToComplete() {
		Logger.info("Login to complete page previewed");
		return badRequest(loginToComplete
				.render("Login to complete this action"));
	}

	/**
	 * Renders the contact form page
	 * 
	 * @return
	 */
	public static Result contact() {
		name = session("name");
		if (name == null) {
			return ok(contact.render(null, new Form<Contact>(Contact.class)));
		} else {
			User currentUser = User.find(name);
			Logger.info("User " + currentUser + " accessed the contact page");
			return ok(contact.render(currentUser, new Form<Contact>(
					Contact.class)));
		}
	}

	public static Promise<Result> sendMail() {
		final DynamicForm temp = DynamicForm.form().bindFromRequest();

		Promise<Result> holder = WS
				.url("https://www.google.com/recaptcha/api/siteverify")
				.setContentType("application/x-www-form-urlencoded")
				.post(String.format("secret=%s&response=%s",

				Play.application().configuration().getString("recaptchaKey"),
						temp.get("g-recaptcha-response")))
				.map(new Function<WSResponse, Result>() {

					public Result apply(WSResponse response) {

						JsonNode json = response.asJson();
						Form<Contact> submit = Form.form(Contact.class)
								.bindFromRequest();

						if (json.findValue("success").asBoolean() == true
								&& !submit.hasErrors()) {

							Contact newMessage = submit.get();
							String email = newMessage.email;
							String name = newMessage.name;
							String phone = newMessage.phone;
							String message = newMessage.message;

							Logger.info("Message sent via contact form");
							flash("success", "Message sent");
							MailHelper
									.sendFeedback(email, name, phone, message);
							return redirect("/contact");
						}

						if (json.findValue("error-codes").toString()
								.equals("missing-input-secret")) {
							flash("error", "You are missing secret key!");
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						if (json.findValue("error-codes").toString()
								.equals("invalid-input-secret")) {
							flash("error", "INVALID SECRET KEY!");
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						if (json.findValue("error-codes").toString()
								.equals("missing-input-response")) {
							flash("error", "The response parameter is missing.");
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						if (json.findValue("error-codes").toString()
								.equals("invalid-input-response")) {
							flash("error",
									"The response parameter is invalid or malformed.");
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						flash("error",
								"WARNING! An error occured! You need to fill in the captcha!");
						User currentUser = User.find(name);
						return ok(contact.render(currentUser, submit));
					}
				});
		return holder;
	}

}