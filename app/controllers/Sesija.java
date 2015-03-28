package controllers;
import models.Company;
import models.User;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

/**
 * This class is a session controller. It allows us to get info on currently
 * logged in user and his attributes.
 */
public class Sesija extends Security.Authenticator {

	/**
	 * Returns username from the User that is in the current session
	 * 
	 * @return username String
	 */
	public String getUsername(Context ctx) {
		if (!ctx.session().containsKey("name")) {
			return null;
		}
		long id = Long.parseLong(ctx.session().get("name"));
		User u = User.find(id);
		if (u != null) {
			return u.username;
		}
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		return redirect(routes.Application.index());
	}

	/**
	 * Returns User from an active session or null the session is empty
	 * 
	 * @param ctx
	 * @return User or null
	 */
	public static User getCurrentUser(Context ctx) {
		if (!ctx.session().containsKey("name")) {
			return null;
		}
		String username = ctx.session().get("name");
		User u = User.find(username);
		return u;
	}

	/**
	 * Checks if the user from the current session is Admin
	 * 
	 * @param ctx
	 * @return true or false
	 */
	public static boolean adminCheck(Context ctx) {

		if (getCurrentUser(ctx) == null)
			return false;
		return getCurrentUser(ctx).isAdmin;
	}

	
	public static Company getCurrentCompany(Context ctx) {
		if (!ctx.session().containsKey("name")) {
			return null;
		}
		String name = ctx.session().get("name");
		Company c = Company.find(name);
		return c;
	}
	/**
	 * Returns username from the User that is in the current session
	 * 
	 * @return username String
	 */
	public String getCompanyName(Context ctx) {
		if (!ctx.session().containsKey("name")) {
			return null;
		}
		long id = Long.parseLong(ctx.session().get("name"));
		Company c = Company.findById(id);
		if (c != null) {
			return c.name;
		}
		return null;
	}
	
}
