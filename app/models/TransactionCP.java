package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import controllers.PayPalController;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import scala.Array;

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
	
	public Date date;

	
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
		this.date = new Date();
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
	
	
	public static List<Coupon> boughtCoupons(long id) {
		List<TransactionCP> ids = find.where().eq("buyer_id", id).findList();
		List<Coupon> coupons = new ArrayList<>();
		for ( TransactionCP tsc : ids) {
			coupons.add(Coupon.find(tsc.coupon.id));
		}
		return coupons;
	}
	
	
}
