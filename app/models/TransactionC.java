package models;

import javax.persistence.*;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class TransactionC extends Model{
	
	@Id
	public long id;
	
	public String name; //zasada
	

	@ManyToOne(cascade=CascadeType.ALL)
	public User buyer;

	@ManyToOne(cascade=CascadeType.ALL)
	public User seller;
	
	@OneToOne(cascade=CascadeType.ALL)
	public Coupon coupon;

	
	private static Finder<Long, TransactionC> find = new Finder<Long, TransactionC>(Long.class,
			TransactionC.class);
	
	public TransactionC(String name, User buyer, User seller, Coupon coupon) {
		this.name = name;
		this.buyer = buyer;
		this.seller = seller;
		this.coupon = coupon;
	}
	
	public static long createTransaction(String name, User buyer, User seller, Coupon coupon) {
		
		TransactionC transaction = new TransactionC(name, buyer, seller, coupon);
		transaction.save();
		
		return transaction.id;
		
	}
	
	public static TransactionC find(long id) {
		return find.byId(id);
	}
	
	
	
	
}
