package models;


import java.util.Date;
import java.util.UUID;

import javax.persistence.*;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class ResetPasword extends Model {
	
	@Id
	public String id;
	
	@Required
	public String userEmail;

	public Date date;
	
	public static Finder<String, ResetPasword> find = new Finder<String, ResetPasword>(String.class, ResetPasword.class);
	
	public ResetPasword(String userEmail){
		this.id = UUID.randomUUID().toString();
		this.userEmail = userEmail;
		this.date = new Date();
	}
	
	public static String createRequest(String userEmail){
		ResetPasword ressetPassword = new ResetPasword(userEmail);
		ressetPassword.save();
		return ressetPassword.id;		
	}
	
	public static String findByID(String id){
		Logger.debug(id);
		String email = "";
		try{
			email = find.byId(id).userEmail;
		} catch(Exception e){
			return null;
		}
		return email;
	}
}

