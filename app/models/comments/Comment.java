/**
 * 
 */
package models.comments;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import models.*;
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
	
	public Comment(String comment, Coupon coupon, User user){
		this.comment = comment;
		this.coupon = coupon;
		this.user = user;
		this.date = new Date();
	}
	
	public static long create(String comment, Coupon coupon, User user){
		Comment comm = new Comment(comment, coupon, user);
		comm.save();
		return comm.id;
	}
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	public static void update(long id, String newComment){
		Comment comment = find.where().eq("id", id).findUnique();
		comment.comment = newComment;
		comment.update();
	}
	
	public static List<Comment> findByCoupon(Coupon coupon){
		return find.where().eq("coupon", coupon).findList();
	}
	
	public static Comment findById(long id){
		return find.where().eq("id", id).findUnique();
	}
}
