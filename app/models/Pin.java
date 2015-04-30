package models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import play.Logger;
import play.db.ebean.Model;

@Entity
public class Pin extends Model {

	@Id
	int id;
	
	@OneToOne
	public User user;

	public String code;

	public Date date;
	
	private static Finder<Integer, Pin> find = new Finder<Integer, Pin>(Integer.class,
			Pin.class);

	private Pin(User u, String pin) {
		this.user = u;
		this.code = pin;
		this.date = new Date();
	}

	public static Pin generatePin(User user) {
		if (user.pin != null)
			user.pin.delete();
		String generatedPin = UUID.randomUUID().toString().substring(0, 6);
		Pin pin = new Pin(user, generatedPin);
		pin.save();
		return pin;
	}

	public static boolean isValid(Date generated) {
		Date currentDate = new Date();//****
		long difference = Math.abs(generated.getTime() - currentDate.getTime());
		if (difference <= 5 * 60 * 1000) {
			return true;
		}
		return false;
	}

	public static String getCode(long id) {
		User user = User.find(id);
		Pin pin = find.where().eq("user", user).findUnique();
		if (pin != null)
			return pin.code;
		return "";
	}
	
	public static User getPinUser(String pin) {
		try{
			Pin userPin = find.where().eq("code", pin).findUnique();
			User user = userPin.user;
			return user;
		}catch(Exception e){
			Logger.error("No user for pin", e.getMessage());
			return null;
		}
	}
	
	public static Pin getPin(String pin) {
		try{
			Pin p = find.where().eq("code", pin).findUnique();
			return p;
		}catch(Exception e){
			Logger.error("Unexisting pin", e.getMessage());
			return null;
		}
	}

	
}
