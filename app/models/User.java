package models;

import helpers.HashHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import play.Logger;
import play.Play;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.ImageController;

/**
 * 
 * Entity class for the User. Creates a table in the database with all of the
 * properties
 *
 */

@Entity
public class User extends SuperUser {

	@Required
	@MinLength(4)
	@MaxLength(45)
	@Pattern(value = "^[A-Za-z0-9 _]*[A-Za-z0-9][A-Za-z0-9 _]*$",
			message="Username not valid, only letters and numbers alowed."	)
	public String username;
	
	@Required
	@MinLength(4)
	@MaxLength(65)
	@Pattern(value = "^[A-Za-z\\u00A1-\\uFFFF0-9]*"
			+ "[A-Za-z\\u00A1-\\uFFFF0-9][A-Za-z\\u00A1-\\uFFFF0-9]*$",
			message="Surname not valid, only letters and numbers alowed."	)
	public String surname;

	@Past
	public Date dob;
	
	@NotNull
	public String gender;

	public boolean isAdmin;
	
	public Date created;
	
	public Date updated;
	
	public String profilePicture = Play.application().configuration().getString("defaultProfilePicture");

	@OneToMany(mappedBy="seller",cascade=CascadeType.ALL)
	public List<Coupon> coupons;
	
	@OneToMany(mappedBy="buyer", cascade=CascadeType.ALL)
	public List<TransactionCP> bought_coupons;
	
	@OneToOne(mappedBy="user",cascade=CascadeType.ALL)
	public Pin pin;
	
	private static Finder<Long, User> find = new Finder<Long, User>(Long.class,
			User.class);
	
	/**
	 * Private constructor for a temporary user
	 *  that is not saved to the database
	 */
	private User(String email, String username, String surname){
		super(email, null, null, null);
		this.username = username;
		this.surname = surname;
		this.email = email;
	}

	/**
	 * Constructor for User
	 * @param username
	 * @param surname
	 * @param dob
	 * @param gender
	 * @param adress
	 * @param city
	 * @param email
	 * @param password
	 * @param isAdmin
	 * @param profilePicture
	 */
	public User(String username, String surname, Date dob, String gender, String adress,
			String city, String email, String password, boolean isAdmin, String profilePicture) {
		super(email, password, adress, city);
		this.username = username;	
		this.surname = surname;
		this.dob = dob;
		this.gender = gender;
		this.created = new Date();
		this.isAdmin = isAdmin;
		this.pin = null;
		this.profilePicture = profilePicture;
	}
	
	/**
	 * Method creates a temporary user that is not saved in the database
	 * @param email String
	 * @param username String
	 * @param surname String
	 * @return User
	 */
	public static User createTempUser(String email, String username, String surname) {
		return new User(email, username, surname);
	}

	/**
	 * Creates a new User and saves it to the database
	 * 
	 * @param username  String
	 * @param email String
	 * @param password String
	 * @return the id of the new user (long)
	 */
	public static long createUser(String username, String surname, Date dob, String gender, String adress, String city, String email,
			String password, boolean isAdmin, String profilePicture) {
		User newUser = new User(username, surname, dob, gender, adress, city, email, password, isAdmin, profilePicture);
		newUser.save();
		return newUser.id;
	}

	/**
	 * Method which return all users from DB
	 * @return
	 */
	public static List<User> all() {
		List<User> all = getFind().all();
		if(all == null)
			all = new ArrayList<User>();
		return all;
	}
	
	/**
	 * 
	 * @return all users as List<User>
	 */
	public static List<User> allList() {
		List<User> users = find.findList();
		return users;
	}
	
	/**
	 * Method which puts attributes of all users to JSON object
	 * @return 
	 */
	public static ArrayNode allAsJson() {
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		List<User> users = all();
		for (User user : users) {
			ObjectNode userNode = Json.newObject();
			userNode.put("username", user.username);
			userNode.put("isAdmin", user.isAdmin);
			userNode.put("created", user.created.toString()); // ?
		}
		return arrayNode;
	}
	
	/**
	 * Method which set user as admin
	 * @param isAdmin
	 */
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
			if (user != null && EmailVerification.isEmailVerified(user.id)) {
				return HashHelper.checkPass(password, user.password);
			} else {
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
		for(User user: userList){
			emails.add(user.email);
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
		for(User user: userList){
			ObjectNode mailAd = Json.newObject();
			mailAd.put("email", user.email);
			arrayNode.add(mailAd);
		}
		return arrayNode;
	}

	/**
	 * Method which find user by email in DB
	 * @param mail of user
	 * @return user
	 */
	public static User getUser(String mail) {
		User user = getFind().where().eq("email", mail).findUnique();

		return user;
	}
	
	/**
	 * Method which puts attributes of certain user to JSON
	 * @param mail of user
	 * @return JSON object
	 */
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
	
	/**
	 * Method which deleting user from DB
	 * @param id of user
	 */
	public static void delete(long id) {
		getFind().byId(id).delete();
	}

	/**
	 * Method which finds certain user by id in DB
	 * @param id of user
	 * @return user
	 */ 
	public static User find(long id) {
		return getFind().byId(id);
	}

	/**
	 * Method which finds admin in DB
	 * @param isAdmin status-is admin
	 * @return certain user-admin
	 */
	public static User find(boolean isAdmin) {
		return getFind().where().eq("isAdmin", isAdmin).findUnique();
	}
	/**
	 * Method which finds all admins in DB
	 * @param isAdmin status-is admin
	 * @return list of all admins
	 */
	public static List<User>findAdmins(boolean isAdmin){
		return getFind().where().eq("isAdmin",isAdmin).findList();//proba-provjeriti
	}

	/**
	 * Method which finds user by username
	 * @param username of user
	 * @return user
	 */
	public static User find(String username) {
		return getFind().where().eq("username", username).findUnique();
	}

	/**
	 * Method which checks by email if the user exists in DB
	 * @param mail of user
	 * @return true if exists, else return false
	 */
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
	
	/**
	 * Method which finds user by email
	 * @param email
	 * @return
	 */
	public static User findByEmail(String email){
		return find.where().eq("email", email).findUnique();
	}

	/**
	 * Method which returns pin code
	 * @return
	 */
	public String getPin() {
		if (pin != null)
			return pin.code;
		return "";
	}
	
	
	/**
	 * Method for validating user registration form
	 * @return
	 */
	public String validate() {
		Date maxDobDate = null;
		try {
			maxDobDate = new SimpleDateFormat("dd/MM/yyyy")
			.parse(Play.application().configuration().getString("minRegistrationDOB"));
		} catch (ParseException e) {
			Logger.error(e.getMessage());
		}
		if (dob.after(maxDobDate)){
			return "You have to be over the age of 18 in order to register";
		}

		if (username.length() < 4 || username.equals("Username")
				|| username.length() > 20) {
			return "Username must be at between 4 and 20 chatacters";
		}
		if (email.equals("Email")) {

			return "Email is required for registration !";
		}
		if (password.length() < 6) {

			return "Password must be at least 6 characters!";
		}

		return null;
	}
	
	/**
	 * Method for counting notifications for admin
	 * @return number of nonApproved coupons and nonApproved companies
	 */
	public static int adminNotifications() {
		return Coupon.nonApprovedCoupons().size() + Company.nonApprovedCompanies().size();
	}
	
}
