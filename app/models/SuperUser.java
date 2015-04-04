package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;


@MappedSuperclass
public abstract class SuperUser extends Model {

	@Id
	public long id;

	@Email
	public String email;

	@Required
	public String password;
	

	public SuperUser(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public static List<SuperUser> allSuperUsers() {
		List<Company> allComp = Company.all();
		List<User> allUsr = User.all();

		List<SuperUser> all = new ArrayList<SuperUser>();
		all.addAll(allComp);
		all.addAll(allUsr);
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
		User u = User.findByEmail(email);
		Company c = Company.findByEmail(email);
		if (u == null && c == null) {
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
