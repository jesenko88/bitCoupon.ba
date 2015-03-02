package models;

import helpers.HashHelper;

import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;


/**
 * 
 * Entity class for the User.
 * Creates a table in the database with all of
 * the properties
 *
 */

@Entity
public class User extends Model {
	
	@Id
	public long id;
	
	@Required
	public String username;
	
	@Email
	public String email;

	@Required
	public String password;

	static Finder<Long, User> find = new Finder<Long,User>(
			Long.class, User.class);

	public User(String username, String email, String password){
		this.username = username;
		this.email = email;
		this.password = password;
	}

	
	/**
	 * Creates a new User and saves it to the database 
	 * @param username String
	 * @param email String
	 * @param password String
	 * @return the id of the new user (long)
	 */
	public static long createUser(String username,String email, String password){
		User newUser = new User(username, email, password);
		newUser.save();
		return newUser.id;
	}
	
	
	public static List<User> all(String username){
		return find.where().eq("username", username).findList();
	}
	
	/**
	 * Login verification
	 * Verifies if the email and password exists by checking in the database
	 * @param mail String
	 * @param password String
	 * @return boolean true or false
	 */
	public static boolean verifyLogin(String mail, String password){		
		try{
		User user = find.where().eq("email", mail).findUnique();
			return HashHelper.checkPass(password, user.password);
	
		} catch (NullPointerException e){
			return false;
		}
		
	}
	
	/**
	 * Checks if there already exists a user with given username or email,
	 * and blocks registration if does.
	 * @param username String
	 * @param email  String
	 * @return boolean true or false
	 */
	public static boolean verifyRegistration(String username, String email){
		List<User> usname = find.where().eq("username", username).findList();
		List<User> mail = find.where().eq("email", email).findList();
		if ( usname.isEmpty() && mail.isEmpty() ){
			return true;
			
		} else return false;
			
	}
	
	
	/* 
	 * TODO
	 * return a username by given mail  
	 */
//	public static List<User> findByMail(String mail){
//		User ll = find.where().eq("email", mail).
//		
//		return find.where().eq("email", mail).findList();
//	}
	
	
	/*
	 * Delete user by id
	 */
	public static void delete(long id){
		find.byId(id).delete();
	}

	/*
	 * Find user by ID
	 */
	public static User find(long id) {
		return find.byId(id);
	}

}
