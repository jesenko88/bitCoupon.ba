package models;

import java.math.BigDecimal;

import javax.persistence.*;

import controllers.PayPalController;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class TransactionCP extends Model{
	
	@Id
	public long id;
	
	public String payment_id; //zasada
	
	public double money_amount;
	
	public String token; //token from paypalReturn

	@ManyToOne(cascade=CascadeType.ALL)
	public User buyer;

	@ManyToOne(cascade=CascadeType.ALL)
	public User seller;
	
	@OneToOne(cascade=CascadeType.ALL)
	public Coupon coupon;

	
	private static Finder<Long, TransactionCP> find = new Finder<Long, TransactionCP>(Long.class,
			TransactionCP.class);
	

	
	public TransactionCP(String payment_id, double money_amount, String token,
			User buyer, User seller, Coupon coupon) {
		this.payment_id = payment_id;
		this.money_amount = money_amount;
		this.token = token;
		this.buyer = buyer;
		this.seller = seller;
		this.coupon = coupon;
	}

	public static long createTransaction(String payment_id, double money_amount, String token,
			User buyer, User seller, Coupon coupon) {
		
		TransactionCP transaction = new TransactionCP(payment_id, money_amount, token, buyer, seller, coupon);
		transaction.save();
		
		return transaction.id;
		
	}
	
	public static TransactionCP find(long id) {
		return find.byId(id);
	}
	
	
	
	
}
