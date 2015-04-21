package models;

import java.util.ArrayList;
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
	public String savePath;
	
	@ManyToOne
	public Coupon coupon;
	
	
	static Finder<Integer, Photo> finder = new Finder<Integer, Photo>(Integer.class, Photo.class);
	
	public Photo(String path,String savePath, Coupon cp){
		this.path = path;
		this.coupon = cp;
		this.savePath = savePath;
	}
	
	
	public static void create(String path,String savePath, Coupon cp){
		new Photo(path,savePath, cp).save();
	}
	
	public static void delete(int id){
		finder.byId(id).delete();
	}
	
	public static Photo find(int id){
		return finder.byId(id);
	}
	
	/**
	 * Method for getting size of photo stack of coupon
	 * sent as parameter.
	 * @param c coupon whose stack size we check.
	 * @return integer which is stack size.
	 */
	public static int photoStackLength(Coupon c){
		List<Photo> photos =  finder.all();
		//In case there is no photos in DB.
		if(photos == null)
			return 0;
		
		int counter = 0;
		for(Photo p: photos){
			if(p.coupon.id == c.id){
				counter ++;
			}
		}
		return counter;
	}
	
	public static List<Photo> photosByCoupon(Coupon coupon){
		List<Photo> allPhotos = finder.all();
		List<Photo> byCoupon = new ArrayList<Photo>();
		//In case there is no photos in DB.
		if(allPhotos == null){
			return new ArrayList<Photo>();
		}
		
		for(Photo photo: allPhotos){
			if(photo.coupon.id == coupon.id){
				byCoupon.add(photo);
			}
		}		
		return byCoupon;
	}
}
