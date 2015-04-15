package models;

import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class Pin extends Model {

	@OneToOne
	public User user;

	public String code;

	public boolean valid;

	private Pin(String pin, User u, boolean valid) {
		this.code = pin;
		this.valid = valid;
		this.user = u;
	}

	public static Pin generatePin(User user) {
		String generatedPin = UUID.randomUUID().toString();
		generatedPin.substring(0, 6);
		Pin pin = new Pin(generatedPin, user, true);
		System.out.println("PINININII" + pin.code);
		pin.save();
		return pin;
	}

	private static void terminatePin(Pin pin, int minutes) {
		long currentTime = Calendar.getInstance().getTimeInMillis();
		long duration = currentTime + (minutes * 60000);
		while (currentTime < duration) {
			currentTime = Calendar.getInstance().getTimeInMillis();
		}
		System.out.println("VALIDAACIJAA" + pin.valid);
		pin.valid = false;
		System.out.println("VALIDAACIJAA" + pin.valid);
		pin.delete();
	}
}
