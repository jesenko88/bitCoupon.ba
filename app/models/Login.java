package models;

import play.db.ebean.Model;

/**
 * Class used just to make a Form for login
 *
 */

public class Login extends Model {
	
	public String email;
	public String password;

	
}
