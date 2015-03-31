package helpers;

import models.User;
import controllers.routes;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Context;
/**
 * This class is a controller filter and ensures that only
 * a logged in user can perform certain actions.
 * @author Haris
 *
 */
public class CurrentUserFilter extends Security.Authenticator {

	@Override
	public String getUsername(Context ctx) {
		if(!ctx.session().containsKey("name"))
			return null;
		String username = ctx.session().get("name");
		User u = User.find(username);
		if (u != null)
			return u.username;
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		Logger.error("Login To Complete, only users allowed to change this pw.");
		return redirect("/loginToComplete");
	}

	
}
