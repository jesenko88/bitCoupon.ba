package controllers;

import helpers.AdminFilter;
import helpers.MailHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.Coupon;
import models.Subscriber;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.users.Newsletter;
import views.html.admin.users.subscribers;

public class SubscribeController extends Controller {

	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");
	
	/**
	 * Rendering newsletter panel.
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result newsletterPanel() {
		List<Coupon> coupons = Coupon.approvedCoupons();
		if(coupons == null){
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("List is empty at newsletterPanel.");
			return redirect("/");
		}
		return ok(Newsletter.render(coupons));

	}

	/**
	 * Rendering subscribers page for admin. Contains list of subscribers.
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result subscribers() {
		List<Subscriber> allSubscribers = Subscriber.find.all();
		if(allSubscribers == null)
			allSubscribers = new ArrayList<Subscriber>();
		
		return ok(subscribers.render(allSubscribers));
	}

	/**
	 * Method which creates newsletter from form. It gets all selected coupons,
	 * add them to newsletter and send newsletter to all registered and not
	 * registered subscribers. TODO make better solution for getting selected
	 * checkboxes.
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result sendNewsletters() {
		String refererUrl = request().getHeader("referer");
		try {
			List<String> subscribers = Subscriber.getAllSubscribers();
			DynamicForm form = Form.form().bindFromRequest();
			String subject = form.data().get("subject");
			// Getting set of results from form.
			Set<String> formResult = form.data().keySet();
			Iterator<String> it = formResult.iterator();
			List<Coupon> coupons = new ArrayList<Coupon>();

			// If checked coupons are more then 3, returning back.
			if (formResult.size() != 5) {
				flash("warning", Messages.get("coupon.newsletter.maxselected"));
				return redirect("/newsletter");
			}

			// Iterating through form results and getting all coupon ids
			// which are checked.
			while (it.hasNext()) {
				String current = it.next();
				if (isNumber(current)) {
					Coupon cp = Coupon.find(Long.valueOf(current));
					coupons.add(cp);
				}
			}
			String message = form.data().get("message");
			if(message.contains("<script>")){
				flash("warning", Messages.get("scripts.restriction"));
				return redirect("/newsletter");
			}
			MailHelper.sendNewsletter(subscribers, subject, coupons, message);
			flash("success", Messages.get("subscribe.newsletter.sent"));
			return redirect(refererUrl);
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at sendNewsletters: " + e.getMessage(),e );
			return redirect(refererUrl);
		}
	}

	/**
	 * Method for subscribing already registered user or non registered visitor
	 * of our site.
	 * 
	 * @param email
	 * @return
	 */
	public static Result subscribe(String email) {		
		try {
			User user = User.findByEmail(email);
			if (Subscriber.isSubscribed(email)) {
				flash("warning", Messages.get("subscribe.allreadySubscribed"));
				return redirect("/");
			}

			if (user != null) {
				Subscriber.subscribe(user);
				Logger.debug("User " + user.email + " subscribed.");
			} else {
				Logger.debug("Visitor with email " + email + " subscribed.");
				flash("success", Messages.get("subscribe.confirmation") + " " + email);
				Subscriber.subscribe(email);
			}
			return redirect("/");
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at subscribe: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Method for unsubscribing user. For security reason it uses unique token.
	 * 
	 * @param token
	 * @return
	 */
	public static Result unsubscribe(String token) {
		try {
			Subscriber subscriber = Subscriber.findByToken(token);
			Subscriber.unsubscribe(subscriber);
			flash("success", Messages.get("subscribe.unsubscribe"));
			return redirect("/");
		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at unsubscribe: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Method for admin to remove subscriber.
	 * 
	 * @param id
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deleteSubscriber(long id) {
		try {
			Subscriber subscriber = Subscriber.find.byId(id);
			Subscriber.unsubscribe(subscriber);
			flash("success", Messages.get("subscribe.removeSubscribe"));
			return redirect("/subscribers");
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at deleteSubscriber: " + e.getMessage());
			return redirect("/subscribers");
		}
	}

	/**
	 * Method for checking if string is number.
	 * 
	 * @param number
	 * @return
	 */
	private static boolean isNumber(String number) {
		try {
			Long.parseLong(number);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
