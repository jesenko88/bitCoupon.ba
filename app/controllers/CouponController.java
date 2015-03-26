package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.h2.util.StringUtils;

import com.google.common.io.Files;

import models.Category;
import models.Coupon;
import models.Photo;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Security;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.Logger;
import views.html.coupon.*;
import views.html.*;
import views.html.admin.users.*;

public class CouponController extends Controller {

	static Form<Coupon> couponForm = new Form<Coupon>(Coupon.class);
	static List<Category> allCategories = Category.all();
	
	/**
	 * 
	 * @return renders the view for coupon add form
	 */
	public static Result addCouponView() {
		List<Category> categories = Category.all();
		return ok(couponPanel.render(session("name"), categories));
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
		List<Category> categories = Category.all();
		return ok(updateCouponView.render(session("name"), coupon, categories, Photo.photosByCoupon(coupon)));

	}

	/**
	 * Update coupon Method receives an id, finds the specific coupon and
	 * renders the update View for the coupon. If any error occurs, the view is
	 * rendered again.
	 * 
	 * @param id
	 *            long
	 * @return Result render the coupon update view
	 */
	public static Result updateCoupon(long id) {

		Coupon coupon = Coupon.find(id);
		if (couponForm.hasErrors()) {
			Logger.info("Coupon updated");
			return redirect("/");
		}

		// TODO handle invalid inputs
		List<Category> categories = Category.all();
		List<Photo> photos = Photo.photosByCoupon(coupon);
		coupon.name = couponForm.bindFromRequest().field("name").value();
		if (coupon.name.length() < 4) {
			flash("error", "Name must be minimal 4 characters long");
			return ok(updateCouponView.render(session("name"), coupon,
					categories, photos ));
		}
		if (coupon.name.length() > 120) {
			flash("error", "Name must be max 120 characters long");
			return ok(updateCouponView.render(session("name"), coupon,
					categories, photos));
		}
		/* price */
		double price = couponForm.bindFromRequest().get().price;

		if (price <= 0) {
			Logger.info("Invalid price input");
			flash("error", "Enter a valid price");

			return badRequest(updateCouponView.render(session("name"), coupon,
					categories, photos));
		}
		coupon.price = price;
		/* date */
		Date current = new Date();
		Date date = couponForm.bindFromRequest().get().dateExpire;
		if (date != null) {
			if (date.before(current)) {
				flash("error", "Enter a valid expiration date");
				return ok(updateCouponView.render(session("name"), coupon,
						categories, photos));
			}
			coupon.dateExpire = date;
		}
		/* category */
		String newCategory = couponForm.bindFromRequest().field("newCategory")
				.value();
		String category = couponForm.bindFromRequest().field("category")
				.value();
		if (!category.equals("New Category")) {
			coupon.category = Category.findByName(category);
		} else {
			if (newCategory.isEmpty()) {
				flash("error", "Enter new Category name");
				return ok(updateCouponView.render(session("name"), coupon,
						categories, photos));
			}
			coupon.category = Category.find(Category
					.createCategory(newCategory));
		}
		coupon.description = couponForm.bindFromRequest().field("description")
				.value();
		coupon.remark = couponForm.bindFromRequest().field("remark").value();

		/* file upload only if its changed */
		String assetsPath = FileUpload.imageUpload("coupon_photos");
		if (!StringUtils.isNullOrEmpty(assetsPath)) {
			coupon.picture = assetsPath;
		}
		Coupon.updateCoupon(coupon);
		flash("success", "Coupon updated");
		return ok(updateCouponView.render(session("name"), coupon, categories, photos));

	}

	/**
	 * Search method for coupons.
	 * If search is unsuccessful a flash message is sent 
	 * @param string 
	 * @return renders index with matching coupons //TODO render a different view for search result
	 *
	 */
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
		List<Category> categories = Category.all();

		if (name.length() < 4) {
			Logger.info("Entered a short coupon name");
			flash("error", "Name must be 4 characters long");
			return badRequest(couponPanel.render(session("name"), categories));

		}
		if (name.length() > 70) {
			Logger.info("Entered a too long coupon name");
			flash("error", "Name must be max 70 characters long");
			return badRequest(couponPanel.render(session("name"), categories));
		}
		/* price */
		String stringPrice = couponForm.bindFromRequest().field("price")
				.value();
		stringPrice = stringPrice.replace(",", ".");
		double price = couponForm.bindFromRequest().get().price;

		if (price <= 0) {
			Logger.info("Invalid price input");
			flash("error", "Enter a valid price");
			return badRequest(couponPanel.render(session("name"), categories));
		}

