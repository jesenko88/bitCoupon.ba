package models;

import helpers.HashHelper;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class EmailVerification extends Model {

	@Id
	public String id;

	@Required
	public long userId;

	@Required
	public Date createdOn;

	@Required
	public boolean isVerified;

	/**
	 * Constructor for EmailVerification
	 * @param userId - id of user which e-mail needs to be verified
	 * @param isVerified - is user e-mail verified or not
	 */
	public EmailVerification(long userId, boolean isVerified) {
		this.id = HashHelper.createPassword(userId + new Date().toString())
				.replace('/', '1');
		this.userId = userId;
		this.createdOn = new Date();
		this.isVerified = isVerified;
		if(isVerified == true){
		    User user = User.find(userId);
		    user.status = SuperUser.VERFIED;
		    user.save();
		}
	}

	static Finder<String, EmailVerification> find = new Finder<String, EmailVerification>(
			String.class, EmailVerification.class);

	/**
	 * Add verification e-mail to user and sets it to nonverified
	 * @param userId - user id
	 * @return - id of EmailVerification table
	 */
	public static String addNewRecord(long userId) {
		EmailVerification verify = new EmailVerification(userId, false);
		verify.save();
		return verify.id;
	}
	
	/**
	 * Make a new record by specifying all parameters
	 * @param userId
	 * @param verified
	 */
	public static void makeNewRecord(long userId, boolean verified) {
		EmailVerification verify = new EmailVerification(userId, verified);
		User user = User.find(verify.userId);
		if(verified == true){
		    user.status = SuperUser.VERFIED;
		    user.save();
		}		
		verify.save();
	}

	/**
	 * Sets verification e-mail to verified
	 * @param recordToUpdate - verification e-mail
	 */
	public static void updateRecord(EmailVerification recordToUpdate) {
		recordToUpdate.isVerified = true;
		User user = User.find(recordToUpdate.userId);
		user.status = SuperUser.VERFIED;
		user.save();
		recordToUpdate.save();
	}

	/**
	 * Finds verification email
	 * @param id -  id of EmailVerification table
	 * @return verification mail
	 */
	public static EmailVerification find(String id) {
		return find.byId(id);
	}

	/**
	 * Check if the e-mail is verified
	 * @param userId - id of user which we check
	 * @return true if the user e-mail is verified, else return false
	 */
	public static boolean isEmailVerified(long userId) {
		List<EmailVerification> list = find.where().eq("userId", userId)
				.orderBy("createdOn").findList();

		return list.get(list.size() - 1).isVerified;
	}
}
