package models;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import play.Logger;
import play.data.validation.Constraints.MinLength;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.libs.F.Option;
import play.mvc.QueryStringBindable;

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

	@Column(name = "created_at")
	public Date dateCreated;

	@Column(name = "expired_at")
	public Date dateExpire;

	public String picture;

	@ManyToOne
	public Category category;

	@Column(columnDefinition = "TEXT")
	public String description;

	public String remark;
	
	@ManyToOne
	public Company seller;
	
	@OneToMany(mappedBy="coupon", cascade=CascadeType.ALL)
	public List<TransactionCP> buyers;

	public int minOrder;
	
	public int maxOrder;
	
	public Date usage;
	
    public boolean status;
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
	 * public long multiOffer_id
	 *  
	 * public long company_id;
	 * 
	 * public long comment_user_id;
	 * 
	 * public long response_company_id;
	 */

	public Coupon(String name, double price, Date dateExpire, String picture,
			Category category, String description, String remark) {

		this.name = name;
		this.price = price;
		this.dateCreated = new Date();
		this.dateExpire = dateExpire;
		this.picture = picture;
		this.category = category;
		this.description = description;
		this.remark = remark;		
		
		/*
		 * this.code = code; this.lastMinute = lastMinute; this.duration =
		 * duration; this.specialPrice = specialPrice; this.viewCount =
		 * viewCount; this.specialOffer = specialOffer; this.multiOffer_id =
		 * multiOffer_id; this.status = status; this.company_id = company_id;
		 * this.comment_user_id = comment_user_id; this.response_company_id =
		 * response_company_id;
		 */
	}
	public Coupon(String name, double price, Date dateExpire, String picture,
			Category category, String description, String remark, int minOrder, int maxOrder,Date usage, Company seller) {

		this.name = name;
		this.price = price;
		this.dateCreated = new Date();
		this.dateExpire = dateExpire;
		this.picture = picture;
		this.category = category;
		this.description = description;
		this.remark = remark;
		this.minOrder = minOrder;
		this.maxOrder = maxOrder;
		this.usage = usage;
		this.seller = seller;
		this.status = false;
	}

	public static Finder<Long, Coupon> find = new Finder<Long, Coupon>(
			Long.class, Coupon.class);

	/**
	 * Creates a new Coupon and saves it to the database
	 * 
	 * @return the id of the new Coupon (long)
	 */

	public static long createCoupon(String name, double price, Date dateExpire,
			String picture, Category category, String description, String remark) {

		// Logger.debug(category.name);
		Coupon newCoupon = new Coupon(name, price, dateExpire, picture,
				category, description, remark);
		newCoupon.save();
		return newCoupon.id;
	}
	
	/**
	 * Method with minimum order variable.
	 */
	public static long createCoupon(String name, double price, Date dateExpire,
			String picture, Category category, String description, String remark, int minOrder,int maxOrder, Date usage, Company seller, User buyer) {

		// Logger.debug(category.name);	
		Coupon newCoupon = new Coupon(name, price, dateExpire, picture,
				category, description, remark, minOrder, maxOrder, usage, seller);
		newCoupon.save();
		return newCoupon.id;
	}
	
	/**
	 * Method with status variable for global coupons.
	 */
	public static long createCoupon(String name, double price, Date dateExpire,
			String picture, Category category, String description, String remark, int minOrder, int maxOrder, Date usage, Company seller, boolean status) {

		// Logger.debug(category.name);
		Coupon newCoupon = new Coupon(name, price, dateExpire, picture,
				category, description, remark, minOrder, maxOrder, usage, seller);
		newCoupon.status = status;
		newCoupon.save();
		return newCoupon.id;
	}

	/**
	 * 
	 * @return price as String in 00.00 format
	 */
	public String getPriceString() {
		return String.format("%1.2f", price);
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
	 * 
	 * @param id
	 *            long
	 * @return Coupon
	 */
	public static Coupon find(long id) {
		return find.byId(id);
	}

	/**
	 * Delete coupon by id
	 * 
	 * @param id
	 *            long
	 */
	public static void delete(long id) {
		find.byId(id).delete();
	}

	/**
	 * Checks if the coupon exists
	 * 
	 * @param name
	 *            of coupon String
	 * @return true or false
	 */
	public static boolean checkByName(String name) {
		return find.where().eq("name", name).findUnique() != null;
	}

	/**
	 * Updates coupon
	 * 
	 * @param coupon
	 */
	public static void updateCoupon(Coupon coupon) {
		coupon.save();
	}

	/**
	 * @param category
	 *            name as String
	 * @return List of coupons by category
	 */
	public static List<Coupon> listByCategory(String categoryName) {

		return find.where().eq("category", Category.findByName(categoryName))
				.findList();
	}

	/**
	 * Get the category of the coupon as String
	 * 
	 * @return category name
	 */
	public String getCategoryName() {
		return category.name;
	}

	/**
	 * Get the expiration date as String in a simple date format day/month/year
	 * 
	 * @return date String
	 */
	public String getExpiration() {
		if (dateExpire == null) {
			return "";
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return "Expiring: " + dateFormat.format(dateExpire);

	}

	// Static constants we're going to use for sorts.
	public static final int SORT_ASCENDING = 1;
	public static final int SORT_DESCENDING = -1;

	/**
	 * Method sorting coupons by category.
	 * 
	 * @param method
	 *            which you choose to sort, 1 for ascending, -1 for descending.
	 * @return
	 */
	public static List<Coupon> sortByCategory(int method) {
		List<Coupon> all = find.all();
		/*
		 * Implementing comparator. Comparing category names and return its
		 * string compare value.
		 */
		Comparator<Coupon> c = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return c1.category.name.compareTo(c2.category.name);
			}
		};

		if (method == SORT_ASCENDING) {
			all.sort(c);
		} else if (method == SORT_DESCENDING) {
			all.sort(c.reversed());
		} else {
			Logger.error("Wrong method type for sorting");
			return null;
		}
		return all;
	}

	/**
	 * Method sorting by price.
	 * 
	 * @param method
	 *            for sort. 1 for ascending, -1 for descending.
	 * @return
	 */
	public static List<Coupon> sortByPrice(int method) {
		List<Coupon> all = find.all();

		/*
		 * Creating comparator for sorting by price.
		 */
		Comparator<Coupon> c = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return (int) (c1.price - c2.price);
			}
		};

		if (method == SORT_ASCENDING) {
			all.sort(c);
		} else if (method == SORT_DESCENDING) {
			all.sort(c.reversed());
		} else {
			Logger.error("Sorted by price went wrong, parameter sent as method is wrong");
			return null;
		}
		return all;
	}

	/**
	 * Method sorting coupons by date.
	 * 
	 * @param method
	 *            for sort. 1` for ascending sort, -1 for descending.
	 * @return List of sorted coupons or null if list was empty or method was
	 *         wrong.
	 */
	public static List<Coupon> sortByDate(int method) {
		List<Coupon> all = find.all();

		/*
		 * Creating comparator for sorting by date.
		 */
		Comparator<Coupon> c = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				if (c1.dateExpire.before(c2.dateExpire)) {
					return -1;
				} else if (c1.dateExpire.after(c2.dateExpire)) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		if (method == SORT_ASCENDING) {
			all.sort(c);
		} else if (method == SORT_DESCENDING) {
			all.sort(c.reversed());
		} else {
			Logger.error("Sorting by date went wrong, method not accepted.");
			return null;
		}
		return all;
	}

	/**
	 * Method for sorting coupon list.
	 * 
	 * @param cpns
	 *            List of coupons to sort.
	 * @param method
	 *            method of sorting, 1 for ascending, -1 for descending.
	 * @return sorted list of coupons or null if there was error.
	 */
	public static List<Coupon> sortByCategory(List<Coupon> cpns, int method) {
		;
		/*
		 * Implementing comparator. Comparing category names and return its
		 * string compare value.
		 */
		Comparator<Coupon> c = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return c1.category.name.compareTo(c2.category.name);
			}
		};

		if (method == SORT_ASCENDING) {
			cpns.sort(c);
		} else if (method == SORT_DESCENDING) {
			cpns.sort(c.reversed());
		} else {
			Logger.error("Wrong method type for sorting");
			return null;
		}
		return cpns;
	}

	/**
	 * Method for sorting list sent as parameter.
	 * 
	 * @param cpns
	 *            list of coupons
	 * @param method
	 *            of sorting, 1 for ascending, -1 for descending
	 * @return sorted list or null
	 */
	public static List<Coupon> sortByPrice(List<Coupon> cpns, int method) {
		/*
		 * Creating comparator for sorting by price.
		 */
		Comparator<Coupon> c = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return (int) (c1.price - c2.price);
			}
		};

		if (method == SORT_ASCENDING) {
			cpns.sort(c);
		} else if (method == SORT_DESCENDING) {
			cpns.sort(c.reversed());
		} else {
			Logger.error("Sorted by price went wrong, parameter sent as method is wrong");
			return null;
		}
		return cpns;
	}

	/**
	 * Method for sorting list of coupons by date.
	 * 
	 * @param cpns
	 * @param method
	 * @return
	 */
	public static List<Coupon> sortByDate(List<Coupon> cpns, int method) {
		/*
		 * Creating comparator for sorting by date.
		 */
		Comparator<Coupon> c = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				if(c1.dateExpire == null || c2.dateExpire == null){
					return 0;
				}				
				
				if (c1.dateExpire.before(c2.dateExpire)) {
					return -1;
				} else if (c1.dateExpire.after(c2.dateExpire)) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		if (method == SORT_ASCENDING) {
			cpns.sort(c);
		} else if (method == SORT_DESCENDING) {
			cpns.sort(c.reversed());
		} else {
			Logger.error("Sorting by date went wrong, method not accepted.");
			return null;
		}
		return cpns;
	}

	/**
	 * Method to parse list of coupons into comma separated string format.
	 * This we use for sorting all coupons or only searched one. Instead of routing
	 * whole list of Coupon we only send this string.
	 * @param coupons
	 * @return
	 */
	public static String commaSeparatedIds(List<Coupon> coupons) {
		if(coupons.size() < 1){
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		for (Coupon c : coupons) {
			sb.append(c.id).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}


	/*
	 * public static List<Coupon> listByDate(){ List<Coupon> oldCoupon = new
	 * ArrayList<Coupon>(); List<Coupon> allCoupon = Coupon.all();
	 * 
	 * for(Coupon cp: allCoupon){ Date today = new Date(); Date expire =
	 * cp.dateExpire; if(today.before(expire)){ oldCoupon.add(cp); }
	 * 
	 * 
	 * } if (oldCoupon.isEmpty()){ return null; } } return oldCoupon;
	 */
	
	
	/**
	 * Return all coupons owned by a company
	 * @param id of the company
	 * @return List of Coupons
	 */
	public static List<Coupon> ownedCoupons(long companyID) {

		return find.where().eq("seller_id", companyID).findList();
	}
	
	public static List<Coupon> userBoughtCoupons(long userId) {
		return find.where().eq("buyer_id", userId).findList();
	}
	
	/**
	 * Method checking if this coupon expired.
	 * Returning true if it expired and false if it is not expired.
	 * @return
	 */
	public  boolean checkIfExpired(){
		Date now = new Date();
		return dateExpire.before(now);
	}

	
	
	public static List<Coupon> approvedCoupons() {
		List<Coupon> all = find.all();
		List<Coupon> approved = new ArrayList<Coupon>();
		for(Coupon coupon: all){
			if(coupon.status){
				approved.add(coupon);
			}
		}
		return approved;
	}
	
	public static List<Coupon> nonApprovedCoupons() {
		List<Coupon> all = find.all();
		List<Coupon> nonApproved = new ArrayList<Coupon>();
		for(Coupon coupon: all){
			if(!coupon.status){
				nonApproved.add(coupon);
			}
		}
		return nonApproved;
	}
	
}
