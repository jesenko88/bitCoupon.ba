package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.util.List;

import models.Category;
import models.Coupon;
import models.User;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.users.adminCategoryPanel;
import views.html.category.CategoriesList;
import views.html.category.categoryPage;
import views.html.category.categoryPanel;
import views.html.category.editCategory;
import api.JSonHelper;

public class CategoryController extends Controller {
	
	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");
	static final String SHORT_NAME = Messages.get("category.shortName");
	static final String LONG_NAME = Messages.get("category.longName");

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
			flash("error", ERROR_MSG_CLIENT);
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
			flash("error", ERROR_MSG_CLIENT);
			return redirect("/");
		}
		User user = User.find(name);
		if(user.isAdmin){
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
			flash("error", ERROR_MSG_CLIENT);
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
		categoryForm = Form.form(Category.class).bindFromRequest();
		if (categoryForm.hasErrors() || categoryForm.hasGlobalErrors()) {
			flash("error", Messages.get("error.form"));
			return addCategoryView();
		}
		//Exception handling.
		try{
			String name = categoryForm.bindFromRequest().field("name").value();
			if (name.length() < 4) {
				Logger.info(session("name") + " entered a short category name");
				flash("error", SHORT_NAME);
				return addCategoryView();				
			}
			if (name.length() > 20) {
				Logger.info(session("name") + " entered a too long category name");
				flash("error", LONG_NAME);
				return addCategoryView();
			}
			if (Category.exists(name)) {
				Logger.info(session("name")
						+ " tried to add a existing category. (" + name + ")");
				flash("error", Messages.get("category.alreadyExists"));
				return addCategoryView();
			}
			/* If no picture is added, a default image is used */
			String picture = FileUpload.imageUpload();
			if (picture != null) {
				Category.createCategory(name, picture);
			} else {
				Category.createCategory(name, ""); //TODO default category image url
			}
			
			Logger.info(session("name") + " created a new category: \"" + name + "\"");
			flash("success", Messages.get("category.Added", name));
			Logger.debug( Messages.get("category.Added", name));
			return addCategoryView();			
		}catch(Exception e){
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at addCategory: " +e.getMessage(), e);
			return addCategoryView();
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
			Category category = Category.find(id);
			List<Coupon> coupons = category.coupons;
			for (Coupon coupon : coupons) {
				coupon.category = null;
				coupon.save();
			}
			category.coupons = null;
			category.save();
			Logger.info(session("name") + " deleted category: \"" + category.name
					+ "\"");
			Category.delete(id);
			flash("success", Messages.get("delete.success"));
			return ok(CategoriesList.render(session("name"), Category.all()));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
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
			flash("error", ERROR_MSG_CLIENT);
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
				flash("error", ERROR_MSG_CLIENT);
				return redirect("/editCategory");
			}
			
			String name = categoryForm.bindFromRequest().field("name").value();
			
			if (name.length() < 4) {
				flash("error", SHORT_NAME);
				return ok(editCategory.render(session("name"), category));
			}
			
			if (name.length() > 20) {
				flash("error", LONG_NAME);
				return ok(editCategory.render(session("name"), category));
			}
			
			category.name = name;
			String picture = FileUpload.imageUpload();
			
			if (picture != null) {
				category.picture = picture;
			}
			
			category.save();
			Logger.info(session("name") + " updated category \"" + category.name
					+ "\"");
			flash("success", Messages.get("category.Updated", name));
			return ok(editCategory.render(session("name"), category));			
		}catch(Exception e){
			flash("error", ERROR_MSG_ADMIN);
			Logger.error("Error at updateCategory: " +e.getMessage(), e);
			return redirect("/");
		}

	}

}
