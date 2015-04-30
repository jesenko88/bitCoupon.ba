package models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.Play;
import play.db.ebean.Model;

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
	
	private static final String BIT_PAYMENT_PREFIX = Play.application().configuration().getString("bitPaymentPrefix");
	

	@Id
	public long id;
	
	public String payment_id; //zasada
	
	public String bitPayment_id; 
	
	public String sale_id;
	
	public double couponPrice;
	
	public int quantity;
	
	public double totalPrice;
	
	public String token; //token from paypalReturn

	@ManyToOne
	public User buyer;
	
	/*for unregistered buyers*/
	public String buyer_name;
	
	public String buyer_surname;
	
	@ManyToOne
	public Coupon coupon;
	
	public Date date;
	
	/* ebean finder */
	public static Finder<Long, TransactionCP> find = new Finder<Long, TransactionCP>(Long.class,
			TransactionCP.class);
	
	/* constructor */
	public TransactionCP(String payment_id, String saleId, double couponPrice,int quantity, double totalPrice, String token,
			User buyer, Coupon coupon) {
		this.payment_id = payment_id;
		this.bitPayment_id = BIT_PAYMENT_PREFIX + UUID.randomUUID().toString().substring(0, 7);
		this.sale_id = saleId;
		this.couponPrice = couponPrice;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.token = token;
		this.buyer = buyer;
		this.buyer_name = buyer.username;
		this.buyer_surname = buyer.surname;
		this.coupon = coupon;
		this.date = new Date();
	}
	
	/* constructor for unregistered users */
	public TransactionCP(String payment_id, String saleId, double couponPrice,int quantity, double totalPrice, String token,
			String username, String surname, Coupon coupon) {
		this.payment_id = payment_id;
		this.bitPayment_id = BIT_PAYMENT_PREFIX + UUID.randomUUID().toString().substring(0, 7);
		this.sale_id = saleId;
		this.couponPrice = couponPrice;
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.token = token;
		this.buyer = null;
		this.buyer_name = username;
		this.buyer_surname = surname;
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
	public static long createTransaction(String payment_id, String saleId, double couponPrice,int quantity,
			double totalPrice, String token, User buyer,  Coupon coupon) {
		
		TransactionCP transaction = new TransactionCP(payment_id, saleId, couponPrice, quantity,totalPrice, token, buyer, coupon);
		transaction.save();
		
		return transaction.id;
		
	}
	
	/**
	 * Creates a new transaction for a unregistered user.
	 * Instead of user id, it saves the username and surname of the buyer/gift receiver
	 * @param payment_id
	 * @param couponPrice
	 * @param quantity
	 * @param totalPrice
	 * @param token
	 * @param username
	 * @param surname
	 * @param coupon
	 * @return id of the transaction Long
	 */
	public static long createTransactionForUnregisteredUser(String payment_id, String saleId, double couponPrice,int quantity,
			double totalPrice, String token, String username, String surname,  Coupon coupon) {	
		TransactionCP transaction = new TransactionCP(payment_id, saleId, couponPrice, quantity,totalPrice, token, username, surname, coupon);
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
		return dateFormat.format(date);

	}
	
	/**
	 * Method which return all transactions of all coupons owned by certain company
	 * @param id of company
	 * @return list of transactions
	 */
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
