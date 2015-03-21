package controllers;

import helpers.AdminFilter;

import java.text.ParseException;
import java.util.List;

import models.Category;
import models.Coupon;
import models.User;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import views.html.category.*;
import play.mvc.Controller;

public class CategoryController extends Controller {

	static Form<Category> categoryForm = new Form<Category>(Category.class);
	
	
	public static Result categoryView(String categoryName){
		
		return ok(categoryPage.render(User.find(session("name")),Coupon.listByCategory(categoryName), categoryName));
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategoryView(){
		
		return ok(categoryPanel.render(session("name")));
	}
	
	
	@Security.Authenticated(AdminFilter.class)
	public static Result listCategories(){
		
		return ok(CategoriesList.render(session("name"), Category.all()));
	}
	
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategory() {

		if (categoryForm.hasErrors()) {
			return redirect("/categoryPanel");
		}

		// TODO handle invalid inputs

		String name = categoryForm.bindFromRequest().field("name").value();
		if (name.length() < 4) {
			flash("error","Name must be at least 4 characters");
			return ok(categoryPanel.render(session("name")));

		}
		if(name.length() > 20){
			flash("error","Name must be max 120 characters long");
			return ok(categoryPanel.render(session("name")));
		}
		if(Category.checkByName(name)){
			flash("error","Category already exists");
			return ok(categoryPanel.render(session("name")));
		}

		//String image = categoryForm.bindFromRequest().field("image").value();		
		//Category.createCategory(name,image);
		
		Category.createCategory(name);
		
		flash("success","Category " + "\""+ name + "\"" + " added");
		return ok(categoryPanel.render( session("name")));
	}
	
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
		Category.delete(id);
		return ok(CategoriesList.render(session("name"), Category.all()));
	}
	
	public static Result editCategoryView(String name){
		Category category = Category.findByName(name); /////////////////////----
		return ok(editCategory.render(session("name"),category));
	}
	
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
		if(Category.checkByName(name)){
			flash("error","Category already exists");
			return ok(editCategory.render(session("name"), category));
		}
		
		category.name = name;
		//TODO image
		category.save();
		
		flash("success","Category " + "\""+ name + "\"" + " updated");
		return ok(editCategory.render( session("name"), category));
	}
	
}
