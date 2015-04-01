package controllers;

import helpers.CurrentUserFilter;
import helpers.HashHelper;
import java.util.Date;
import models.Company;
import models.SuperUser;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.user.*;

public class SuperUserController extends Controller {

	// @Security.Authenticated(CurrentUserFilter.class)
	public static Result changePass(String email) {
		DynamicForm updateForm = Form.form().bindFromRequest();
		if (updateForm.hasErrors()) {
			return redirect("/updateUser ");
		}
		String oldPass = updateForm.data().get("password");
		String newPass = updateForm.data().get("newPassword");
		String confPass = updateForm.data().get("confirmPassword");
		SuperUser superUser = SuperUser.getSuperUser(email);

		/* if only one password field is filled out */
		if (oldPass.isEmpty() && !newPass.isEmpty() || newPass.isEmpty()
				&& !oldPass.isEmpty()) {
			flash("error", "If you want to change your password,"
					+ " please fill out both fields");
			return TODO;
		}
		/* if there was a input in password fields */
		if (!oldPass.isEmpty() && !newPass.isEmpty()) {
			if (HashHelper.checkPass(oldPass, superUser.password) == false) {
				flash("error", "You're old password is incorrect!");
				return TODO;
			}
			if (newPass.length() < 6) {
				flash("error", "The password must be at least 6 characters");
				return TODO;
			}
			superUser.password = HashHelper.createPassword(newPass);
		}
		if (!newPass.equals(confPass)) {
			flash("error", "Passwords don't match, try again ");
			return TODO;
		}
		User user;
		Company company;
		if (superUser.isUser()) {
			user = superUser.getUser();
			user.updated = new Date();
			user.save();
			flash("success", "Password changed!");
			Logger.info(user.username + " is updated");
			return ok(profile.render(user));
		}
		company = superUser.getCompany();
		company.updated = new Date();
		company.save();
		flash("success", "Password changed!");
		Logger.info(company.name + " is updated");
		return ok(profile.render(company));

	}

}
