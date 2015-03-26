package helpers;

import models.User;
import play.Logger;
import play.mvc.Result;
import play.mvc.Http.Context;
import play.mvc.Security;

/**
 * This class is a controller filter and ensures that only a user with ADMIN privilegies
 * can perform certain actions.
 * @author Haris
 *
 */
public class AdminFilter extends Security.Authenticator {

	@Override
	public String getUsername(Context ctx) {
		if (!ctx.session().containsKey("name"))
			return null;
		String username = ctx.session().get("name");
		User u = User.find(username);
		if (u != null) {
			if (u.isAdmin == true) {
				return u.username;
			} else
				return null;
		}
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		Logger.error("Login To Complete");
		return redirect("/loginToComplete");
	}

}