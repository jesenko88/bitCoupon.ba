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

/**
 * Controller for comments.
 * Available methods for commenting coupon,
 * reporting coupon, and removing coupon and report.
 *
 */
public class CommentController extends Controller {
	
	/**
	 * Method for creating comment.
	 * @param couponId
	 * @return
	 */
	public static Result comment(long couponId){
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String comment = dynamicForm.data().get("comment");
		String rate = dynamicForm.data().get("rate");
		if(rate == null) {
			rate = "0";
		}
		Integer intRate = Integer.parseInt(rate);
		Comment.create(comment, coupon, user, intRate);	
		return redirect("/coupon/" +couponId);	
	}
	
	/**
	 * Method for reporting comment.
	 * TODO ajax on this method.
	 * @param commentId
	 * @return
	 */
	public static Result report(long commentId){
		Comment comment = Comment.findById(commentId);
		User user = Sesija.getCurrentUser(ctx());
		Report.create("", comment, user);		
		return CouponController.showCoupon(comment.coupon.id);	
	}
	
	public static Result report(String id){
		Logger.debug("IN STRING REPORT WITH ID " +id);
		long commentId = Long.valueOf(id);
		Comment comment = Comment.findById(commentId);
		User user = Sesija.getCurrentUser(ctx());
		Report.create("", comment, user);		
		return CouponController.showCoupon(comment.coupon.id);	
	}
	
	/**
	 * Method for removing comment.
	 * First of all removing all reports on comment.
	 * @param commentId
	 * @return
	 */
	public static Result removeComment(long commentId){		
		Report.removeCommentReports(commentId);
		Comment.delete(commentId);
		long userId = Sesija.getCurrent(ctx()).id;
		return UserController.controlPanel(userId);
	}
	
	/**
	 * Method for removing reports.
	 * All reports on comment will be removed.
	 * @param commentId
	 * @return
	 */
	public static Result removeReport(long commentId){
		Report.removeCommentReports(commentId);
		long userId = Sesija.getCurrent(ctx()).id;
		return UserController.controlPanel(userId);
	}
	
	

}
