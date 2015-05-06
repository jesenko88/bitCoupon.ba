/**
 * 
 */
package models.comments;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.Coupon;
import models.User;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

/**
 * Model comment on coupon.
 * Represents Comments on coupon. 
 * All CRUD operations implemented.
 *
 */
@Entity
public class Comment extends Model{

	@Id
	public long id;

	@Required
	@Column(columnDefinition = "TEXT")
	public String comment;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Coupon coupon;

	@OneToMany
	public Report report;
	
	public Date date;
		
	static Finder<Long, Comment> find = new Finder<Long, Comment>(Long.class, Comment.class);
	
	/**
	 * Constructor for Comment
	 * @param comment
	 * @param coupon
	 * @param user
	 */
	public Comment(String comment, Coupon coupon, User user){
		this.comment = comment;
		this.coupon = coupon;
		this.user = user;
		this.date = new Date();
	}
	
	/**
	 * Method which create comment
	 * @param comment
	 * @param coupon
	 * @param user
	 * @return
	 */
	public static long create(String comment, Coupon coupon, User user){
		Comment comm = new Comment(comment, coupon, user);
		comm.save();
		return comm.id;
	}
	
	/**
	 * Method which delete comment from DB
	 * @param id
	 */
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	/**
	 * Method which update comment
	 * @param id
	 * @param newComment
	 */
	public static void update(long id, String newComment){
		Comment comment = find.where().eq("id", id).findUnique();
		comment.comment = newComment;
		comment.update();
	}
	
	/**
	 * This method finds all comments of certain coupon in DB
	 * @param coupon
	 * @return list of comments
	 */
	public static List<Comment> findByCoupon(Coupon coupon){
		List<Comment> comments = find.where().eq("coupon", coupon).findList();
		if (comments == null)
			return new ArrayList<Comment>();
		return comments;
	}
	
	/**
	 * This method finds certain comment in DB
	 * @param id of comment
	 * @return comment
	 */
	public static Comment findById(long id){
		return find.where().eq("id", id).findUnique();
	}
}
