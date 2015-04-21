package controllers;

import java.util.Date;

import models.Coupon;
import models.TransactionCP;
import models.User;
import models.questions.Question;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.coupon.coupontemplate;

public class QuestionController extends Controller {
	

	public static Result addQuestion(long couponId) {
		
		DynamicForm form = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String question = form.data().get("question");
		long id = Question.create(question, "", coupon, user);
		CouponController.notifications++;
		return redirect("/coupon/" + coupon.id);
	}
	
	public static Result addAnswer(long questionId) {
		
		DynamicForm form = Form.form().bindFromRequest();
		String answer = form.data().get("answer");
		System.out.println("DEBUGGG ANSWEER" + answer);
		Question question = Question.findById(questionId);
		question.answer = answer;
		question.answerDate = new Date();
		question.save();
		return redirect("/coupon/" + question.coupon.id);
	}
	
	
	public static Result deleteQuestion(long questionID){	
		Coupon coupon = Question.findById(questionID).coupon;
		Question.delete(questionID);
		return redirect("/coupon/" + coupon.id);
	}
	
	
	public static Result showCoupon(long questionID) {
		Question currentQuestion = Question.findById(questionID);
		Coupon coupon = currentQuestion.coupon;
		currentQuestion.newQuestion = false;
		currentQuestion.save();
		if (coupon == null) {
			Logger.error("error", "Coupon null at showCoupon()");
			flash("error", "Ooops, error has occured.");
			return redirect("/");
		}
		return ok(coupontemplate.render(currentQuestion.coupon));
	}
	

}
