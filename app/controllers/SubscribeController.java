package controllers;

import helpers.MailHelper;

import java.util.List;

import models.Subscriber;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;

public class SubscribeController extends Controller {
	
	
	public static Result sendNewsletters(){
		List<String> subscribers = Subscriber.getAllSubscribers();
		DynamicForm form = Form.form().bindFromRequest();
		String subject = form.data().get("subject");
		String listOfCoupons = form.data().get("listOfCoupons");
		
		
		MailHelper.sendNewsletter(subscribers,
				subject, 
				Subscriber.getPreparedHTMLContent(listOfCoupons));
		
		
		
		return TODO;		
	}
}
