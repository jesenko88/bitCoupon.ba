package models.comments;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import models.User;
import play.db.ebean.Model;

/**
 * class represents model of report.
 * At this moment message of report is empty.
 * TODO Do modal report, so message can be left and displayed to admin.
 *
 */
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
	
	/**
	 * Method returns HashMap of reports.
	 * Key is comment, value is number of reports.
	 * @return
	 */
	public static HashMap<Comment, Integer> reports(){
		HashMap<Comment, Integer> reportedComments = new HashMap<Comment, Integer>();
		List<Report> all = find.all();
		for(Report report: all){
			if(reportedComments.containsKey(report.comment)){
				int i = reportedComments.get(report.comment) ;
				reportedComments.put(report.comment, i+1);
			}else{
				reportedComments.put(report.comment, 1);
			}
		}		
		return reportedComments;	
	}
	
	/**
	 * Removing all reports on comment.
	 * @param commentId
	 */
	public static void removeCommentReports(long commentId){
		Comment c = Comment.findById(commentId);
		List<Report> reports = findByComment(c);
		for(Report r: reports){
			r.delete();
		}
	}

}
