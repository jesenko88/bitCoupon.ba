package models.comments;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.Company;
import models.Question;
import models.User;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class CommentCompany extends Model {

	@Id
	public long id;

	@Required
	@Column(columnDefinition = "TEXT")
	public String comment;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Company company;

	@OneToMany
	public Report report;
	
	public Date date;
	
	public boolean newComment;
		
	static Finder<Long, CommentCompany> find = new Finder<Long, CommentCompany>(Long.class, CommentCompany.class);
	
	public CommentCompany(String comment, Company coupon, User user){
		this.comment = comment;
		this.company = coupon;
		this.user = user;
		this.date = new Date();
		this.newComment = true;
	}
	
	public static long create(String comment, Company coupon, User user){
		CommentCompany comm = new CommentCompany(comment, coupon, user);
		comm.save();
		return comm.id;
	}
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	public static void update(long id, String newComment){
		CommentCompany comment = find.where().eq("id", id).findUnique();
		comment.comment = newComment;
		comment.update();
	}
	
	public static List<CommentCompany> findByCompany(Company company){
		List<CommentCompany> comments = find.where().eq("company", company).findList();
		if (comments == null)
			return new ArrayList<CommentCompany>();
		return comments;
	}
	
	public static CommentCompany findById(long id){
		return find.where().eq("id", id).findUnique();
	}

	/**
	 * Returns a list of new comments for a company.
	 * It finds all comments that have a false 'newComment' attribute
	 * @param id of the Company that is commented
	 * @return List<CommentCompany>
	 */
/*	public static List<CommentCompany> newProfileComments(long companyId) {
		List<CommentCompany> questions = find.where().eq("company", Company.findById(companyId)).findList();
		if (questions == null)
			return new ArrayList<CommentCompany>();
		List<CommentCompany> newQuestions = new ArrayList<>();
		for (CommentCompany question : questions){
			if(question.newComment == true){
				newQuestions.add(question);
			}
		}
		questions.clear();
		return newQuestions;
		
	}
*/
	
}
