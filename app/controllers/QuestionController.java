package controllers;

import models.Coupon;
import models.User;
import models.questions.Question;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class QuestionController extends Controller {

	public static Result addQuestion(long couponId) {
		
		DynamicForm form = Form.form().bindFromRequest();
		Coupon coupon = Coupon.find(couponId);
		User user = Sesija.getCurrentUser(ctx());
		String question = form.data().get("question");
		Question.create(question,"", coupon, user);
		return redirect("/coupon/" + coupon.id);
	}
	
	public static Result addAnswer(long questionId) {
		
		DynamicForm form = Form.form().bindFromRequest();
		String answer = form.data().get("answer");
		System.out.println("DEBUGGG ANSWEER" + answer);
		Question question = Question.findById(questionId);
		question.answer = answer;
		question.save();
		return redirect("/coupon/" + question.coupon.id);
	}
	
	
	
	

}
