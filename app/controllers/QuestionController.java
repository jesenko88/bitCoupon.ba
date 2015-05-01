package controllers;

import java.util.Date;

import models.Coupon;
import models.Question;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.coupon.coupontemplate;
import views.html.coupon.questionLine;

/**
 * Question controller class
 * Included methods for add question, answer, delete question, and
 * to show coupon which has a new question
 */
public class QuestionController extends Controller {
	
	private static final String INTERNAL_SERVER_ERROR = "Internal server error";
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");

	
	/**
	 * Method for adding a new question for a coupon
	 * It receives a coupon id, finds that coupon and sets the new question
	 * retrieved from the request
	 * @param coupon id long
	 * @return
	 */
	
	public static Result addQuestion(long couponId) {
		try{
			Coupon coupon = Coupon.find(couponId);
			User user = Sesija.getCurrentUser(ctx());
			DynamicForm form = Form.form().bindFromRequest();
			String question = form.data().get("question");
			if (question.length() > 600){
				flash("error", Messages.get("coupon.question.length"));
				return badRequest("/coupon/" + couponId);
			}
			long id = Question.create(question, "", coupon, user);
			coupon.seller.notifications ++;
			coupon.seller.save();
			return ok(questionLine.render(Question.findById(id), coupon));
		}catch(Exception e){
			Logger.error(e.getMessage(), e);
			flash("error", ERROR_MSG_CLIENT);
			return badRequest("/coupon/" + couponId);
		}
	}
	
	/**
	 * Method for adding a answer for a existing coupon question
	 * It receives a question id, finds that question and sets the answer
	 * retrieved from the request
	 * @param question id long
	 * @return
	 */
	public static Result addAnswer(long questionId) {
			Question question = null;
		try{
			DynamicForm form = Form.form().bindFromRequest();
			question = Question.findById(questionId);
			String answer = form.data().get("answer");
			if (answer.length() > 450){
				flash("error",Messages.get("coupon.question.answer.length"));
				return badRequest("/coupon/" + question.coupon.id);
			}
			question.answer = answer;
			question.answerDate = new Date();
			question.save();
			return redirect("/coupon/" + question.coupon.id);
		}catch(Exception e){
			Logger.error(e.getMessage(), e);
			flash("error", ERROR_MSG_CLIENT);
			return badRequest("/coupon/" + question.coupon.id);
		}
	}
	
	/**
	 * Method for deleting a question by id
	 * @param question id long
	 * @return
	 */
	public static Result deleteQuestion(long questionID){	
		Coupon coupon = Question.findById(questionID).coupon;
		if (coupon != null){
			Question.delete(questionID);
			return redirect("/coupon/" + coupon.id);
		}
		flash("error", ERROR_MSG_CLIENT);
		return redirect("/");
	}
	
	/**
	 * Method for showing a coupon that has a new question
	 * Receives a question id and finds the coupon that holds the question.
	 * Sets the coupon attribute 'newQuestion' to false, so the question is 
	 * not displayed in the notifications any more.
	 * @param question id long
	 * @return
	 */
	public static Result showCoupon(long questionID) {
		try {
			Question currentQuestion = Question.findById(questionID);
			currentQuestion.newQuestion = false;
			currentQuestion.save();
			return ok(coupontemplate.render(currentQuestion.coupon));
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			flash("error",ERROR_MSG_CLIENT);
			return redirect("/");
		}
	}
	

}
