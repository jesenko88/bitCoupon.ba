package models;

import java.util.Date;
import java.util.List;
import helpers.AdminFilter;
import helpers.CurrentUserFilter;
import helpers.HashHelper;
import helpers.MailHelper;

import java.util.ArrayList;
import java.io.File;
import javax.persistence.*;

import controllers.Sesija;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.mvc.Result;
import play.mvc.Security;

@Entity
public class Rate extends Model {

	@Id
	public long id;

	@Required
	public int rate;
	
	@ManyToOne
	public User user;
	
	@ManyToOne
	public Coupon coupon;
	
	public Date date;
	
	static Finder<Long, Rate> find = new Finder<Long, Rate>(Long.class, Rate.class);

	
	public Rate(int rate, User user, Coupon coupon){
		this.rate = rate;
		this.user = user;
		this.coupon = coupon;
		this.date = new Date();
	}
	
	public static void create(int rate, User user, Coupon coupon){
		new Rate(rate, user, coupon).save();
	}
	
	public static void delete(long id){
		find.where().eq("id", id).findUnique().delete();
	}
	
	public static void update(long id, int newRate){
		Rate rate = find.where().eq("id", id).findUnique();
		rate.rate = newRate;
		rate.update();
	}
	
	public static List<Rate> findByCoupon(Coupon coupon){
		return find.where().eq("coupon", coupon).findList();
	}
	
	public static Rate findById(long id){
		return find.where().eq("id", id).findUnique();
	}

	public static Rate findByUser(long userId) {
		return find.where().eq("user_id", userId).findUnique();
	}
	
	public static int progres(long id) {
		int progress = 0;
		Coupon coupon = Coupon.find(id);
		List<Rate> rates = findByCoupon(coupon);
		for(int i = 0; i < rates.size(); i++) {
			progress = rates.get(i).rate;
		}
		progress = (int)(progress / rates.size());
		return progress;
	}
	
	public static boolean alreadyRate(long userId) {
		
		if(Rate.findByUser(userId) != null)
			return true;
		return false;
	}
}
