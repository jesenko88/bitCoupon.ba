package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

@MappedSuperclass
public abstract class SuperUser extends Model {

	@Id
	public long id;

	@Email
	public String email;

	@Required	
	@Pattern(value = "^[A-Za-z0-9]*[A-Za-z0-9][A-Za-z0-9]*$",
	message="Password not valid, only letters and numbers alowed."	)
	public String password;
	
	@MinLength(6)
	@MaxLength(165)
	@Pattern(value = "^[A-Za-z\\u00A1-\\uFFFF0-9 .,]*"
			+ "[A-Za-z\\u00A1-\\uFFFF0-9][A-Za-z\\u00A1-\\uFFFF0-9 .,]*$",
			message="Adress not valid, only letters and numbers alowed."	)
	public String adress;
	
	@MinLength(6)
	@MaxLength(165)
	@Pattern(value = "^[A-Za-z\\u00A1-\\uFFFF0-9 .,]*"
			+ "[A-Za-z\\u00A1-\\uFFFF0-9][A-Za-z\\u00A1-\\uFFFF0-9 .,]*$",
			message="City not valid, only letters and numbers alowed."	)
	public String city;
	

	public SuperUser(String email, String password, String adress, String city) {
		this.email = email;
		this.password = password;
		this.adress = adress;
		this.city = city;
	}

	public static List<SuperUser> allSuperUsers() {
		List<Company> allCompanies = Company.all();
		List<User> allUsers = User.all();
		
		//In case user list or company list is null.
		if(allCompanies == null){
			allCompanies = new ArrayList<Company>();
		}
		if(allUsers == null){
			allUsers = new ArrayList<User>();
		}
		
		List<SuperUser> all = new ArrayList<SuperUser>();
		all.addAll(allCompanies);
		all.addAll(allUsers);
		return all;
	}
	
	
	public boolean isUser() {
		return (this instanceof User);
	}

	public boolean isCompany() {
		return (this instanceof Company);
	}

	public User getUser() {
		User u = (User) this;
		return u;
	}

	public Company getCompany() {
		return (Company) this;
	}

	/**
	 * Method checks if user with this email already exists.
	 * 
	 * @param email
	 * @return
	 */
	public static boolean verifyRegistration(String email) {
		User user = User.findByEmail(email);
		Company company = Company.findByEmail(email);
		if (user == null && company == null) {
			return true;
		} else
			return false;

	}

	/**
	 * Finds and returns a user or company by provided email
	 * @param email String
	 * @return SuperUser (user or company)
	 */
	public static SuperUser getSuperUser(String email) {
		User user = User.findByEmail(email);
		Company company = Company.findByEmail(email);
		if (user != null) {
			return user;
		}
		return company;
	}

}
