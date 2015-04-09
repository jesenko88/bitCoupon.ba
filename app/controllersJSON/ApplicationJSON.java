package controllersJSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.JSonHelper;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class ApplicationJSON extends Controller {

	
	public static Result loginJSON() {
			
		JsonNode json = request().body().asJson();
		System.out.println(json.toString());
		String mail = json.findPath("email").textValue();
		String password = json.findPath("password").textValue();
		System.out.println("DEBUG ******** EMAIL *******" + mail);
		
		if (mail == null || password == null) {
			Logger.info("Login error, mail or password is null");
			return badRequest();
		}

		if (mail.isEmpty() || password.length() < 6) {
			Logger.info("Invalid login form, mail empty or short password");
			ObjectNode info = Json.newObject();
			info.put("info", "Invalid login form, mail empty or short password");
			return badRequest(info);
		}
		if (User.verifyLogin(mail, password) == true) {
			User cc = User.getUser(mail);
			Logger.info(cc.username + " logged in");
			session("name", cc.username);
			session("email", cc.email);
			System.out.println("DEBUG ********** LOGIN ");
			return ok(JSonHelper.couponListToJson(Coupon.approvedCoupons() ));
		}
		if (Company.verifyLogin(mail, password) == true) {
			Company cc = Company.findByEmail(mail);
			Logger.info(cc.name + " logged in");
			return ok(JSonHelper.companyToJSon(cc) );
		}
		Logger.info("User tried to login with invalid email or password");
		return badRequest();
	}
	
}
