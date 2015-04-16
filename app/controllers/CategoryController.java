package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.util.List;

import api.JSonHelper;
import models.Category;
import models.Company;
import models.Coupon;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.users.*;
import views.html.category.CategoriesList;
import views.html.category.categoryPage;
import views.html.category.categoryPanel;
import views.html.category.editCategory;

public class CategoryController extends Controller {

	static Form<Category> categoryForm = new Form<Category>(Category.class);

	/**
	 * Shows all coupons from a certain category
	 * 
	 * @param categoryName
	 * @return renders category view
	 */
	public static Result categoryView(String categoryName) {
		User user = User.find(session("name"));
		List<Coupon> byCategory = Coupon.listByCategory(categoryName);
		//Exception handling.
		if(byCategory == null || categoryName == null){
			flash("error", "Error has occured, please try again alter.");
			return redirect("/");
		}
		return ok(categoryPage.render(user, byCategory, categoryName));
	}

	/**
	 * Renders the page for adding a new category Only admin access permitted
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategoryView() {
		String name = session("name");
		//Exception handling.
		if(name == null){
			flash("error", "Error has occured, please try again.");
			return redirect("/");
		}
		User u = User.find(name);
		if(u.isAdmin){
			return ok(adminCategoryPanel.render(name));
		}
		return ok(categoryPanel.render(name));
	}

	/**
	 * Shows all categories with edit and delete buttons Only for admin
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result listCategories() {
		String name = session("name");
		List<Category> allCategories = Category.all();
		if (name == null || allCategories == null) {
			flash("error", "Ooops, error has occured. Please try again.");
			return redirect("/");
		}
		if (request().accepts("text/html")) {
			return ok(CategoriesList.render(session("name"), Category.all()));
		}
		return ok(JSonHelper.categoryListToJSon(Category.all()));
	}

	/**
	 * Add a new category in the DB
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result addCategory() {
		if (categoryForm.hasErrors()) {
			flash("error", "Error in form.");
			return redirect("/categoryPanel");
		}
		//Exception handling.
		try{
			String name = categoryForm.bindFromRequest().field("name").value();
			if (name.length() < 4) {
				Logger.info(session("name") + " entered a short category name");
				flash("error", "Name must be at least 4 characters");
				return ok(categoryPanel.render(session("name")));
				
			}
			if (name.length() > 20) {
				Logger.info(session("name") + " entered a too long category name");
				flash("error", "Name must be max 120 characters long");
				return ok(categoryPanel.render(session("name")));
			}
			if (Category.exists(name)) {
				Logger.info(session("name")
						+ " tried to add a existing category. (" + name + ")");
				flash("error", "Category already exists");
				return ok(categoryPanel.render(session("name")));
			}
			/* If no picture is added, a default image is used */
			String picture = FileUpload.imageUpload("category-photos");
			if (picture != null) {
				Category.createCategory(name, picture);
			} else {
				Category.createCategory(name, FileUpload.DEFAULT_IMAGE);
			}
			
			Logger.info(session("name") + " created a new category: \"" + name
					+ "\"");
			flash("success", "Category " + "\"" + name + "\"" + " added");
			return ok(categoryPanel.render(session("name")));			
		}catch(Exception e){
			flash("error", "Error has occured. Please try again later.");
			Logger.error("Error at addCategory: " +e.getMessage(), e);
			return redirect("/");
		}

	}

	/**
	 * Deletes a category and sets the category property for each coupon of that
	 * category to null
	 * 
	 * @param id
	 *            of the category
	 * @return Renders the same page
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deleteCategory(long id) {
		try {
			Category c = Category.find(id);
			List<Coupon> cpns = c.coupons;
			for (Coupon cp : cpns) {
				cp.category = null;
				cp.save();
			}
			c.coupons = null;
			c.save();
			Logger.info(session("name") + " deleted category: \"" + c.name
					+ "\"");
			Category.delete(id);
			return ok(CategoriesList.render(session("name"), Category.all()));
		} catch (Exception e) {
			flash("error",
					"Error occured while deleting category. Please check logs");
			Logger.error("Error while deleting category: " + e.getMessage());
			return redirect("/");

		}
	}

	/**
	 * Shows the edit category view and sets the category properties in the
	 * fields
	 * 
	 * @param name
	 *            of the category
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result editCategoryView(String name) {
		String username = session("name");
		Category category = Category.findByName(name);
		//Exception handling.
		if(username == null || category == null || name == null){
			flash("Ooops, error occured. Please try again later.");
			return redirect("/");
		}
		return ok(editCategory.render(name, category));
	}

	/**
	 * Update category
	 * 
	 * @param id
	 *            of the category
	 * @return render same page
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result updateCategory(long id) {
		try{
			Category category = Category.find(id);
			
			if (categoryForm.hasErrors()) {
				return redirect("/editCategory");
			}
			
			String name = categoryForm.bindFromRequest().field("name").value();
			
			if (name.length() < 4) {
				flash("error", "Name must be at least 4 characters");
				return ok(editCategory.render(session("name"), category));
			}
			
			if (name.length() > 20) {
				flash("error", "Name must be max 120 characters long");
				return ok(editCategory.render(session("name"), category));
			}
			
			category.name = name;
			String picture = FileUpload.imageUpload("category-photos");
			
			if (picture != null) {
				category.picture = picture;
			}
			
			category.save();
			Logger.info(session("name") + " updated category \"" + category.name
					+ "\"");
			flash("success", "Category " + "\"" + name + "\"" + " updated");
			return ok(editCategory.render(session("name"), category));			
		}catch(Exception e){
			flash("Error has occured, please try again later.");
			Logger.error("Error at updateCategory: " +e.getMessage(), e);
			return redirect("/");
		}

	}

}
