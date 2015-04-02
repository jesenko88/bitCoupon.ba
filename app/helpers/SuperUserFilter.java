package helpers;

import models.Company;
import models.SuperUser;
import play.Logger;
import play.mvc.Result;
import play.mvc.Http.Context;
import play.mvc.Security;

public class SuperUserFilter extends Security.Authenticator{
	
	
	public String getUsername(Context ctx) {
		if(!ctx.session().containsKey("email"))
			return null;
		String email = ctx.session().get("email");
		SuperUser currentUser = SuperUser.getSuperUser(email);
		if (currentUser != null)
			return currentUser.email;
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		Logger.error("You dont have access to this page");
		return redirect("/loginToComplete");
	}

}
