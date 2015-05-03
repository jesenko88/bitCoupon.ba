package models.comments;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import models.User;
import play.db.ebean.Model;

@Entity
public class ReportCommentCompany extends Model {

	@Id
	public long id;
	
	public String message;
	
	@ManyToOne
	public CommentCompany comment;
	
	@ManyToOne
	public User user;
	
	static Finder<Long, ReportCommentCompany> find = new Finder<Long, ReportCommentCompany>(Long.class, ReportCommentCompany.class);
	
	public ReportCommentCompany(String message, CommentCompany comment, User user){
		this.message = message;
		this.comment = comment;
		this.user = user;
	}	
	
	public static long create(String message, CommentCompany comment, User user){
		ReportCommentCompany report = new ReportCommentCompany(message, comment, user);
		report.save();
		return report.id;
	}
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	public static ReportCommentCompany findById(long id){
		return find.where().eq("id", id).findUnique();
	}
	
	public static List<ReportCommentCompany> findByComment(CommentCompany comment){
		return find.where().eq("comment", comment).findList();
	}
	
	public static List<ReportCommentCompany> findByUser(User user){
		return find.where().eq("user", user).findList();				
	}
	
	/**
	 * Method returns HashMap of reports.
	 * Key is comment, value is number of reports.
	 * @return
	 */
	public static HashMap<CommentCompany, Integer> reports(){
		HashMap<CommentCompany, Integer> reportedComments = new HashMap<CommentCompany, Integer>();
		List<ReportCommentCompany> all = find.all();
		for(ReportCommentCompany report: all){
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
		CommentCompany comment = CommentCompany.findById(commentId);
		List<ReportCommentCompany> reports = findByComment(comment);
		for(ReportCommentCompany r: reports){
			r.delete();
		}
	}
	
}
