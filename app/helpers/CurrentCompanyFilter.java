package helpers;

import models.Company;
import play.Logger;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Context;

/**
 * This class is a controller filter and ensures that only
 * a logged in company can perform certain actions.
 *
 */
public class CurrentCompanyFilter extends Security.Authenticator {

	public String getCompanyName(Context ctx) {
		if(!ctx.session().containsKey("name"))
			return null;
		String name = ctx.session().get("name");
		Company c = Company.find(name);
		if (c != null)
			return c.name;
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		Logger.error("Login To Complete");
		return redirect("/loginToComplete");
	}

}
