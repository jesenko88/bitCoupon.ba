package helpers;

import play.Logger;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Context;

public class CompanyFilters extends Security.Authenticator {

	@Override
	public String getUsername(Context ctx) {
		return null;
	}
	
	@Override
	public Result onUnauthorized(Context ctx) {
		Logger.error("Login To Complete");
		return TODO;
	}
}
