package controllers;

import helpers.HashHelper;
import play.*;
import play.api.mvc.Session;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import models.*;

public class UserController extends Controller {

	static String message = "Welcome ";
	static String bitName = "bitCoupon";
	static String name = null;

	static Form<User> newUser = new Form<User>(User.class);
	static Form<Login> login = new Form<Login>(Login.class);

	/**
	 * @return Renders the registration view
	 */
	public static Result signup() {
		return ok(signup.render("Registration", "Username", "Email"));
	}

	/**
	 * Pulls the input form from the three registration fields and creates a new
	 * user in the Database.
	 * 
	 * @return redirects to the index page with welcome.
	 */
	public static Result register() {

		 String username = newUser.bindFromRequest().get().username;
		 String mail = newUser.bindFromRequest().get().email;
		 String password = newUser.bindFromRequest().get().password;
		 String hashPass= HashHelper.createPassword(password);
		 String confPass = newUser.bindFromRequest().field("confirmPassword").value();
		 
		 if( username.length() < 4 || username.equals("Username")){
		 return ok(signup.render(
		 "Enter a username with minimum 4 characters !",null, mail ));
		 }
		 else if ( mail.equals("Email")){
		 return ok(signup.render(
		 "Email required for registration !",username, null ));
		 }
		 else if ( password.length() < 6 ){
		 return ok(signup.render(
		 "Enter a password with minimum 6 characters !",username, mail ));
		 }
		 else if ( !password.equals(confPass)){
			 return ok(signup.render(
					 "Passwords don't match, try again ",username, mail ));
		 }
		
		 /* Creating new user if the username or mail is free for use, and
		 there are no errors */
		
		 else if ( User.verifyRegistration(username, mail) == true){
		 session("name", username);
		 long id = User.createUser(username, mail, hashPass);
		 return redirect("/user/" + id);
		
		 } else {
		 return ok(signup.render("Username or email allready exists!",
		 username, mail ));
		 }
	}
	
	public static Result show(long id){
		
		User u = User.find(id);
		if ( !u.username.equals(session("name"))){
			return redirect("/");
		}
		
		return ok(userIndex.render(message, u.username, null));
		
	}

}
