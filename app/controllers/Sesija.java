package controllers;
import models.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

/*
 * 
 */
public class Sesija extends Security.Authenticator {
	
	public String getUsername(Context ctx){
		if ( !ctx.session().containsKey("user_id") ){
			return null;
		}
		long id = Long.parseLong(ctx.session().get("user_id"));
		User u = User.find(id);
		if ( u != null){
			return u.email;
		}
		return null;
	}
	
	@Override
	public Result onUnauthorized(Context ctx){
		return redirect(routes.Application.index());
	}
	
	public static User getCurrentUser(Context ctx){
		if ( !ctx.session().containsKey("name") ){
			return null;
		}
		String mail =ctx.session().get("name");
		User u = User.find(mail);
		return u;
	}
	
	public static boolean adminCheck(Context ctx) {
//		if ( !ctx.session().containsKey("isAdmin") ){
//			return false;
//		}
//		boolean isAdmin = Boolean.parseBoolean(ctx.session().get("user_isAdmin"));
		if(getCurrentUser(ctx) == null)
			return false;
		return getCurrentUser(ctx).isAdmin;
	}
	
}
