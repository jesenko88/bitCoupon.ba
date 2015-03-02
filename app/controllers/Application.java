package controllers;

import models.*;
import play.*;
import play.data.Form;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {
	
	static String message = "Welcome ";
	static String bitName = "bitCoupon";
	static String name = null;
	
	
	static Form<User> newUser = new Form<User>(User.class);
	static Form<Login> login = new Form<Login>(Login.class);

	
	/**
	 * 
	 * @return redirects to index according to session name
	 */
    public static Result index() {
    	name = session("name");
    	if (name == null){
    		name = "Public user";	
    		return ok(index.render(message, name )); 
    	} else {
    		return ok(index.render(message, name )); 
    	}
    }
    
    
    	/* MOVED TO UserController */
//    /**
//     * @return Renders the registration view
//     */
//    public static Result registration(){ 	
//    	return ok(registration.render(bitName, "Registration"));
//    }
//    
//    /**
//     * Pulls the input form from the three registration fields and creates a new user in the Database.
//     * 
//     * @return redirects to the index page with welcome.
//     */
//    public static Result register(){
//    	String username = newUser.bindFromRequest().get().username;
//    	String mail = newUser.bindFromRequest().get().email;
//    	String password = newUser.bindFromRequest().get().password;    	
//    	session("name", username);
//    	
//    	User u = new User(username, mail, null);
//    	
//    	if ( u.verifyRegistration(u.username, u.email) == true){
//    		
//    	long id = User.createUser(username, mail, password);
//    	  return ok(userIndex.render(message, username ));  
//    	  
//    	} else {
//    		return ok(registration.render(bitName, "Username or mail allready exists!" ));
//    	}
//    	
//    }
//    
//    /**
//     * 
//     * @return renders the loginpage view
//     */
//  public static Result loginpage(){
//    		return ok(Loginpage.render(bitName, "Login to your account"));   	
//    }
//  
//  	/**
//  	 * Pulls the value from two login fields and verifies if the
//  	 * mail exists and the password is valid.
//  	 * @return redirects to index if the verification is ok, or reloads the login page
//  	 * with warning message.
//  	 */
//  public static Result login(){
//	  String mail = login.bindFromRequest().get().email;
//	  String password = login.bindFromRequest().get().password;  
//	  User u = new User(null, mail, password);
//	  session("name", mail);
//	  if ( u.verifyLogin(u.email, u.password) == true ){
//		  return ok(userIndex.render(message, mail )); 
//	  }
//	  
//	  return ok(Loginpage.render(bitName,"Invalid email or password"));
//	  
//  }
//  
//  /**
//   * Clears the session
//   * @return redirects to index
//   */
//  public static Result logout() {
//	  session().clear();
//	    flash("OK!", "You've been logged out");
//	    return redirect(
//	        routes.Application.index()
//	    );
//  }

        
  

}
