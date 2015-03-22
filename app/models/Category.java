package models;

import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class Category extends Model{

	@Id
	public long id;
	
	@Required
	public String name;
	
	public String picture;	
	
	@OneToMany
	public List<Coupon> coupons;
    
    
    static Finder<Long, Category> find = new Finder<Long, Category>(Long.class,
			Category.class);
    
    public Category(String name, String image){
    	this.name=name;
    	this.picture=image;
    	
    	}
    
    public Category(String name){
    	this.name=name;
    	
    }
    
    public static long createCategory(String name){
    	Category newCategory=new Category(name);
    	newCategory.save();
    	return newCategory.id;
    }
    
    public static long createCategory(String name,String image){
    	Category newCategory=new Category(name, image);
    	newCategory.save();
    	return newCategory.id;
    	
    }
    
    public static List<Category>all(){
    	return find.all();
    }
    
    public static Category find(long id){
    	return find.byId(id);
    }
    
    public static Category findByName(String name){
    	List<Category>list=find.where().eq("name", name).findList();
    		return list.get(0);
    	}
    
    public static boolean exists(String name){
		 List<Category> list=find.where().eq("name", name).findList();
		 if(list.size()>0)
			 return true;
		 return false;
	}
    
    public static void delete(long id){
    	find.byId(id).delete();
    }
}
