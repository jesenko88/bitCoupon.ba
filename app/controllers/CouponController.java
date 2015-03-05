package controllers;

import java.util.Date;

import models.Coupon;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Loginpage;
import views.html.coupontemplate;
import views.html.userIndex;

public class CouponController extends Controller {

	static Form<Coupon> couponForm = new Form<Coupon>(Coupon.class);

	/**
	 * Shows view for adding coupon
	 * 
	 * @return redirect to create coupon view
	 */
	public static Result createCouponView() {
		// TODO
		return redirect("/");// todo//ok(userIndex.render("Create your coupon",
								// couponForm));
	}

	/**
	 * First check if coupon form have errors. If have, return us to create
	 * coupon view, else creates new coupon.
	 * 
	 * @return redirect to create coupon view
	 */
	public static Result addCoupon() {

		if (couponForm.hasErrors()) {
			return redirect("/");// todo
		}
		
		//TODO handle invalid inputs
		
		Coupon newc = couponForm.bindFromRequest().get();
		long couponID = Coupon.createCoupon(newc.name, newc.price,
				newc.dateCreated, newc.dateExpire, newc.picture, newc.category,
				newc.description);

		return ok(userIndex.render(null, null, "Coupon \"" + newc.name
				+ "\" added"));
	}
	
	
	

	/**
	 * Finds certain coupon
	 * 
	 * @return
	 */
	public static Result find() {
		return TODO;
	}

	public static Result viewCoupon(int id) {
		Coupon coupon = Coupon.find(id);
		return redirect("/");// todo//ok(viewCoupon.render(coupon, couponForm));
	}
	
	public static Result showCoupon(long id) {
		Coupon current = Coupon.find(id);
		
		return ok(coupontemplate.render(session("name"), current));
		
	}

}
