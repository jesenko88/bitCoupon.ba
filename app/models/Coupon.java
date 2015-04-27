package models;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import api.JSonHelper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.Min;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

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

	@Required
	@MinLength(3)
	@MaxLength(45)
	@Pattern(value = "^[A-Za-z0-9 .,!?()_]*[A-Za-z0-9][A-Za-z0-9 .,!?()_]*$",
			message="Company name format is not valid."	)
	public String name;
	
	@Required
	public double price;

	@Column(name = "created_at")
	@NotNull
	public Date dateCreated;

	@Column(name = "expired_at")
	@Required
	@Future
	public Date dateExpire;

	public String picture;

	@ManyToOne
	public Category category;

	@Column(columnDefinition = "TEXT")
	@Required
	@MinLength(10)
	@MaxLength(1000)
	@Pattern(value = "^[A-Za-z0-9 .,!?()_]*[A-Za-z0-9][A-Za-z0-9 .,!?()_]*$",
			message="Company description format is not valid."	)
	public String description;
	
	@MinLength(6)
	@MaxLength(200)
	@Pattern(value = "^[A-Za-z0-9 .,!?()_]*[A-Za-z0-9][A-Za-z0-9 .,!?()_]*$",
			message="Company remark format is not valid."	)
	public String remark;

	@ManyToOne
	@NotNull
	public Company seller;

	@OneToMany(mappedBy = "coupon")
	public List<TransactionCP> buyers;

	@Required
	@Min(0)
	public int minOrder;
	
	@Required
	public int maxOrder;
	
	@Required
	@Future
	public Date usage;

	public int status;
	
	@OneToOne
	public Statistic statistic;
	
	@OneToMany(mappedBy = "coupon")
	public List<Question> questions;
	
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
	}

	public Coupon(String name, double price, Date dateExpire, String picture,
			Category category, String description, String remark, int minOrder,
			int maxOrder, Date usage, Company seller) {

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
		this.status = Status.DEFAULT;		
	}
	
	/*TODO coupons for empty fields */
	public Coupon(String name, String picture) {
		this.name = name;
		this.picture = picture;
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
		
		Coupon newCoupon = new Coupon(name, price, dateExpire, picture,
				category, description, remark);
		newCoupon.save();	
		Statistic statistic = Statistic.createStatistic(newCoupon);
		newCoupon.statistic = statistic;
		newCoupon.save();
		//Creating statistic for each coupon created.
				
		return newCoupon.id;
	}

	/**
	 * Method with minimum order variable.
	 */
	public static long createCoupon(String name, double price, Date dateExpire,
			String picture, Category category, String description,
			String remark, int minOrder, int maxOrder, Date usage,
			Company seller, User buyer) {

		// Logger.debug(category.name);
		Coupon newCoupon = new Coupon(name, price, dateExpire, picture,
				category, description, remark, minOrder, maxOrder, usage,
				seller);
		newCoupon.save();	
		Statistic statistic = Statistic.createStatistic(newCoupon);
		newCoupon.statistic = statistic;
		newCoupon.save();				
		return newCoupon.id;
	}

	/**
	 * Method with status variable for global coupons.
	 */
	public static long createCoupon(String name, double price, Date dateExpire,
			String picture, Category category, String description,
			String remark, int minOrder, int maxOrder, Date usage,
			Company seller, int status) {

		// Logger.debug(category.name);
		Coupon newCoupon = new Coupon(name, price, dateExpire, picture,
				category, description, remark, minOrder, maxOrder, usage,
				seller);		
		newCoupon.status = status;
		newCoupon.save();	
		Statistic statistic = Statistic.createStatistic(newCoupon);
		newCoupon.statistic = statistic;
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
		if (coupons == null)
			coupons = new ArrayList<Coupon>();
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
		Coupon coupon = find.byId(id);
		coupon.status = Status.DELETED;
		coupon.save();
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
		List<Coupon> coupons = find.where()
				.eq("category", Category.findByName(categoryName)).findList();
		if (coupons == null)
			coupons = new ArrayList<Coupon>();
		return coupons;
	}

	/**
	 * @param categoryName
	 * @return List of coupons by provided category. ArrayNode (JSon content)
	 */
	public static ArrayNode listByCategoryJSon(String categoryName) {

		return JSonHelper.couponListToJson(find.where()
				.eq("category", Category.findByName(categoryName)).findList());
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
		return dateFormat.format(dateExpire);

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

		// Handling exceptions.
		if (all == null) {
			return new ArrayList<Coupon>();
		}
		/*
		 * Implementing comparator. Comparing category names and return its
		 * string compare value.
		 */
		Comparator<Coupon> comparator = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return c1.category.name.compareTo(c2.category.name);
			}
		};

		if (method == SORT_ASCENDING) {
			all.sort(comparator);
		} else if (method == SORT_DESCENDING) {
			all.sort(comparator.reversed());
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
		// Handling exceptions.
		if (all == null) {
			return new ArrayList<Coupon>();
		}
		/*
		 * Creating comparator for sorting by price.
		 */
		Comparator<Coupon> comparator = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return (int) (c1.price - c2.price);
			}
		};

		if (method == SORT_ASCENDING) {
			all.sort(comparator);
		} else if (method == SORT_DESCENDING) {
			all.sort(comparator.reversed());
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
		// Handling exceptions.
		if (all == null) {
			return new ArrayList<Coupon>();
		}
		/*
		 * Creating comparator for sorting by date.
		 */
		Comparator<Coupon> comparator = new Comparator<Coupon>() {
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
			all.sort(comparator);
		} else if (method == SORT_DESCENDING) {
			all.sort(comparator.reversed());
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
		if (cpns == null)
			return new ArrayList<Coupon>();

		/*
		 * Implementing comparator. Comparing category names and return its
		 * string compare value.
		 */
		Comparator<Coupon> comparator = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return c1.category.name.compareTo(c2.category.name);
			}
		};

		if (method == SORT_ASCENDING) {
			cpns.sort(comparator);
		} else if (method == SORT_DESCENDING) {
			cpns.sort(comparator.reversed());
		} else {
			Logger.error("Wrong method type for sorting");
			return null;
		}
		return cpns;
	}

	/**
	 * Method for sorting list sent as parameter.
	 * 
	 * @param coupons
	 *            list of coupons
	 * @param method
	 *            of sorting, 1 for ascending, -1 for descending
	 * @return sorted list or null
	 */
	public static List<Coupon> sortByPrice(List<Coupon> coupons, int method) {

		if (coupons == null)
			return new ArrayList<Coupon>();
		/*
		 * Creating comparator for sorting by price.
		 */
		Comparator<Coupon> comparator = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				return (int) (c1.price - c2.price);
			}
		};

		if (method == SORT_ASCENDING) {
			coupons.sort(comparator);
		} else if (method == SORT_DESCENDING) {
			coupons.sort(comparator.reversed());
		} else {
			Logger.error("Sorted by price went wrong, parameter sent as method is wrong");
			return null;
		}
		return coupons;
	}

	/**
	 * Method for sorting list of coupons by date.
	 * 
	 * @param coupons
	 * @param method
	 * @return
	 */
	public static List<Coupon> sortByDate(List<Coupon> coupons, int method) {
		if (coupons == null)
			return new ArrayList<Coupon>();
		/*
		 * Creating comparator for sorting by date.
		 */
		Comparator<Coupon> comparator = new Comparator<Coupon>() {
			@Override
			public int compare(Coupon c1, Coupon c2) {
				if (c1.dateExpire == null || c2.dateExpire == null) {
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
			coupons.sort(comparator);
		} else if (method == SORT_DESCENDING) {
			coupons.sort(comparator.reversed());
		} else {
			Logger.error("Sorting by date went wrong, method not accepted.");
			return null;
		}
		return coupons;
	}

	/**
	 * Method to parse list of coupons into comma separated string format. This
	 * we use for sorting all coupons or only searched one. Instead of routing
	 * whole list of Coupon we only send this string.
	 * 
	 * @param coupons
	 * @return
	 */
	public static String commaSeparatedIds(List<Coupon> coupons) {
		if (coupons.size() < 1) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Coupon coupon : coupons) {
			sb.append(coupon.id).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}	


	public static List<Coupon> userBoughtCoupons(long userId) {
		List<Coupon> coupons = find.where().eq("buyer_id", userId).findList();
		if (coupons == null) {
			coupons = new ArrayList<Coupon>();
		}
		return coupons;
	}

	/**
	 * Method checking if this coupon expired. Returning true if it expired and
	 * false if it is not expired.
	 * @return
	 */
	public boolean checkIfExpired() {
		Date now = new Date();
		if (dateExpire != null)
			return dateExpire.before(now);
		return false;
	}

	public static List<Coupon> approvedCoupons() {
		List<Coupon> approvedCoupons =find.where().eq("status", Status.ACTIVE).findList();
		if(approvedCoupons == null)
			approvedCoupons = new ArrayList<Coupon>();
		return approvedCoupons;

	}
	
	/**
	 * Returns the number of empty fields on in a row on the page
	 * @return
	 */
	public static int numberOfEmptyFields(){
		
		List<Coupon> approved = approvedCoupons();
		int columns = 3;
		int emptyFields = approved.size() % columns;
		if(emptyFields > 0)
			return columns - emptyFields;
		return 0;
	}	

		public static List<Coupon> nonApprovedCoupons() {
			
			List<Coupon> nonApprovedCoupons = find.where().eq("status", Status.DEFAULT).findList();
			if(nonApprovedCoupons == null)
				nonApprovedCoupons = new ArrayList<Coupon>();
			return nonApprovedCoupons;
		}

	public static List<Coupon> ownedCoupons(long companyID) {		
		 
		List<Coupon> ownedByCompany = find.where().eq("seller_id", companyID).findList();
		if(ownedByCompany == null)
			ownedByCompany = new ArrayList<Coupon>();
		return ownedByCompany;	
	}
	
	
	public String validate() {
		
			if ( name.length() < 4 || name.length() > 70){
				return "Coupon name has to be in range 4 - 70 characters";
			}
			if ( price <= 0){
				return "Invalid price";
			}
			if (dateExpire.before(new Date())){
				return "Invalid expiration date";
			}
			if (category.name.equals("New Category")){
				if (Category.findByName(category.name) != null){
					return "Category allready exists";
				}
			}else{
				if (Category.findByName(category.name) == null){
					return "Invalid category selection";
				}
			}
			if (description.length() < 10 || description.length() > 999){
				return "Description length has to be in range 10 - 999 characters";
			}
			if (remark.length() > 150){
				return "Remark length has to be max 150 characters";
			}
			if ( minOrder < 0 || maxOrder < 0 || minOrder > maxOrder){
				return "Invalid order quantity, minimal order can not exceed max order";
			}
			//TODO dateUsage ??
			
		return null;
	}
	
	/**
	 * This method gets list of coupons by status sent as parameter.
	 * in case list is null or sent status is not valid, method
	 * returns empty array list.
	 * @param status
	 * @return
	 */
	public static List<Coupon> getByStatus(int status){
		List<Coupon> byStatus = find.where().eq("status", status).findList();
		if(byStatus == null){
			byStatus = new ArrayList<Coupon>();
		}
		return byStatus;
	}
	
	/**
	 * Non static method checks if coupon has expired.
	 * @return true if coupon has expired, false if it is not.
	 */
	public boolean hasExpired(){
		if(dateExpire.before(new Date()))
			return true;
		return false;
	}
	/**
	 * Method checks if selected coupon can or cannot
	 * be deleted. 
	 * Coupon may be deleted by company only
	 * if if it is expired and/or has no buyers.
	 * @param coupon
	 * @return
	 */
	public  boolean isDeletable(){		
		if(!hasExpired())
			return false;
		if(buyers.size() > 0)
			return false;
		return true;					
	}
	
	/**
	 * List of constants which
	 * represents status of coupon.
	 *
	 */
	public abstract class Status{
		
		public static final int ACTIVE = 1;
		public static final int DEFAULT = 0;
		public static final int DELETED = -1;
		public static final int EXPIRED = -2;
		public static final int OFFER_FAILED = -3;
		public static final int OFFER_SUCCEED = 3;
		
	}
	
}
