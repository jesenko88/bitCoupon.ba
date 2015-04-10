package controllersJSON;

import java.util.Date;

import javax.mail.internet.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import helpers.*;
import models.*;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;

public class JSonOperatorGET extends Controller {
	
	
	public static Result profilePage(String username) {
		try {
			User user = User.find(username);
			Company company = Company.find(username);
			if (user != null) {
				return ok(JSonHelper.userToJSon(user));
			} else if (company != null) {
				return ok(JSonHelper.companyToJSon(company));
			}
		} catch (Exception e) {
			Logger.error("error","Profile page failed due null user or company! " + e.getMessage(), e);
		}
		return badRequest(JSonHelper.messageToJSon("erorr",	"An error occured"));
	}
	
	
}
