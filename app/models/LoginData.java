package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class LoginData extends Model {
	
	@Id
	public long id;
	
	public long user_id;
	
	public Date lastLogin;
	
	public int activeCoupons;

	static Finder<Long, LoginData> find = new Finder<Long, LoginData>(Long.class, LoginData.class);
	
	/**
	 * Constructor for login data.
	 * Saves the loginData record to the database
	 * @param userId long
	 * @param loginTime Date
	 * @param activeCoupons int
	 */
	public LoginData(long userId, Date loginTime, int activeCoupons) {
		this.user_id = userId;
		this.lastLogin = loginTime;
		this.activeCoupons = activeCoupons;
		this.save();
	}
	
	/**
	 * Returns login data for a user.
	 * Contains the last login time, and number of active coupons at
	 * the last time the user was logged in
	 * @param userId long
	 * @return LoginData
	 */
	public static LoginData findByUserId(long userId) {
		return find.where().eq("user_id", userId).findUnique();
	}
	
	/**
	 * Updates an existing record
	 * @param loginTime
	 * @param activeCoupons
	 */
	public void updateRecord(Date loginTime, int activeCoupons) {
		this.lastLogin = loginTime;
		this.activeCoupons = activeCoupons;
		this.update();
	}
	
	
}
