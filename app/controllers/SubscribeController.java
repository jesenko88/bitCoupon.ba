package controllers;

import helpers.AdminFilter;
import helpers.MailHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import models.Coupon;
import models.Subscriber;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.*;
import views.html.*;
import views.html.user.*;
import views.html.admin.users.*;
import views.html.helper.checkbox;

public class SubscribeController extends Controller {

	/**
	 * Rendering newsletter panel.
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result newsletterPanel() {
		List<Coupon> coupons = Coupon.approvedCoupons();
		if(coupons == null){
			flash("error", "Error has occured. Please try again.");
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
			if (formResult.size() != 4) {
				flash("warning",
						"Number of selected coupons must be exactly 3.");
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
			MailHelper.sendNewsletter(subscribers, subject, coupons);
			return redirect(refererUrl);
		} catch (Exception e) {
			flash("error", "Error while sending newsletter."
					+ "Please check your logs.");
			Logger.error("Error at sendNewsletters: " + e.getMessage());
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
		String refererUrl = request().getHeader("referer");
		try {
			User u = User.findByEmail(email);
			if (Subscriber.isSubscribed(email)) {
				flash("warning", "That email is already subscribed");
				return redirect("/");
			}

			if (u != null) {
				Subscriber.subscribe(u);
				Logger.debug("User " + u.email + " subscribed.");
			} else {
				Logger.debug("Visitor with email " + email + " subscribed.");
				Subscriber.subscribe(email);
			}
			return redirect(refererUrl);
		} catch (Exception e) {
			flash("error", "Error while subscribing. "
					+ "If you're admin please check your logs.");
			Logger.error("Error at subscribe: " + e.getMessage());
			return redirect(refererUrl);
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
			Subscriber s = Subscriber.findByToken(token);
			Subscriber.unsubscribe(s);
			flash("success", "You have been unsubscribed.");
			return redirect("/");
		} catch (Exception e) {
			flash("error", "Error while unsubscribing. "
					+ "If you're admin please check your logs.");
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
			Subscriber s = Subscriber.find.byId(id);
			Subscriber.unsubscribe(s);
			flash("success", "Subscriber has been removed");
			return redirect("/subscribers");
		} catch (Exception e) {
			flash("error", "Error while deleting subscriber. "
					+ "Please check your logs.");
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
