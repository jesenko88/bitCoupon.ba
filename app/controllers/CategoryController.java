package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.util.List;

import models.Category;
import models.Coupon;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.category.CategoriesList;
import views.html.category.categoryPage;
import views.html.category.categoryPanel;
import views.html.category.editCategory;

public class CategoryController extends Controller {

	static Form<Category> categoryForm = new Form<Category>(Category.class);
	
	
	/**
	 * Shows all coupons from a certain category
	 * @param categoryName
	 * @return renders category view
	 */
	public static Result categoryView(String categoryName){
		
		return ok(categoryPage.render(User.find(session("name")),Coupon.listByCategory(categoryName), categoryName));
	}
	
	/**
	 * Renders the page for adding a new category
	 * Only admin access permitted
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategoryView(){
		
		return ok(categoryPanel.render(session("name")));
	}
	
	/**
	 * Shows all categories with edit and delete buttons
	 * Only for admin
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result listCategories(){
		
		return ok(CategoriesList.render(session("name"), Category.all()));
	}
	
	/**
	 * Add a new category in the DB
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategory() {

		if (categoryForm.hasErrors()) {
			return redirect("/categoryPanel");
		}

		String name = categoryForm.bindFromRequest().field("name").value();
		if (name.length() < 4) {
			Logger.info(session("name") + " entered a short category name");
			flash("error","Name must be at least 4 characters");
			return ok(categoryPanel.render(session("name")));

		}
		if(name.length() > 20){
			Logger.info(session("name") + " entered a too long category name");
			flash("error","Name must be max 120 characters long");
			return ok(categoryPanel.render(session("name")));
		}
		if(Category.exists(name)){
			Logger.info(session("name") + " tried to add a existing category. (" + name +")");
			flash("error","Category already exists");
			return ok(categoryPanel.render(session("name")));
		}
		/* If no picture is added, a default image is used*/
		String picture = FileUpload.imageUpload("category-photos");		
		if(picture != null){
			Category.createCategory(name, picture);
		}else {
			Category.createCategory(name, FileUpload.DEFAULT_IMAGE);
		}
	
		Logger.info(session("name") + " created a new category: \"" + name + "\"");
		flash("success","Category " + "\""+ name + "\"" + " added");
		return ok(categoryPanel.render( session("name")));
	}
	
	/**
	 * Deletes a category and sets the category property
	 * for each coupon of that category to null
	 * @param id of the category
	 * @return Renders the same page
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deleteCategory(long id) {
		Category c = Category.find(id);
		List<Coupon> cpns = c.coupons;
		for(Coupon cp : cpns){
			cp.category = null;
			cp.save();
		}
		c.coupons = null;
		c.save();
		Logger.info(session("name") + " deleted category: \"" + c.name + "\"");
		Category.delete(id);
		return ok(CategoriesList.render(session("name"), Category.all()));
	}
	
	/**
	 * Shows the edit category view 
	 * and sets the category properties in the 
	 * fields
	 * @param name of the category
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result editCategoryView(String name){
		Category category = Category.findByName(name);
		return ok(editCategory.render(session("name"),category));
	}
	
	/**
	 * Update category
	 * @param id of the category
	 * @return render same page
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result updateCategory(long id) {

		Category category = Category.find(id);
		
		if (categoryForm.hasErrors()) {
			return redirect("/editCategory");
		}
		
		String name = categoryForm.bindFromRequest().field("name").value();
		
		if (name.length() < 4) {		
			flash("error","Name must be at least 4 characters");
			return ok(editCategory.render(session("name"), category));
		}
		
		if(name.length() > 20){
			
			flash("error","Name must be max 120 characters long");
			return ok(editCategory.render(session("name"), category));
		}		
		category.name = name;
		
		String picture = FileUpload.imageUpload("category-photos");		
		
		
		if(picture != null){
				category.picture = picture;
			}

		
		category.save();
		Logger.info(session("name") + " updated category \"" + category.name + "\"");
		flash("success","Category " + "\""+ name + "\"" + " updated");
		return ok(editCategory.render( session("name"), category));
	}
	
}

