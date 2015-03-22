package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.text.ParseException;
import java.util.List;

import models.Category;
import models.Coupon;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import views.html.*;
import play.mvc.Controller;

public class CategoryController extends Controller {

	static Form<Category> categoryForm = new Form<Category>(Category.class);
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategoryView(){
		
		return ok(categoryPanel.render(session("name"), null));
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
			return ok(categoryPanel.render(session("name"), "Name must be 4 characters long"));

		}
		if(name.length()>20){
			return ok(categoryPanel.render(session("name"),"Name must be max 120 characters long"));
		}
		
		String picture = FileUpload.imageUpload("category-photos");		
		
		if(!Category.checkByName(name)){
			if(picture != null){
				Category.createCategory(name, picture);
			}
			Category.createCategory(name, FileUpload.DEFAULT_IMAGE);
		}
		//else()-dodati flash:"Category already exists!"
		//return ok(categoryPanel.render( session("name"), "Category \"" + name));
		//String image = categoryForm.bindFromRequest().field("image").value();
		
		//Category.createCategory(name,image);
		
		
		return ok(categoryPanel.render( session("name"), "Category \"" + name
				+ "\" added"));
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
	
}
