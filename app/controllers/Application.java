package controllers;

import helpers.MailHelper;

import java.util.List;

import models.Category;
import models.Company;
import models.Coupon;
import models.Login;
import models.User;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Loginpage;
import views.html.contact;
import views.html.index;
import views.html.indexC;
import views.html.loginToComplete;
import views.html.signup;

import com.fasterxml.jackson.databind.JsonNode;

public class Application extends Controller {

	static String name = null;
	static String loginSuccess = Messages.get("login.Success");
	
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
		List<Coupon> approvedCoupons = Coupon.approvedCoupons();
		List<Category> categories = Category.all();

		//Handling exceptions
		if(approvedCoupons == null || categories == null){
			flash("warning", Messages.get("error.msg.01"));
			return redirect("/");
		}		
		if (name == null) {
			return ok(index.render( approvedCoupons, categories ));
		} 	
		return ok(index.render( approvedCoupons, categories ));

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
		
			Form<Login> loginForm = new Form<Login>(Login.class);
			if (loginForm.hasGlobalErrors()) {
				Logger.info("Login global error");
				flash("error", Messages.get("login.Error01"));

				return badRequest(Loginpage.render(" "));
			}
			try {
				String mail = loginForm.bindFromRequest().get().email;
				String password = loginForm.bindFromRequest().get().password;
				List<Coupon> approvedCoupons = Coupon.approvedCoupons();
				if (mail.isEmpty() || password.length() < 6) {
					Logger.info("Invalid login form, mail empty or short password");
					flash("error", Messages.get("login.IncorrectPasword"));
					return badRequest(Loginpage.render(" "));
				}
				if (User.verifyLogin(mail, password) == true) {
					User user = User.getUser(mail);
					session().clear();
					session("name", user.username);
					session("email", user.email);
					Logger.info(user.username + " logged in");
					flash("success", loginSuccess + " " + mail);
					return ok(index.render(approvedCoupons,
							Category.all()));

				}
				if (Company.verifyLogin(mail, password) == true) {
					Company company = Company.findByEmail(mail);
					session().clear();
					session("name", company.name);
					session("email", company.email);
					flash("success", loginSuccess + " " + mail);
					Logger.info(company.name + " logged in");
					return ok(indexC.render(company, approvedCoupons));
				}

				flash("error", Messages.get("login.InvalidEmailOrPassword"));
				Logger.info("User tried to login with invalid email or password");
				return badRequest(Loginpage.render(" "));

			} catch (Exception e) {
				flash("error", Messages.get("error.msg.01"));
				Logger.error("Error has occured at login: " + e.getMessage(), e);
				return redirect("/");
			}
	}

	/**
	 * @return renders the loginpage view
	 */
	public static Result loginpage() {
		return ok(Loginpage.render(" "));
	}

	/**
	 * Logs out the User and clears the session
	 * 
	 * @return redirects to index
	 */
	public static Result logout() {
		String name = session("name");
		//Handling exceptions
		if(name == null){
			flash("error.msg.01");
			return redirect("/");
		}
		Logger.info(name + " has logged out");
		session().clear();
		flash("success",Messages.get("logoutSuccess"));
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
				.render(Messages.get("loginToComplete")));
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

	/**
	 * This method is used for sending e-mail.
	 * First, checks if the recaptcha key is filled properly,
	 * if it's not filled properly, or if it's missing, user get flash error message,
	 * else method sends e-mail.
	 * @return holder(Promise of type Result)
	 */
	public static Promise<Result> sendMail() {
		final DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();			
		Promise<Result> holder = WS
				.url("https://www.google.com/recaptcha/api/siteverify")
				.setContentType("application/x-www-form-urlencoded")
				.post(String.format("secret=%s&response=%s",

				Play.application().configuration().getString("recaptchaKey"),
						dynamicForm.get("g-recaptcha-response")))
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
							flash("error", Messages.get("mail.MissingSecretKey"));
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						if (json.findValue("error-codes").toString()
								.equals("invalid-input-secret")) {
							flash("error", Messages.get("mail.InvalidSecretKey"));
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						if (json.findValue("error-codes").toString()
								.equals("missing-input-response")) {
							flash("error", Messages.get("mail.MissingResponseParameter"));
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						if (json.findValue("error-codes").toString()
								.equals("invalid-input-response")) {
							flash("error", Messages.get("mail.MalformedParameter"));
							User currentUser = User.find(name);
							return ok(contact.render(currentUser, submit));
						}

						flash("error", Messages.get("error.captcha"));
						User currentUser = User.find(name);
						return ok(contact.render(currentUser, submit));
					}
				});
		return holder;
		
	}
	
	/**
	 * @return Renders the registration view
	 */
	public static Result signup() {
		return ok(signup.render(new Form<User>(User.class), new Form<Company>(Company.class)));
	}

}
