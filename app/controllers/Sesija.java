package controllers;
import models.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

/*
 * 
 */
public class Sesija extends Security.Authenticator {
	
	/**
	 * Returns username from the User that is in the
	 * current session
	 * @return username String
	 */
	public String getUsername(Context ctx){
		if ( !ctx.session().containsKey("user_id") ){
			return null;
		}
		long id = Long.parseLong(ctx.session().get("user_id"));
		User u = User.find(id);
		if ( u != null){
			return u.username;
		}
		return null;
	}
	
	@Override
	public Result onUnauthorized(Context ctx){
		return redirect(routes.Application.index());
	}
	
	/**
	 * Returns User from an active session
	 * or null the session is empty
	 * @param ctx
	 * @return User or null
	 */
	public static User getCurrentUser(Context ctx){
		if ( !ctx.session().containsKey("name") ){
			return null;
		}
		String username =ctx.session().get("name");
		User u = User.find(username);
		return u;
	}
	
	/**
	 * Checks if the user from the current session is Admin
	 * @param ctx
	 * @return true or false
	 */
	public static boolean adminCheck(Context ctx) {

		if(getCurrentUser(ctx) == null)
			return false;
		return getCurrentUser(ctx).isAdmin;
	}
	
}
