package models;

import java.util.List;

import javax.persistence.*;

import com.avaje.ebean.ExpressionList;

import play.db.ebean.Model;

/**
 * Class which represents photos for each coupon.
 * These photos are not main photo which is required to be uploaded
 * those are optional photos. Each coupon has only 4 optional photos to be
 * uploaded.
 */
@Entity
public class Photo extends Model {

	@Id
	public int id;
	
	public String path;
	
	@ManyToOne
	public Coupon coupon;
	
	
	static Finder<Integer, Photo> finder = new Finder<Integer, Photo>(Integer.class, Photo.class);
	
	public Photo(String path, Coupon cp){
		this.path = path;
		this.coupon = cp;
	}
	
	public static void create(String path, Coupon cp){
		new Photo(path, cp).save();
	}
	
	public static void delete(int id){
		finder.byId(id).delete();
	}
	
	/**
	 * Method for getting size of photo stack of coupon
	 * sent as parameter.
	 * @param c coupon whose stack size we check.
	 * @return integer which is stack size.
	 */
	public static int photoStackLength(Coupon c){
		List<Photo> photos =  finder.all();
		int counter = 0;
		for(Photo p: photos){
			if(p.coupon.id == c.id){
				counter ++;
			}
		}
		return counter;
	}
}
