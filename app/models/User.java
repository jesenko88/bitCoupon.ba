package models;

import helpers.HashHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.persistence.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

/**
 * 
 * Entity class for the User. Creates a table in the database with all of the
 * properties
 *
 */

@Entity
public class User extends SuperUser {

	@Required
	public String username;

	public boolean isAdmin;
	
	public Date created;
	
	public Date updated;
	
	public String profilePicture;
	

	@OneToMany(mappedBy="seller",cascade=CascadeType.ALL)
	public List<Coupon> coupons;
	
	@OneToMany(mappedBy="buyer", cascade=CascadeType.ALL)
	public List<TransactionCP> bought_coupons;
	
	private static Finder<Long, User> find = new Finder<Long, User>(Long.class,
			User.class);

	public User(String username, String email, String password, boolean isAdmin) {
		super(email, password);
		this.username = username;		
		this.created = new Date();
		this.isAdmin = isAdmin;
	}

	/**
	 * Creates a new User and saves it to the database
	 * 
	 * @param username
	 *            String
	 * @param email
	 *            String
	 * @param password
	 *            String
	 * @return the id of the new user (long)
	 */
	public static long createUser(String username, String email,
			String password, boolean isAdmin) {
		User newUser = new User(username, email, password, isAdmin);
		newUser.save();
		return newUser.id;
	}

	/* Return all users */
	public static List<User> all() {
		return getFind().all();
	}
	
	/**
	 * 
	 * @return all users as List<User>
	 */
	public static List<User> allList() {
		List<User> users = find.findList();
		return users;
	}
	
	//TODO
	/*
	 *  
	 */
	public static ArrayNode allAsJson() {
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		List<User> users = all();
		for (User u : users) {
			ObjectNode userNode = Json.newObject();
			userNode.put("username", u.username);
			userNode.put("isAdmin", u.isAdmin);
			userNode.put("created", u.created.toString()); // ?
		}
		return arrayNode;
	}
	
	
	public void setAdmin(boolean isAdmin){
		this.isAdmin = isAdmin;
		save();
			
	}

	/**
	 * Login verification Verifies if the email and password exists by checking
	 * in the database
	 * 
	 * @param mail
	 *            String
	 * @param password
	 *            String
	 * @return boolean true or false
	 */
	public static boolean verifyLogin(String mail, String password) {
		try {
			User user = getFind().where().eq("email", mail).findUnique();
			if(user != null && EmailVerification.isEmailVerified(user.id)){
				return HashHelper.checkPass(password, user.password);
			}
			else{
				return false;
			}
				

		} catch (NullPointerException e) {
			Logger.error(e.getMessage());
			return false;
		}

	}

	/**
	 * Checks if there already exists a user with given username or email, and
	 * blocks registration if does.
	 * 
	 * @param username
	 *            String
	 * @param email
	 *            String
	 * @return boolean true or false
	 */
	public static boolean verifyRegistration(String username, String email) {
		List<User> usname = getFind().where().eq("username", username).findList();
		List<User> mail = getFind().where().eq("email", email).findList();
		if (usname.isEmpty() && mail.isEmpty()) {
			return true;
		} else
			return false;

	}
	
	/**
	 * Returns all admin email's
	 * @return List<String>
	 */
	public static List<String> allAdminMails(){	
		List<User> userList =  getFind().where().eq("isAdmin", true).findList();
		List<String> emails = new ArrayList<String>();
		for(User u: userList){
			emails.add(u.email);
		}
		return emails;
	}
	
	/**
	 * Returns all admin email's as Json
	 * @return ArrayNode
	 */
	public static ArrayNode allAdminMailsJSon(){	
		List<User> userList =  getFind().where().eq("isAdmin", true).findList();
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for(User u: userList){
			ObjectNode mailAd = Json.newObject();
			mailAd.put("email", u.email);
			arrayNode.add(mailAd);
		}
		return arrayNode;
	}

	/*
	 * Return user by mail
	 */
	public static User getUser(String mail) {
		User user = getFind().where().eq("email", mail).findUnique();

		return user;
	}
	
	public static ObjectNode getUserJson(String mail) {
		User user = getFind().where().eq("email", mail).findUnique();
		ObjectNode userJson = Json.newObject();
		userJson.put("username", user.username);
		userJson.put("email", user.email);
		userJson.put("isAdmin", user.isAdmin);
		userJson.put("created", user.created.toString()); // ?????
		userJson.put("updated", user.updated.toString()); // ?????
		userJson.put("profilePicture", user.profilePicture);

		return userJson;
	}
	
	/*
	 * Delete user by id
	 */
	public static void delete(long id) {
		getFind().byId(id).delete();
	}

	/*
	 * Find user by ID
	 */
	public static User find(long id) {
		return getFind().byId(id);
	}

	public static User find(boolean isAdmin) {
		return getFind().where().eq("isAdmin", isAdmin).findUnique();
	}
	/*
	 * Find and return list of admins
	 */
	public static List<User>findAdmins(boolean isAdmin){
		return getFind().where().eq("isAdmin",isAdmin).findList();//proba-provjeriti
	}

	/* 
	 * Find and return user by username 
	 */
	public static User find(String username) {
		return getFind().where().eq("username", username).findUnique();
	}

	public static boolean check(String mail) {
		return getFind().where().eq("email", mail).findUnique() != null;
	}
	
	/**
	 * Checks if the user is last admin
	 * @param user - user which we check
	 * @return true if user is admin, else return false
	 */
	public static boolean isLastAdmin(User user) {
		List<User> adminList=User.findAdmins(true);
		if(adminList.size() == 1 && user.equals(adminList.get(0))) 
			return true;
		return false;
	}

	/**
	 * @return the find
	 */
	public static Finder<Long, User> getFind() {
		return find;
	}

	/**
	 * @param find the find to set
	 */
	public static void setFind(Finder<Long, User> find) {
		User.find = find;
	}
	
	public static User findByEmail(String email){
		return find.where().eq("email", email).findUnique();
	}

	
}
