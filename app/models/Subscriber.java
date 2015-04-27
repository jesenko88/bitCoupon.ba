package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

/**
 * Model of subscribers on our web.
 * Subscribers will be receiving newsletters which admin sends.
 * Admin will use MailHelper class to send newsletter.
 *
 */
@Entity
public class Subscriber extends Model{

	@Id
	public long id;
	
	@Required
	public String token;
	
	@OneToOne
	public User subscriber;	
	
	@Email
	public String email;	
	
	
	
	public Subscriber(User subscriber){		
		this.token = UUID.randomUUID().toString();
		this.subscriber = subscriber;
		this.email = subscriber.email;		
	}
	
	/**
	 * Constructor for non registered user who want to subscribe.
	 * @param email
	 */
	public Subscriber(String email){
		this.token = UUID.randomUUID().toString();
		this.subscriber = null;
		this.email = email;
	}
	 	
	
	public static Finder<Long, Subscriber> find = new Finder<Long, Subscriber>(Long.class, Subscriber.class);
	
	/**
	 * Method which subscribe user to our newsletters.
	 * @param user
	 * @return
	 */
	public static long subscribe(User user){
		Subscriber subscriber = new Subscriber(user);
		subscriber.save();
		return subscriber.id;
	}
	
	/**
	 * For non registered user who want to subscribe.
	 * @param email
	 */
	public static void subscribe(String email){
		Subscriber subscriber = new Subscriber(email);
		subscriber.save();
	}
	
	/**
	 * Method for removing user from our subscribers list.
	 * @param u
	 */
	public static void unsubscribe(User u ){
		Subscriber subscriber = find.where().eq("subscriber", u).findUnique();
		subscriber.delete();
	}
	
	public static void unsubscribe(Subscriber subscriber ){	
		subscriber.delete();
	}

	/**
	 * All subscribers emails.
	 * @return
	 */
	public static List<String> getAllSubscribers() {
		List<Subscriber> allSubscribers = find.all();
		List<String> allEmails = new ArrayList<String>();
		if(allSubscribers == null){
			return allEmails;
		}
		for(Subscriber s: allSubscribers){
			allEmails.add(s.email);
		}
		return allEmails;
	}
	
	/**
	 * Getting subscriber by token.
	 * @param token
	 * @return
	 */
	public static Subscriber findByToken(String token){
		List<Subscriber> all = find.all();
		for(Subscriber subscriber: all){
			if(subscriber.token.equals(token)){
				Logger.debug("Subscribers email: " +subscriber.email);
				return subscriber;
			}
		}
		 return null;
	}
	
	/**
	 * Gets subscriber by email.
	 * @param email
	 * @return
	 */
	public static Subscriber findByEmail(String email){
		Subscriber subscriber = find.where().eq("email", email).findUnique();
		return subscriber;
	}
	/**
	 * Get token from User ( if subscribed)
	 * @param u
	 * @return
	 */
	public static String getToken(String email){
		Subscriber subscriber = find.where().eq("email", email).findUnique();		
		return subscriber.token;
	}
	
	/**
	 * Check if user is subscribed.
	 * @param user
	 * @return
	 */
	public static boolean isSubscribed(User user){
		Subscriber current = find.where().eq("subscriber", user).findUnique();
		return current != null;
	}
	/**
	 * Check if user is subscribed.
	 * @param u
	 * @return
	 */
	
	public static boolean isSubscribed(String email){
		Subscriber current = find.where().eq("email", email).findUnique();
		return current != null;
	}
	
}
