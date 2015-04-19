package controllers;

import java.util.List;

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
		return CouponController.showCoupon(comment.coupon.id);	
	}
	
	public static Result removeComment(long commentId){	
		Comment c = Comment.findById(commentId);
		Report.removeCommentReports(commentId);
		Comment.delete(commentId);
		long userId = Sesija.getCurrent(ctx()).id;
		return UserController.controlPanel(userId);
	}
	
	public static Result removeReport(long commentId){
		Report.removeCommentReports(commentId);
		long userId = Sesija.getCurrent(ctx()).id;
		return UserController.controlPanel(userId);
	}
	
	

}
