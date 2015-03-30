package models;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

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
	
	public static List<Company> all(){
		List<Company> all = find.all();
		return all;
	}
	
	public static List<Company> findByName(String name){
		List<Company> byName = find.where().eq("name", name).findList();
		return byName;
	}

}
