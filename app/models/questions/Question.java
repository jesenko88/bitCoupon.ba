package models.questions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

import models.Company;
import models.Coupon;
import models.User;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;


@Entity
public class Question extends Model{

	@Id
	public long id;

	@Required
	public String question;
	
	@ManyToOne
	public User user;
	
	@OneToOne
	public Company company;
	
	@ManyToOne
	public Coupon coupon;

	public String answer;
	
	public Date questionDate;
	
	public Date answerDate;
	
	public boolean newQuestion;
	
	
	static Finder<Long, Question> find = new Finder<Long, Question>(Long.class, Question.class);
	
	public Question(String question, String answer, Coupon coupon, User user){
		this.question = question;
		this.answer = answer;
		this.coupon = coupon;
		this.company = coupon.seller;
		this.user = user;
		this.questionDate = new Date();
		this.newQuestion = true;
	}
	
	public static long create(String question, String answer, Coupon coupon, User user){
		Question newQuestion = new Question(question, answer, coupon, user);
		newQuestion.save();
		return newQuestion.id;
	}
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	
	public static List<Question> findByCoupon(Coupon c){
		return find.where().eq("coupon", c).findList();
	}
	
	public static Question findById(long id){
		return find.where().eq("id", id).findUnique();
	}
	
	
	public static List<Question> allFromCompany(long id) {
		List<Question> questions = find.where().eq("company", Company.findById(id)).findList();
		if (questions == null)
			return new ArrayList<Question>();
		List<Question> newQuestions = new ArrayList<>();
		for (Question question : questions){
			if(question.newQuestion == true){
				newQuestions.add(question);
//				question.newQuestion = false;
//				question.save();
			}
		}
		questions.clear();
		return newQuestions;
		
	}
	

	
	
}
