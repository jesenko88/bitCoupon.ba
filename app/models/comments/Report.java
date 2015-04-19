package models.comments;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import models.User;
import play.db.ebean.Model;

@Entity
public class Report extends Model {
	
	@Id
	public long id;
	
	public String message;
	
	@ManyToOne
	public Comment comment;
	
	@ManyToOne
	public User user;
	
	static Finder<Long, Report> find = new Finder<Long, Report>(Long.class, Report.class);
	
	public Report(String message, Comment comment, User user){
		this.message = message;
		this.comment = comment;
		this.user = user;
	}	
	
	public static void create(String message, Comment comment, User user){
		new Report(message, comment, user).save();
	}
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	public static List<Report> findByComment(Comment comment){
		return find.where().eq("comment", comment).findList();
	}
	
	public static List<Report> findByUser(User user){
		return find.where().eq("user", user).findList();				
	}

}
