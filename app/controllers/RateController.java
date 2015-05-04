package controllers;

import java.util.List;

import models.Company;
import models.Coupon;
import models.Rate;
import models.TransactionCP;
import models.User;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class RateController extends Controller {

	/**
	 * Method for rating coupon.
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
	 * Method for rating company.
	 * 
	 * @param couponId
	 * @return
	 */
	public static Result ratingCompany(long companyId) {
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Company company = Company.findById(companyId);
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
		Rate.create(doubleRate, user, company);
		return redirect("/companyProfile/" + companyId);
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
		if(Sesija.getCurrentUser(ctx()) != null && Sesija.adminCheck(ctx()) == true 
				&& Rate.alreadyRate(Sesija.getCurrentUser(ctx()).id, couponId) == false 
				&& TransactionCP.allBoughtCoupons(Sesija.getCurrentUser(ctx()).id).contains(coupon))
			return true;
		else
		return false;
	}
	
	/**
	 * Method checks if the certain user can rate certain company
	 * @param userId - id of user
	 * @param companyId - id of company
	 * @return true if the user can rate, else return false
	 */
	public static boolean canRateCompany(long userId, long companyId) {
		List<Coupon> allFromCompany = Coupon.ownedCoupons(companyId);
		List<Coupon> allFromBuyer = TransactionCP.allBoughtCoupons(Sesija.getCurrentUser(ctx()).id);
		if(Sesija.getCurrentUser(ctx()) != null) {
		for(int i = 0; i < allFromBuyer.size(); i++) {
				if(allFromCompany.contains(allFromBuyer.get(i)) && Rate.alreadyRateCompany(Sesija.getCurrentUser(ctx()).id, companyId) == false) {
					return true;
				}
			}
		}
		if(Sesija.getCurrentUser(ctx()) != null && Sesija.adminCheck(ctx()) == true 
				&& Rate.alreadyRateCompany(Sesija.getCurrentUser(ctx()).id, companyId) == false)
			return true;
		else
		return false;
	}
}
