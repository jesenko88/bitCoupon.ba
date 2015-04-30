package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.google.common.collect.Lists;

/**
 * Entity for the coupon questions and answers
 *
 */
@Entity
public class Question extends Model{

	@Id
	public long id;

	@Required
	@Column(columnDefinition = "TEXT")
	public String question;
	
	@ManyToOne
	public User user;
	
	@OneToOne
	public Company company;
	
	@ManyToOne
	public Coupon coupon;

	@Column(columnDefinition = "TEXT")
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
	
	/**
	 * Method creates a new Question and saves it to the database
	 * @param question String
	 * @param answer String
	 * @param coupon Coupon
	 * @param user User
	 * @return question id long
	 */
	public static long create(String question, String answer, Coupon coupon, User user){
		Question newQuestion = new Question(question, answer, coupon, user);
		newQuestion.save();
		return newQuestion.id;
	}
	
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	/**
	 * Finds all questions for a provided Coupon
	 * @param coupon Coupon
	 * @return List<Question>
	 */
	public static List<Question> findByCoupon(Coupon coupon){
		List<Question> questionList =  find.where().eq("coupon", coupon).findList();
		if(questionList == null)
			return new ArrayList<Question>();
		return Lists.reverse(questionList);
	}
	
	/**
	 * Finds a Question by Id
	 * @param id long
	 * @return Question
	 */
	public static Question findById(long id){
		return find.where().eq("id", id).findUnique();
	}
	
	
	/**
	 * Returns a list of new questions for a company.
	 * It finds all questions that have a false 'newQuestion' attribute
	 * @param id of the Company that owns the questioned coupons
	 * @return List<Question>
	 */
	public static List<Question> newQuestions(long companyId) {
		List<Question> questions = find.where().eq("company", Company.findById(companyId)).findList();
		if (questions == null)
			return new ArrayList<Question>();
		List<Question> newQuestions = new ArrayList<>();
		for (Question question : questions){
			if(question.newQuestion == true){
				newQuestions.add(question);
			}
		}
		questions.clear();
		return newQuestions;
		
	}
	
}
