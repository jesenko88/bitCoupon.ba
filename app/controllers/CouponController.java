package controllers;

import helpers.FileUpload;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.h2.util.StringUtils;

import com.google.common.io.Files;

import models.Category;
import models.Coupon;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.Logger;
import views.html.coupon.*;
import views.html.*;

public class CouponController extends Controller {

	static Form<Coupon> couponForm = new Form<Coupon>(Coupon.class);

	/**
	 * 
	 * @return renders the view for coupon add form
	 */
	public static Result addCouponView() {

		return ok(couponPanel.render(session("name")));
	}
	

	/**
	 * Finds coupon using id and shows it
	 * 
	 * @param id
	 *            - Coupon id
	 * @return redirect to the Coupon view
	 */
	public static Result showCoupon(long id) {
		Coupon current = Coupon.find(id);
		User cUser = User.find(session("name"));
		return ok(coupontemplate.render(session("name"), cUser, current));
	}

	/**
	 * Delete coupon using id
	 * 
	 * @param id
	 *            - Coupon id (long)
	 * @return redirect to index after delete
	 */
	public static Result deleteCoupon(long id) {
		Coupon.delete(id);
		Logger.info("coupon deleted");
		return redirect("/");
	}

	/**
	 * Renders the view of a coupon. Method receives the id of the coupon and
	 * finds the coupon by id and send's the coupon to the view.
	 * 
	 * @param id
	 *            long
	 * @return Result render couponView
	 */
	public static Result editCoupon(long id) {
		Coupon coupon = Coupon.find(id);

		return ok(updateCouponView.render(session("name"), coupon));

	}

	/**
	 * Update coupon Method receives an id, finds the specific coupon and
	 * renders the update View for the coupon. If any error occurs, the view is
	 * rendered repeatedly.
	 * 
	 * @param id
	 *            long
	 * @return Result render the coupon update view
	 */
	public static Result updateCoupon(long id) {
		
		Coupon coupon=Coupon.find(id);	
		if (couponForm.hasErrors()) {
			Logger.info("Coupon updated");
			return redirect("/");
		}

		// TODO handle invalid inputs

		coupon.name = couponForm.bindFromRequest().field("name").value();
		if (coupon.name.length() < 4) {
			flash("error","Name must be minimal 4 characters long");
			return ok(updateCouponView.render(session("name"),coupon));
			}
		if(coupon.name.length() > 120){
			flash("error", "Name must be max 120 characters long");
			return ok(updateCouponView.render(session("name"),coupon));
		}

		String strPrice = couponForm.bindFromRequest().field("price").value();
		strPrice = strPrice.replace(",", ".");
		double price;
		try{
			price = Double.valueOf(strPrice);
			if ( price <= 0){
				Logger.info("Invalid price input");
				flash("error","Enter a valid price");
				return badRequest(updateCouponView.render(session("name"), coupon));
			}
			
		} catch (NumberFormatException e){

			//TODO logger
			flash("error","Enter a valid price");
			return ok(updateCouponView.render(session("name"), coupon));

		}
		coupon.price =  price;
		Date current = new Date();
		Date date = couponForm.bindFromRequest().get().dateExpire;
		if (date != null){  
			if ( date.before(current)){
				flash("error", "Enter a valid expiration date");
				return ok(updateCouponView.render(session("name"), coupon));
			}
			coupon.dateExpire = date;
		}
	
		coupon.category = Category.findByName(couponForm.bindFromRequest().field("category").value());
		coupon.description = couponForm.bindFromRequest().field("description").value();
		coupon.remark = couponForm.bindFromRequest().field("remark").value();
	
		/* file upload only if its changed */
		String assetsPath = FileUpload.imageUpload("coupon_photos");
		if(!StringUtils.isNullOrEmpty(assetsPath)){
			coupon.picture = assetsPath;
		}
		Coupon.updateCoupon(coupon);
		flash("success", "Coupon updated");
		return ok(updateCouponView.render(session("name"), coupon));

	}

	public static Result search(String q) {
		List<Coupon> coupons = Coupon.find.where().ilike("name", "%" + q + "%")
				.findList();

		if (coupons.isEmpty()) {
			flash("error", "No such coupon");
			User u = User.find(session("name"));
			return badRequest(index.render(u, Coupon.all()));
		}

		return ok(index.render(null, coupons));
	}
	
	
	/**
	 * First checks if the coupon form has errors. Creates a new coupon or
	 * renders the view again if any error occurs.
	 * 
	 * @return redirect to create coupon view
	 * @throws ParseException
	 */
	public static Result addCoupon() {
		
		if (couponForm.hasErrors()) {
			Logger.debug("Error adding coupon");
			return redirect("/couponPanel");
		}
		
		/* name */
		String name = couponForm.bindFromRequest().field("name").value();
		if (name.length() < 4) {
			Logger.info("Entered a short coupon name");
			flash("error", "Name must be 4 characters long");
			return badRequest(couponPanel.render(session("name")));

		}
		if (name.length() > 70) {
			Logger.info("Entered a too long coupon name");
			flash("error", "Name must be max 70 characters long");
			return badRequest(couponPanel.render(session("name")));
		}
		/* price */
		String stringPrice = couponForm.bindFromRequest().field("price").value();
		stringPrice = stringPrice.replace(",", ".");
		double price;
		try {
			price = Double.valueOf(stringPrice);
				if (price <= 0) {
				Logger.info("Invalid price input");
				flash("error", "Enter a valid price");
				return badRequest(couponPanel.render(session("name")));
			}
		} catch (NumberFormatException | IllegalStateException e) {
			Logger.error(e.getMessage(), "Invalid price input");
			flash("error", "Enter a valid price");
			return badRequest(couponPanel.render(session("name")));
		}
		/* date */
		Date current = new Date();
		Date date = couponForm.bindFromRequest().get().dateExpire;
		if (date.before(current)) {
			Logger.info("Invalid date input");
			flash("error", "Enter a valid expiration date");
			return badRequest(couponPanel.render(session("name")));

		}
		/* category */
		Category category = null;
		String categoryName = couponForm.bindFromRequest().field("category")
				.value();
		if (!Category.exists(categoryName)) {
			category = Category.find(Category.createCategory(categoryName));
		} else {
			category = Category.findByName(categoryName);
		}
		String description = couponForm.bindFromRequest().field("description")
				.value();
		String remark = couponForm.bindFromRequest().field("remark").value();

		/*
		 * Managing file upload.
		 */
		// Path for saving file.
		String assetsPath = FileUpload.imageUpload("coupon_photos");
		if(!StringUtils.isNullOrEmpty(assetsPath)){
			Coupon.createCoupon(name, price, date, assetsPath, category,
				description, remark);
			flash("success", "Coupon successfuly created.");
			return redirect("/couponPanel");
		}else{
			flash("success", "Coupon created without image");
			Coupon.createCoupon(name, price, date, FileUpload.DEFAULT_IMAGE, category,
					description, remark);
			return redirect("/couponPanel");
		}
	}


}
