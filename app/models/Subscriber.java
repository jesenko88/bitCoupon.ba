package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import play.Logger;
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
	
	public String token;
	
	@OneToOne
	public User subscriber;	
	
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
	 * @param subscriber
	 * @return
	 */
	public static long subscribe(User subscriber){
		Subscriber s = new Subscriber(subscriber);
		s.save();
		return s.id;
	}
	
	/**
	 * For non registered user who want to subscribe.
	 * @param email
	 */
	public static void subscribe(String email){
		Subscriber s = new Subscriber(email);
		s.save();
	}
	
	/**
	 * Method for removing user from our subscribers list.
	 * @param u
	 */
	public static void unsubscribe(User u ){
		Subscriber s = find.where().eq("subscriber", u).findUnique();
		s.delete();
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
		for(Subscriber s: all){
			if(s.token.equals(token)){
				Logger.debug("Subscribers email: " +s.email);
				return s;
			}
		}
		 return null;
	}
	/**
	 * Get token from User ( if subscribed)
	 * @param u
	 * @return
	 */
	public static String getToken(String email){
		Subscriber s = find.where().eq("email", email).findUnique();
		Logger.debug(s.token);
		return s.token;
	}
	
	/**
	 * Check if user is subscribed.
	 * @param u
	 * @return
	 */
	public static boolean isSubscribed(User u){
		Subscriber current = find.where().eq("subscriber", u).findUnique();
		return current != null;
	}
	
}
