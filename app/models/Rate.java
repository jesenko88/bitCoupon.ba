package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Rate extends Model {

	@Id
	public long id;

	@Required
	public double rate;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Coupon coupon;
	
	@ManyToOne
	public Company company;
	
	public Date date;
	
	static Finder<Long, Rate> find = new Finder<Long, Rate>(Long.class, Rate.class);

	/**
	 * Constructor for Rate 
	 * @param rate - rate of coupon
	 * @param user - user who is rating
	 * @param coupon - coupon which is rated
	 */
	public Rate(double rate, User user, Coupon coupon){
		this.rate = rate;
		this.user = user;
		this.coupon = coupon;
		this.date = new Date();
	}
	
	/**
	 * Constructor for Rate 
	 * @param rate - rate of company
	 * @param user - user who is rating
	 * @param coupon - company which is rated
	 */
	public Rate(double rate, User user, Company company){
		this.rate = rate;
		this.user = user;
		this.company = company;
		this.date = new Date();
	}
	
	 /**
	  * Creates a new rate in DB
	  * @param rate
	  * @param user
	  * @param coupon
	  */
	public static void create(double rate, User user, Coupon coupon){
		new Rate(rate, user, coupon).save();
	}
	
	/**
	  * Creates a new rate in DB
	  * @param rate
	  * @param user
	  * @param company
	  */
	public static void create(double rate, User user, Company company) {
		new Rate(rate, user, company).save();
	}
	/**
	 * Deletes rate from DB
	 * @param id
	 */
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	/**
	 * Change existing rate
	 * @param id - id of existing rate
	 * @param newRate - new rate
	 */
	public static void update(long id, double newRate){
		Rate rate = find.where().eq("id", id).findUnique();
		rate.rate = newRate;
		rate.update();
	}
	
	/**
	 * Finds rate by coupon
	 * @param coupon
	 * @return list of rates
	 */
	public static List<Rate> findByCoupon(long couponId){
		return find.where().eq("coupon_id", couponId).findList();
	}
	
	/**
	 * Finds rate by company id
	 * @param company
	 * @return list of rates
	 */
	public static List<Rate> findByCompany(long companyId){
		return find.where().eq("company_id", companyId).findList();
	}
	
	/**
	 * Find rate by id
	 * @param id - id of rate
	 * @return - certain rate
	 */
	public static Rate findById(long id){
		return find.where().eq("id", id).findUnique();
	}

	/**
	 * Find rate by user
	 * @param userId
	 * @return list of rates
	 */
	public static List<Rate> findByUser(long userId) {
		return find.where().eq("user_id", userId).findList();
	}
	
	/**
	 * Find rate by user id
	 * @param couponId
	 * @return certain rate
	 */
	public static Rate findByUserId(long userId) {
		return find.where().eq("user_id", userId).findUnique();
	}
	
	/**
	 * Find rate by coupon id
	 * @param couponId
	 * @return certain rate
	 */
	public static Rate findByCouponId(long couponId) {
		return find.where().eq("coupon_id", couponId).findUnique();
	}
	
	/**
	 * Find rate by company id
	 * @param companyId
	 * @return certain rate
	 */
	public static Rate findByCompanyId(long companyId) {
		return find.where().eq("company_id", companyId).findUnique();
	}
	
	
	/**
	 * Method which calculate average grade of certain coupon
	 * @param id - id of coupon
	 * @return - average grade of coupon
	 */
	public static double progres(long id) {
		double progress = 0;
		List<Rate> rates = findByCoupon(id);
		for(int i = 0; i < rates.size(); i++) {
			progress += rates.get(i).rate;
		}
		if(rates.size() > 0) {
		progress = progress / rates.size();
		return progress;
		} else 
			return 0;
		
	}
	
	/**
	 * Method which calculate average grade of certain company
	 * @param id - id of company
	 * @return - average rate of company
	 */
	public static double companyProgres(long companyId) {
		double progress = 0;
		List<Rate> rates = findByCompany(companyId);
		for(int i = 0; i < rates.size(); i++) {
			progress += rates.get(i).rate;
		}
		if(rates.size() > 0) {
		progress = progress / rates.size();
		return progress;
		} else 
			return 0;
		
	}
	
	/**
	 * Check if the user is already rate coupon
	 * @param userId
	 * @return return true if user is already rate, else return false
	 */
	public static boolean alreadyRate(long userId, long couponId) {
		List<Rate> findByUser = findByUser(userId);
		List<Rate> findByCoupon = findByCoupon(couponId);
		for(int i = 0; i < findByUser.size(); i++) {
			for(int j = 0; j < findByCoupon.size(); j++) {
				
				if(findByCoupon != null && findByUser != null && findByCoupon.get(j).equals(findByUser.get(i)))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Check if the user is already rate company
	 * @param userId
	 * @return return true if user is already rate, else return false
	 */
	public static boolean alreadyRateCompany(long userId, long companyId) {
		List<Rate> findByUser = findByUser(userId);
		List<Rate> findByCompany = findByCompany(companyId);
		for(int i = 0; i < findByUser.size(); i++) {
			for(int j = 0; j < findByCompany.size(); j++) {
				
				if(findByCompany != null && findByUser != null && findByCompany.get(j).equals(findByUser.get(i)))
					return true;
			}
		}
		return false;
	}
}
