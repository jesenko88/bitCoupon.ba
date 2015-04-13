package models;

import helpers.AdminFilter;
import helpers.CurrentUserFilter;
import helpers.HashHelper;
import helpers.MailHelper;
import java.util.ArrayList;
import java.io.File;
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
public class Company  extends SuperUser{	
	
	@Required
	public String name;	
	
	public Date created;
	
	public Date updated;
	
	public String logo;
	
	public String contact;
		
	@OneToMany(mappedBy="seller",cascade=CascadeType.ALL)
	public List<Coupon> coupons;
	
//	@OneToMany(mappedBy="seller", cascade=CascadeType.ALL)
//	public List<TransactionCP> sold_coupons;	
	
	public static Finder<Long, Company> find = new Finder<Long, Company>(Long.class,
				Company.class);
	
	/**
	 * Constructor for company.
	 * @param name Name of company
	 * @param email	email for loggin
	 * @param password password for loggin
	 * @param created date when its created
	 * @param logo logo, picture path.
	 */
	public Company(String name, String email, String password, Date created, String logo, String adress, String city, String contact){
		super(email,password, adress, city);
		this.name = name;
		this.created = created;
		this.logo = "images/home/company-default.jpg";
		this.contact = contact;	
	}
	public static long createCompany(String name, String email, String password, String logo, String adress, String city, String contact){
		logo = "images/home/No-Logo.jpg";
		Date now = new Date();
		Company c = new Company(name, email, password, now, logo, adress, city, contact);
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
		if(all == null)
			all = new ArrayList<Company>();
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
		return getFind().where().eq("name", name).findUnique();
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
	public static boolean verifyRegistration(String name, String email) {
		List<Company> usname = find.where().eq("name", name).findList();
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
	/**
	 * Checks if a company exists in the database
	 * searching by name
	 * @param name of the Company
	 * @return true or false
	 */
	public static boolean exists(String name) {
		return getFind().where().eq("name", name).findUnique() != null;
	}
	
	public static List<Company> approvedCompanies() {
		return find.where().eq("status", true)
				.findList();
	}
	
	public static List<Company> nonApprovedCompanies() {
		return find.where().eq("status", false).findList();
	}	 
}
