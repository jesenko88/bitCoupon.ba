package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.db.ebean.Model;

@Entity
public class Subscriber extends Model{

	@Id
	public long id;
	
	@OneToOne
	public User subscriber;
	
	public String newsletter;

	
	public Subscriber(User subscriber){
		this.subscriber = subscriber;
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
	 * Method for removing user from our subscribers list.
	 * @param u
	 */
	public static void unsubscribe(User u ){
		Subscriber s = find.where().eq("subscriber", u).findUnique();
		s.delete();
	}
	
	/**
	 * Method creates HTML page for email newsletter.
	 * @param listOfCoupons
	 * @return
	 */
	public static String getPreparedHTMLContent(String listOfCoupons){
		
		StringBuilder sb = new StringBuilder();
		
		
		
		return null;
	}

	public static List<String> getAllSubscribers() {
		List<Subscriber> allSubscribers = find.all();
		List<String> allEmails = new ArrayList<String>();
		for(Subscriber s: allSubscribers){
			allEmails.add(s.subscriber.email);
		}
		return allEmails;
	}
	
	
	
}
