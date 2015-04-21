package models;

import javax.persistence.*;

import play.db.ebean.Model;

/**
 *Model represents statistic for each coupon.
 *TODO: Since its statistic and has lot of insertions in
 *		this model, we should use NoSQL DB for this model.
 */
@Entity
public class Statistic extends Model{
	
	@Id
	public long id;

	@OneToOne
	public Coupon coupon;
	
	public int visited;
	
	public int bought;
	
	
	static Finder<Long, Statistic> find = new Finder<Long, Statistic>(Long.class, Statistic.class);
	/**
	 * Constructor for statistic.
	 * At creation all stats are set to 0.
	 * @param coupon
	 */
	public Statistic(Coupon coupon){
		this.coupon = coupon;
		this.visited = 0;
		this.bought = 0;
	}
	
	/**
	 * Create statistic for coupon.
	 * This method should be used at creation of coupon.
	 * @param coupon
	 */
	public static Statistic createStatistic(Coupon coupon){
		Statistic statistic = new Statistic(coupon);
		statistic.save();
		return statistic;
	}
	
	/**
	 * Reset statistic of a single coupon.
	 * @param coupon
	 */
	public static void resetStatistic(Coupon coupon){
		Statistic stats = find.where().eq("coupon", coupon).findUnique();
		stats.visited = 0;
		stats.bought = 0;
		stats.save();
	}
	
	/**
	 * Non static method for increasing visit statistic.
	 * Created to avoid using .save() and similar things
	 * directly in controller.
	 */
	public void visited(){
		this.visited++;
		this.update();		
	}

	/**
	 * Non static method for increasing number of purchases on this coupon.
	 */
	public void bought(int quantity) {
		this.bought += quantity;
		this.update();	
		
	}
}
