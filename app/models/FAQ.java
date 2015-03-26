package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

/**
 * Entity class for FAQ
 *
 */

@Entity
public class FAQ extends Model {
	
	@Id
	public int id;
	
	@Required
	public String question;
	
	@Required
	@Column(columnDefinition = "TEXT")
	public String answer;
	
	static Finder<Integer, FAQ> find = new Finder<Integer, FAQ>(Integer.class, FAQ.class);
	
	/* constructor */
	public FAQ(String question, String answer){
		this.question = question;
		this.answer = answer;
	}
	
	/**
	 * Creates a new FAQ and saves it to the database
	 * @param question String
	 * @param answer String
	 * @return id of the FAQ as int
	 */
	public static int createFAQ(String question, String answer){
		FAQ newFaq = new FAQ(question, answer);
		newFaq.save();
		return newFaq.id;
	}
	
	/**
	 * 
	 * @return all FAQ-s ass List<FAQ>
	 */
	public static List<FAQ> all(){
		List<FAQ> faqs = find.findList();
		return faqs;
	}
	
	/**
	 * Checks if a FAQ exists
	 * @param question String
	 * @return true or false
	 */
	public static boolean checkByQuestion(String question){
		return find.where().eq("question", question).findUnique() != null;
	}
	
	/**
	 * Find FAQ by id
	 * @param id int
	 * @return FAQ
	 */
	public static FAQ find(int id){
		return find.byId(id);
	}
	
	/**
	 * Update FAQ
	 * @param faq
	 */
	public static void update(FAQ faq){
		faq.save();
	}
	
	/**
	 * Delete FAQ 
	 * @param id int
	 */
	public static void delete(int id){
		find.byId(id).delete();
	}
	
}
