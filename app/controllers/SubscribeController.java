package controllers;

import helpers.MailHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.Coupon;
import models.Subscriber;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import views.html.user.*;
import views.html.admin.users.*;
import views.html.helper.checkbox;

public class SubscribeController extends Controller {
	
	
	public static Result newsletterPanel(){
		return ok(Newsletter.render(Coupon.approvedCoupons()));
	}
	
	
	/**
	 * Method which creates newsletter from form.
	 * It gets all selected coupons, add them to newsletter and send
	 * newsletter to all registered and not registered subscribers.
	 * TODO make better solution for getting selected checkboxes.
	 * @return
	 */
	public static Result sendNewsletters(){
		List<String> subscribers = Subscriber.getAllSubscribers();
		
		DynamicForm form = Form.form().bindFromRequest();
		String subject = form.data().get("subject");			
		//Getting set of results from form.
		Set<String> formResult = form.data().keySet();
		Iterator<String> it = formResult.iterator();	
		List<Coupon> coupons = new ArrayList<Coupon>();
		
		//If checked coupons are more then 3, returning back.
		if(formResult.size() > 4){
			flash("error", "Max number of coupons for newsletter is 3");
			return redirect("/newsletter");
		}
		
		//Iterating through form results and getting all coupon ids 
		//which are checked.
		while(it.hasNext()){
			String current = it.next();
			if(isNumber(current)){
				Coupon cp = Coupon.find(Long.valueOf(current));
				coupons.add(cp);								
			}
		}
		String commaSeparatedIds = Coupon.commaSeparatedIds(coupons);			
		MailHelper.sendNewsletter(subscribers,
			subject, 
				coupons);
	
		
		
		return TODO;		
	}
	
	/**
	 * Method for checking if string is number.
	 * @param number
	 * @return
	 */
	public static boolean isNumber(String number){
		try{
			Long.parseLong(number);
			return true;
		}catch(Exception e){
			return false;
		}
	}
}
