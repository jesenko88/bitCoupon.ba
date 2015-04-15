package models;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import controllers.CouponController;
import controllers.PayPalController;
import controllers.Sesija;
import play.api.mvc.Session;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import scala.Array;

/**
 * 
 * TransactionCP class
 * Name specified with 'CP' at the end because of
 * possible naming issues, and standard usage of the name Transaction.
 * This entity makes the relation between user and coupon.
 * It stores the id's of the buyer, seller and the id of the sold coupon.
 * Among that, other data is stored as well (payment_id, date ...etc)
 *
 */
@Entity
public class TransactionCP extends Model{
	

	@Id
	public long id;
	
	public String payment_id; //zasada
	
	public double couponPrice;
	
	public int quantity;
	
	public double totalPrice;
	
	public String token; //token from paypalReturn

	@ManyToOne(cascade=CascadeType.ALL)
	public User buyer;
	
	@ManyToOne(cascade=CascadeType.ALL)
	public Coupon coupon;
	
	public Date date;
	
	/* ebean finder */
	private static Finder<Long, TransactionCP> find = new Finder<Long, TransactionCP>(Long.class,
			TransactionCP.class);
	
	/* constructor */
	public TransactionCP(String payment_id,double couponPrice,int quantity, double totalPrice, String token,
			User buyer, Coupon coupon) {
		this.payment_id = payment_id;
		this.couponPrice = couponPrice;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.token = token;
		this.buyer = buyer;
		this.coupon = coupon;
		this.date = new Date();
	}

	/**
	 * Creates a new transaction and saves it to the database
	 * @param payment_id String
	 * @param money_amount double
	 * @param token String
	 * @param buyer User
	 * @param seller to be declared**
	 * @param coupon Coupon
	 * @return id of the created transaction (long)
	 */
	public static long createTransaction(String payment_id,double couponPrice,int quantity,
			double totalPrice, String token, User buyer,  Coupon coupon) {
		
		TransactionCP transaction = new TransactionCP(payment_id, couponPrice, quantity,totalPrice, token, buyer, coupon);
		transaction.save();
		
		return transaction.id;
		
	}
	
	/**
	 * Returns a transaction
	 * @param id of the transaction
	 * @return
	 */
	public static TransactionCP find(long id) {
		return find.byId(id);
	}
	
	/**
	 * Returns all coupons that someone bought
	 * @param id of the buyer
	 * @return List of coupons
	 */
	public static List<Coupon> allBoughtCoupons(long id) {
		List<TransactionCP> ids = find.where().eq("buyer_id", id).findList();
		List<Coupon> coupons = new ArrayList<Coupon>();
		for ( TransactionCP tsc : ids) {
			coupons.add(Coupon.find(tsc.coupon.id));
		}
		return coupons;
	}
	
	/**
	 * Returns all transactions from a certain buyer
	 * @param id of the buyer
	 * @return List of transactions
	 */
	public static List<TransactionCP> allFromBuyer(long id) {
		return find.where().eq("buyer_id", id).findList();
	}
	
	/**
	 * Get transaction date and time as String
	 * @return
	 */
	public String getDateString() {
		if (date == null) {
			return "";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return "Expiring: " + dateFormat.format(date);

	}
	
	public static List<TransactionCP> allFromCompany(long id) {
		List<Coupon> coupons = Coupon.ownedCoupons(id);
		List<TransactionCP> ids = TransactionCP.find.all();
		List<TransactionCP> forCompany = new ArrayList<TransactionCP>();
		for(int i = 0; i < ids.size(); i++) {
			for(int j = 0; j < coupons.size() ; j ++) {
				if(coupons.get(j).equals(ids.get(i).coupon))
						forCompany.add(ids.get(i));
			}
		}
		Collections.reverse(forCompany);
		return forCompany;
	}
	
}
