package models;

import helpers.HashHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import models.Coupon.Status;
import play.Logger;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;

@Entity
public class Company  extends SuperUser{	
	
	@Required
	@MinLength(3)
	@MaxLength(45)
	@Pattern(value = "^[A-Za-z\\u00A1-\\uFFFF0-9 .,]*"
			+ "[A-Za-z\\u00A1-\\uFFFF0-9][A-Za-z\\u00A1-\\uFFFF0-9 .,]*$",
			message="Company contact format is not valid."	)
	public String name;	
	
	public Date created;
	
	public Date updated;
	
	public String logo;	
	
	public String contact;
		
	public int notifications;
	
	public int status;

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
		this.status = Status.DEFAULT;
	}
	
	/**
	 * Method which creates company
	 * @param name
	 * @param email
	 * @param password
	 * @param logo
	 * @param adress
	 * @param city
	 * @param contact
	 * @return company id
	 */
	public static long createCompany(String name, String email, String password, String logo, String adress, String city, String contact, int status){
		logo = "images/home/No-Logo.jpg";
		Date now = new Date();
		Company company = new Company(name, email, password, now, logo, adress, city, contact);
		company.status = status;
		company.save();
		return company.id;
	
	}
	
	/**
	 * Method which finds company by id
	 * @param id of company
	 * @return company
	 */
	public static Company findById(long id){
		Company company = find.byId(id);
		return company;
	}
	
	/**
	 * Method for deleting company
	 * @param id of company
	 */
	public static void delete(long id){
		Company company = find.byId(id);
		company.status = Status.DELETED;
		company.save();
	}
	
	/**
	 * Method which finds certain company by email adress
	 * @param email of company
	 * @return company
	 */
	public static Company findByEmail(String email) {
		return getFind().where().eq("email", email).findUnique();
	}
	
	/**
	 * Method which finds list of all companies in DB
	 * @return
	 */
	public static List<Company> all(){
		List<Company> all = find.all();
		if(all == null)
			all = new ArrayList<Company>();
		return all;
	}
	
	/**
	 * Method which finds list of companies by name 
	 * @param name of companies
	 * @return list of companies
	 */
	public static List<Company> findByName(String name){
		List<Company> byName = find.where().eq("name", name).findList();
		if(byName == null)
			byName = new ArrayList<Company>();
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
	
//	public String validate() {
//		
//		if ( name.length() < 4 || name.length() > 70){
//			return "Coupon name has to be in range 4 - 70 characters";
//		}
//		if ( price <= 0){
//			return "Invalid price";
//		}
//		if (dateExpire.before(new Date())){
//			return "Invalid date";
//		}
//		if (category.name.equals("New Category")){
//			if (Category.findByName(category.name) != null){
//				return "Category allready exists";
//			}
//		}else{
//			if (Category.findByName(category.name) == null){
//				return "Invalid category selection";
//			}
//		}
//		if (description.length() < 10 || description.length() > 999){
//			return "Description length has to be in range 10 - 999 characters";
//		}
//		if (remark.length() > 150){
//			return "Remark length has to be max 150 characters";
//		}
//		if ( minOrder < 0 || maxOrder < 0 || minOrder > maxOrder){
//			return "Invalid order amount";
//		}
//		//TODO dateUsage ??
//		
//	return null;
//}
	
	/**
	 * Method which finds all approved companies in DB
	 * @return
	 */
	public static List<Company> approvedCompanies() {
		//List<Coupon> approvedCoupons =find.where().eq("status", Status.ACTIVE).orderBy().desc("dateCreated").findList();
		List<Company> approvedCompanies =find.where().eq("status", Status.ACTIVE).findList();
		if(approvedCompanies == null)
			approvedCompanies = new ArrayList<Company>();
		return approvedCompanies;
	}
	
	/**
	 * Method which finds all nonApproved companies in DB
	 * @return list of nonApproved companies
	 */
	public static List<Company> nonApprovedCompanies() {
			
			List<Company> nonApprovedCompanies = find.where().eq("status", Status.DEFAULT).findList();
			if(nonApprovedCompanies == null)
				nonApprovedCompanies = new ArrayList<Company>();
			return nonApprovedCompanies;
		}
}
