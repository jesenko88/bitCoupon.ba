package models.questions;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import models.Coupon;
import models.User;
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
	
	@ManyToOne
	public Coupon coupon;

	public String answer;
	
	public Date questionDate;
	
	public Date answerDate;
	
	
	static Finder<Long, Question> find = new Finder<Long, Question>(Long.class, Question.class);
	
	public Question(String question, String answer, Coupon coupon, User user){
		this.question = question;
		this.answer = answer;
		this.coupon = coupon;
		this.user = user;
		this.questionDate = new Date();
	}
	
	public static void create(String question, String answer, Coupon coupon, User user){
		new Question(question, answer, coupon, user).save();
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
	
}
