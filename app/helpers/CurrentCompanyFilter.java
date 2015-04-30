package helpers;

import models.Company;
import play.Logger;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

/**
 * This class is a controller filter and ensures that only
 * a logged in company can perform certain actions.
 *
 */
public class CurrentCompanyFilter extends Security.Authenticator {

	public String getUsername(Context ctx) {
		if(!ctx.session().containsKey("email"))
			return null;
		String email = ctx.session().get("email");
		Company c = Company.findByEmail(email);
		if (c != null)
			return c.name;
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		Logger.error("Login To Complete, only companies here.");
		return redirect("/loginToComplete");
	}

}
