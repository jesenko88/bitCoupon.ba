package models;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import play.Logger;
import play.data.validation.Constraints.MinLength;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

/**
 * 
 * Entity class for the Coupon. Creates a table in the database with all of the
 * properties
 *
 */

@Entity
public class Coupon extends Model {

	@Id
	public long id;

	@MinLength(4)
	public String name;

	public double price;
	
	@Column(name="created_at")
	public Date dateCreated;

	@Column(name="expired_at")
	public Date dateExpire;

	public String picture;

	@ManyToOne
	public Category category;

	public String description;
	
	public String remark;

	@OneToMany
	public Photo photo;
	/*
	 * public String code;
	 * 
	 * public boolean lastMinute;
	 * 
	 * public double duration;
	 * 
	 * public boolean specialPrice;
	 * 
	 * public int viewCount;
	 * 
	 * public boolean specialOffer;
	 * 
	 * public long multiOffer_id;
	 * 
	 * public boolean status;
	 * 
	 * public long company_id;
	 * 
	 * public long comment_user_id;
	 * 
	 * public long response_company_id;
	 */

	public Coupon(String name, double price,
			Date dateExpire, String picture, Category category,
			String description, String remark) {

		this.name = name;
		this.price = price;
		this.dateCreated = new Date();
		this.dateExpire = dateExpire;
		this.picture = picture;
		this.category = category;
		this.description = description;
		this.remark=remark;
		/*
		 * this.code = code; this.lastMinute = lastMinute; this.duration =
		 * duration; this.specialPrice = specialPrice; this.viewCount =
		 * viewCount; this.specialOffer = specialOffer; this.multiOffer_id =
		 * multiOffer_id; this.status = status; this.company_id = company_id;
		 * this.comment_user_id = comment_user_id;
		 * this.response_company_id = response_company_id;
		 */
	}

	public static Finder<Long, Coupon> find = new Finder<Long, Coupon>(Long.class,
			Coupon.class);

	/**
	 * Creates a new Coupon and saves it to the database
	 * 
	 * @return the id of the new Coupon (long)
	 */

	public static long createCoupon(String name, double price, Date dateExpire, String picture,
		Category category, String description,String remark) {
		
		//Logger.debug(category.name);
		Coupon newCoupon = new Coupon(name, price,dateExpire,
				picture, category, description, remark);
		newCoupon.save();
		return newCoupon.id;
	}

	/**
	 * 
	 * @return price as String in 00.00 format
	 */
	public String getPriceString() {
		return String.format("%1.2f",price);
	}

	/**
	 * 
	 * @return all coupons as List<Coupon>
	 */
	public static List<Coupon> all() {
		List<Coupon> coupons = find.findList();
		return coupons;
	}

	/**
	 * Find coupon by id
	 * @param id long
	 * @return Coupon
	 */
	public static Coupon find(long id) {
		return find.byId(id);
	}

	
	/**
	 * Delete coupon by id
	 * @param id long
	 */
	public static void delete(long id) {
		find.byId(id).delete();
	}
	
	/**
	 * Checks if the coupon exists
	 * @param name of coupon String
	 * @return true or false
	 */
	public static boolean checkByName(String name){
		return find.where().eq("name", name).findUnique() != null;
	}
	
	/**
	 * Updates coupon
	 * @param coupon
	 */
	public static void updateCoupon(Coupon coupon){
		coupon.save();
	}
	
	/**
	 * @param category name as String
	 * @return List of coupons by category 
	 */
    public static List<Coupon> listByCategory(String categoryName){

    	return find.where().eq("category", Category.findByName(categoryName)).findList();
    }

    /* getter*/
	public String getCategoryName() {
		return category.name;
	}
	

   public String getExpiration(){
	   if ( dateExpire == null){
		   return "";
	   } 
	   SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	   return "Expiring: " + dateFormat.format(dateExpire);
	   
   }

}
