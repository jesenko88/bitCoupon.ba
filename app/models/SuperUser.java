package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@MappedSuperclass
public  abstract class SuperUser extends Model{
	
	
	@Id
	public long id;

	@Email
	public String email;

	@Required
	public String password;

	
	
	public SuperUser( String email, String password) {		
		this.email = email;
		this.password = password;
	}

	public static List<SuperUser> allSuperUsers(){
		List<Company> allComp = Company.all();
		List<User> allUsr = User.all();
		
		List<SuperUser> all = new ArrayList<SuperUser>();
		all.addAll(allComp);
		all.addAll(allUsr);
		return all;		
	}
	
	public boolean isUser(){
		return (this instanceof User);
	}
	
	public User getUser(){
		User u = (User) this;
		Logger.debug("User: " +u.username);
		return u;
	}

}
