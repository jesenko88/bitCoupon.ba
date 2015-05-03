package controllers;

import helpers.CurrentUserFilter;
import models.Company;
import models.Coupon;
import models.User;
import models.comments.Comment;
import models.comments.CommentCompany;
import models.comments.Report;
import models.comments.ReportCommentCompany;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.coupon.commentLine;
import views.html.coupon.commentCompanyLine;

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
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result comment(long couponId){
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String comment = dynamicForm.data().get("comment");
		if (comment.length() > 1200){
			flash("error",Messages.get("comment.tooLong"));
			return redirect("/coupon/" + couponId);
		}
		long id = Comment.create(comment, coupon, user);	
		return ok(commentLine.render(Comment.findById(id), coupon));
	}
	
	/**
	 * Method for reporting comment.
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
	
	
	
	/**
	 * Method for creating comment.
	 * @param companyId
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result commentCompany(long companyId){
		DynamicForm dynamicForm = Form.form().bindFromRequest();
		Company company = Company.findById(companyId);
		User user = Sesija.getCurrentUser(ctx());
		String comment = dynamicForm.data().get("comment");
		if (comment.length() > 1200){
			flash("error",Messages.get("comment.tooLong"));
			return redirect("/companyProfile/" + companyId);
		}
		long id = CommentCompany.create(comment, company, user);	
		company.notifications++;
		company.save();
		return ok(commentCompanyLine.render(CommentCompany.findById(id)));
	}
	

	
	public static Result reportCommentCompany(String id){
		long commentId = Long.valueOf(id);
		CommentCompany comment = CommentCompany.findById(commentId);
		User user = Sesija.getCurrentUser(ctx());
		ReportCommentCompany.create("", comment, user);		
		return TODO;
	}
	
	/**
	 * Method for removing comment.
	 * First of all removing all reports on comment.
	 * @param commentId
	 * @return
	 */
	public static Result removeCommentCompany(long commentId){		
		ReportCommentCompany.removeCommentReports(commentId);
		CommentCompany.delete(commentId);
		long userId = Sesija.getCurrent(ctx()).id;
		return UserController.controlPanel(userId);
	}
	
	/**
	 * Method for removing reports.
	 * All reports on comment will be removed.
	 * @param commentId
	 * @return
	 */
	public static Result removeReportCommentCompany(long commentId){
		ReportCommentCompany.removeCommentReports(commentId);
		long userId = Sesija.getCurrent(ctx()).id;
		return UserController.controlPanel(userId);
	}	

}
