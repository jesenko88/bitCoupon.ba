package models;

import helpers.AdminFilter;
import helpers.CurrentUserFilter;
import helpers.HashHelper;
import helpers.MailHelper;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import controllers.Sesija;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.mvc.Result;
import play.mvc.Security;

@Entity
public class Company  extends Model{

	@Id
	public long id;

	@Required
	public String name;

	@Email
	public String email;

	@Required
	public String password;
	
	public Date created;
	
	public Date updated;
	
	public String logo;
	
	public boolean verfied;
	
	static Finder<Long, Company> find = new Finder<Long, Company>(Long.class,
				Company.class);
	
	/**
	 * Constructor for company.
	 * @param name Name of company
	 * @param email	email for loggin
	 * @param password password for loggin
	 * @param created date when its created
	 * @param logo logo, picture path.
	 */
	public Company(String name, String email, String password, Date created, String logo){
		this.name = name;
		this.email = email;
		this.password = password;
		this.created = created;
		this.logo = logo;
		this.verfied = false;
	}
	
	public static long createCompany(String name, String email, String password, String logo){
		Date now = new Date();
		Company c = new Company(name, email, password, now, logo);
		c.save();
		return c.id;	
	}
	
	public static Company findById(long id){
		Company c = find.byId(id);
		return c;
	}
	
	public static void delete(long id){
		Company c = find.byId(id);
		c.delete();
	}
	
	public static Company findByEmail(String email) {
		return getFind().where().eq("email", email).findUnique();
	}
	
	public static List<Company> all(){
		List<Company> all = find.all();
		return all;
	}
	
	public static List<Company> findByName(String name){
		List<Company> byName = find.where().eq("name", name).findList();
		return byName;
	}
	
	/**
	 * @return the find
	 */
	public static Finder<Long, Company> getFind() {
		return find;
	}
	
	/** 
	 * Find and return user by username 
	 */
	public static Company find(String name) {
		return getFind().where().eq("username", name).findUnique();
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
		List<Company> usname = find.where().eq("username", username).findList();
		List<Company> mail = find.where().eq("email", email).findList();
		if (usname.isEmpty() && mail.isEmpty()) {
			return true;
		} else
			return false;
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
			Company company = find.where().eq("email", mail).findUnique();
			if(company != null && EmailVerification.isEmailVerified(company.id)){
				return HashHelper.checkPass(password, company.password);
			}
			else{
				return false;
			}			
		} catch (NullPointerException e) {
			Logger.error(e.getMessage());
			return false;
		}
	}

}
