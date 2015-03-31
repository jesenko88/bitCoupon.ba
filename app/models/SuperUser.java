package models;

import java.util.List;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@MappedSuperclass
public  class SuperUser extends Model{
	
	
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


}
