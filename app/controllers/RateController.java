package controllers;

import java.util.List;

import models.Coupon;
import models.Rate;
import models.TransactionCP;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class RateController extends Controller {

	/**
	 * Method for rating.
	 * 
	 * @param couponId
	 * @return
	 */
	public static Result rating(long couponId) {
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String rate = dynamicForm.data().get("rate");
		if (rate == null) {
			rate = "5";
		}
		double doubleRate = Double.parseDouble(rate);
		if (doubleRate < 0)
			doubleRate = 0;
		if (doubleRate > 5)
			doubleRate = 5;
		Rate.create(doubleRate, user, coupon);
		return redirect("/coupon/" + couponId);
	}

	/**
	 * Method checks if the certain user can rate certain coupon
	 * @param userId - id of user
	 * @param couponId - id of coupon
	 * @return true if the user can rate, else return false
	 */
	public static boolean canRate(long userId, long couponId) {
		Coupon coupon = Coupon.find(couponId);
		if(Sesija.getCurrentUser(ctx()) != null  
		&& Rate.alreadyRate(Sesija.getCurrentUser(ctx()).id, couponId) == false
		&& TransactionCP.allBoughtCoupons(Sesija.getCurrentUser(ctx()).id).contains(coupon))
			return true;
		if(Sesija.adminCheck(ctx()) == true && Rate.alreadyRate(Sesija.getCurrentUser(ctx()).id, couponId) == false)
			return true;
		else
		return false;
	}
}
