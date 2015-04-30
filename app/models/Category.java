package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Pattern;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

/**
 * Entity class for category
 *
 */

@Entity
public class Category extends Model{

	@Id
	public long id;
	
	@Required
	@MinLength(3)
	@MaxLength(45)
	@Pattern(value = "^[A-Za-z0-9 _]*[A-Za-z0-9][A-Za-z0-9 _]*$",
			message="Category name format is not valid."	)
	public String name;
	
	//Default photo for this category coupons.
	public String picture;	
	
	@OneToMany
	public List<Coupon> coupons;
    
    
    public static Finder<Long, Category> find = new Finder<Long, Category>(Long.class,
			Category.class);
    
    /* constructors*/
    public Category(String name, String image){
    	this.name=name;
    	this.picture=image;
    	
    	}
    
    public Category(String name){
    	this.name=name;
    	
    }
    
    /**
     * Creates new category in the DB
     * without a picture
     * @param name 
     * @return id of the category
     */
    public static long createCategory(String name){
    	Category newCategory=new Category(name);
    	newCategory.save();
    	return newCategory.id;
    }
    
    /**
     * Creates a new category in the DB with
     * name and picture
     * @param name String
     * @param image String
     * @return id of the category
     */
    public static long createCategory(String name,String image){
    	Category newCategory=new Category(name, image);
    	newCategory.save();
    	return newCategory.id;
    	
    }
    
    /**
     * In case list is null, returning empty array list.
     * @return all categories as List<Category>
     */
    public static List<Category>all(){
    	List<Category> all = find.all(); 
    	if(all == null)
    		all = new ArrayList<Category>();
    	return all;
    }
    
    /**
     * Find category by id
     * @param id Long
     * @return Category
     */
    public static Category find(long id){
    	return find.byId(id);
    }
    
    /**
     * Find category by name
     * @param name String
     * @return Category
     */
    public static Category findByName(String name){
    	Category category= find.where().eq("name", name).findUnique();
    	return category;
    }
    
    /**
     * Checks if a category exists in the DB
     * by searching by name.
     * @param name String
     * @return true or false
     */
    public static boolean exists(String name){
		 List<Category> list = find.where().eq("name", name).findList();
		 if(list.size()>0)
			 return true;
		 return false;
	}
    
    /**
     * Delete category
     * @param id of the category
     */
    public static void delete(long id){
    	find.byId(id).delete();
    }
}
