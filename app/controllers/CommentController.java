package controllers;

import models.Coupon;
import models.User;
import models.comments.Comment;
import models.comments.Report;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class CommentController extends Controller {
	
	public static Result comment(long couponId){
		DynamicForm df = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String comment = df.data().get("comment");
		Comment.create(comment, coupon, user);
		Logger.debug("CREATED COMMENT");
		return CouponController.showCoupon(couponId);		
	}
	
	public static Result report(long commentId){
		Comment comment = Comment.findById(commentId);
		User user = Sesija.getCurrentUser(ctx());
		Report.create("", comment, user);
		String referedUrl = request().getHeader("referer");
		return redirect(referedUrl);
	}
	
	

}