		/* date */
		Date current = new Date();
		Date date = couponForm.bindFromRequest().get().dateExpire;
		if (date.before(current)) {
			Logger.info("Invalid date input");
			flash("error", "Enter a valid expiration date");
			return badRequest(couponPanel.render(session("name"), categories));

		}
		/* category */
		Category category = null;
		String newCategory = couponForm.bindFromRequest().field("newCategory")
				.value();
		String categoryy = couponForm.bindFromRequest().field("category")
				.value();
		if (!categoryy.equals("New Category")) {
			category = Category.findByName(categoryy);
		} else {
			if (newCategory.isEmpty()) {
				flash("error", "Enter new Category name");
				return redirect("/couponPanel");
			}
			category = Category.find(Category.createCategory(newCategory));
		}
		String description = couponForm.bindFromRequest().field("description")
				.value();
		String remark = couponForm.bindFromRequest().field("remark").value();

		/*
		 * Managing file upload.
		 */
		// Path for saving file.
		String assetsPath = FileUpload.imageUpload("coupon_photos");
		if (!StringUtils.isNullOrEmpty(assetsPath)) {
			Coupon.createCoupon(name, price, date, assetsPath, category,
					description, remark);
			flash("success", "Coupon successfuly created.");
			return redirect("/couponPanel");
		} else {
			flash("success", "Coupon created without image");
			Coupon.createCoupon(name, price, date, FileUpload.DEFAULT_IMAGE,
					category, description, remark);
			return redirect("/couponPanel");
		}
	}

	/**
	 * Method for uploading coupon photos into coupon gallery. At this moment
	 * its only allowed 4 photos + first one uploaded at creating of coupon.
	 * Gallery photos are not required, this is option admin have at update
	 * coupon panel. Also this method is able to get multiple files from one
	 * request. Each file is being checked by method made in FileUpload class.
	 * 
	 * @param couponId
	 *            - id of coupon we're adding photos add.
	 * @return
	 */
	public static Result galleryUpload(long couponId) {
		/*
		 * Save path where our photos are going to be saved. Each coupon gets
		 * his own folder with name cpn(+ID of coupon)
		 */
		String savePath = "." + File.separator + "public" + File.separator
				+ "images" + File.separator + "coupon_photos" + File.separator
				+ "cpn" + couponId + File.separator;

		Coupon cp = Coupon.find(couponId);
		int photos = Photo.photoStackLength(cp);

		/*
		 * Checking if coupon has fulfilled his stack for photos and if user has
		 * chosen more then available number of photos.
		 */
		if (photos >= 4) {
			flash("error",
					"You already fullfilled this coupons photos. Delete some to add more.");
			return redirect("/editCoupon/" + cp.id);
		}
		MultipartFormData body = request().body().asMultipartFormData();
		List<FilePart> photoParts = body.getFiles();
		if (photoParts.size() > (4 - photos)) {
			flash("error", "You selected " + photoParts.size()
					+ " photos but you can upload only " + (4 - photos)
					+ " more.");
			return redirect("/editCoupon/" + cp.id);
		}

		/*
		 * Once all checks are passed, we create folder for this coupon and add
		 * photos user selected. Also if user uploaded files which are not
		 * photos they're not going to be accepted.
		 */
		new File(savePath).mkdir();
		for (FilePart part : photoParts) {
			if (FileUpload.confirmImage(part) != null) {
				File temp = FileUpload.confirmImage(part);
				String extension = FileUpload.getExtension(part);
				String name = UUID.randomUUID().toString();
				File saveFile = new File(savePath
						+ name  + extension);
				
				//Resizing photos.
				BufferedImage img;
				try {
					img = ImageIO.read(temp);
					BufferedImage resizedImg = FileUpload.resize(img, 600, 400);
					ImageIO.write(resizedImg, "jpg", temp);
				} catch (IOException e1) {
					Logger.error("Failed to resize image");
				}
				
				//Moving file.
				try {
					Files.move(temp, saveFile);
				} catch (IOException e) {
					Logger.error("File " + saveFile.getName()
							+ " failed to move.");
				}
				String assetsPath = "images" + File.separator + "coupon_photos"
						+ File.separator + "cpn" + couponId + File.separator
						+ saveFile.getName();

				Photo.create(assetsPath,saveFile.getPath(), cp);
			}
		}
		flash("success", "Successfully uploaded photos.");	
		Coupon coupon = Coupon.find(couponId);
		return ok(updateCouponView.render(session("name"), coupon, allCategories, Photo.photosByCoupon(coupon)));
	}
	
	/**
	 * Method deletes photo from database and from folder where photo is.
	 * 
	 * @param id of photo
	 * @return returns to editCoupon view.
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deletePhoto(int id){
		Photo temp = Photo.find(id);
		Coupon cp = temp.coupon;		
		File image = new File(temp.savePath);
		
		try{
			image.delete();				
			temp.delete();
			temp.save();
			flash("succes", "You have successfuly deleted photo.");
		}catch(Exception e){
			flash("error", "There was error with deleting file");
			Logger.error("There was error with deleting photo with id: " +id);
		}
		
		return ok(updateCouponView.render(session("name"), cp, allCategories, Photo.photosByCoupon(cp)));
		
	}
	
	
	@Security.Authenticated(AdminFilter.class)
	public static Result listCoupons() {

		return ok(couponsAll.render(session("name"), Coupon.all()));
	}


}
