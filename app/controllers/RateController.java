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
	 * @param couponId
	 * @return
	 */
	public static Result rating(long couponId){
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String rate = dynamicForm.data().get("rate");
		if(rate == null) {
			rate = "5";
		}
		double doubleRate = Double.parseDouble(rate);
		if(doubleRate < 0)
			doubleRate = 0;
		if(doubleRate > 5)
			doubleRate = 5;
		Rate.create(doubleRate, user, coupon);	
		return redirect("/coupon/" +couponId);	
	}

}
