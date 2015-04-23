package controllers;

import helpers.AdminFilter;
import helpers.FileUpload;
import helpers.HashHelper;
import helpers.SuperUserFilter;

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

import api.JSonHelper;

import com.google.common.io.Files;

import models.Category;
import models.comments.Comment;
import models.questions.Question;
import models.Company;
import models.Coupon;
import models.Photo;
import models.SuperUser;
import models.TransactionCP;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Security;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import views.html.coupon.*;
import views.html.company.*;
import views.html.*;
import views.html.admin.users.*;

public class CouponController extends Controller {

	static Form<Coupon> couponForm = new Form<Coupon>(Coupon.class);
	static List<Category> allCategories = Category.all();
	static Result result;
	/**
	 * 
	 * @return renders the view for coupon add form
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result addCouponView() {
		List<Category> categories = Category.all();

		String name = session("name");
		//Exception handling.
		if(categories == null || name == null){
			flash("error", "Ooops, error has occured. Please try again later.");
			return redirect("/");
		}
		
		User user = User.find(name);
		if(user != null){ 
			if(user.isAdmin){
				return ok(adminCouponPanel.render(name, categories, new Form<Coupon>(Coupon.class)));
			}
		}

		return ok(couponPanel.render(session("name"), categories, new Form<Coupon>(Coupon.class)));

	}

	/**
	 * Finds coupon using id and shows it
	 * 
	 * @param id  - Coupon id
	 * @return redirect to the Coupon view
	 */
	public static Result showCoupon(long id) {
		if(Sesija.companyCheck(ctx()) == true)
			TransactionCP.allFromCompany(Sesija.getCurrentCompany(ctx()).id).clear();
		Coupon coupon = Coupon.find(id);
		if(coupon == null ){
			Logger.error("error", "Coupon null at showCoupon()");
			flash("error", "Ooops, error has occured.");
			return redirect("/");
		}
		coupon.statistic.visited();;
			return ok(coupontemplate.render(coupon));
	}

	/**
	 * Delete coupon using id
	 * 
	 * @param id
	 *            - Coupon id (long)
	 * @return redirect to index after delete
	 */
	// TODO admin filter after determining the company rights
	public static Result deleteCoupon(long id) {
		try {
			Coupon coupon = Coupon.find(id);
			//Check if coupon can be deleted.
			//Only companies have this check.
			if(Sesija.getCurrent(ctx()).isCompany()){
				if(!coupon.isDeletable()){
					Logger.info("Company " +session("name") 
							+" tried to delete coupon which cannot"
							+ " be deleted");
					flash("error", "This coupon cannot be deleted."
							+ " Please contact admin for more info.");
					return showCoupon(id);
				}					
			}
			Logger.info(session("name") + " deleted coupon: \"" + coupon.name + "\"");			
			Coupon.delete(id);
			return redirect("/");
		} catch (Exception e) {
			flash("error",
					"Error occured while deleting coupon. Please check logs");
			Logger.error("Error at delete coupon: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Renders the view of a coupon. Method receives the id of the coupon and
	 * finds the coupon by id and send's the coupon to the view.
	 * 
	 * @param id
	 *            long
	 * @return Result render couponView
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result editCoupon(long id) {
		Coupon coupon = Coupon.find(id);
		List<Category> categories = Category.all();
		String name = session("name");
		//Exception handling.
		if(coupon == null || categories == null || name == null){
			flash("error", "Oops, error has occured. Please try again later");
			return redirect("/");
		}
		List<Photo> photosByCoupon = Photo.photosByCoupon(coupon);
		if(photosByCoupon == null){
			flash("error", "Ooops, error has occured. Please try again later.");
			return redirect("/");
		}
		return ok(updateCouponView.render(name, coupon, photosByCoupon));

	}

	/**
	 * This method is used only by admin/s and it approves coupons which has
	 * been added by company
	 * 
	 * @param id
	 *            of coupon
	 * @return redirect to coupon panel
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result approveCoupon(long id) {
		try {
			Coupon coupon = Coupon.find(id);
			coupon.status = Coupon.Status.ACTIVE;
			coupon.save();
			flash("succes", "Coupon " + coupon.name + " has been approved");
			return ok(couponsAll.render(Coupon.approvedCoupons(),
					Coupon.nonApprovedCoupons()));
		} catch (Exception e) {
			flash("Error occured while approving coupon. Please check your logs");
			Logger.error("Error at approveCoupon: " + e.getMessage());
			return redirect("/");
		}
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
	@Security.Authenticated(SuperUserFilter.class)
	public static Result updateCoupon(long id) {
		DynamicForm updateCouponForm = Form.form().bindFromRequest();
		try {
			Coupon coupon = Coupon.find(id);
			if (couponForm.hasErrors()) {
				Logger.info("Coupon update error");
				return redirect("/");
			}

			List<Photo> photos = Photo.photosByCoupon(coupon);
			coupon.name = couponForm.bindFromRequest().field("name").value();
			if (coupon.name.length() < 4) {
				Logger.info(session("name")+ "entered a short coupon name in coupon update");
				return ok(updateCouponView.render(session("name"), coupon, photos));
			}
			if (coupon.name.length() > 120) {
				Logger.info(session("name") + "entered a too long coupon name in coupon update");
				return ok(updateCouponView.render(session("name"), coupon, photos));
			}
			
			/* price */
			String sPrice = updateCouponForm.data().get("price");
			double price = Double.valueOf(sPrice);
			
			if (price <= 0) {
				Logger.info(session("name")	+ " entered a invalid price in coupon update");
				flash("error", "Enter a valid price");
				return badRequest(updateCouponView.render(session("name"),
						coupon, photos));
			}
			coupon.price = price;
			/* date 
			 * FIX FOR DATE BUG.
			 * */
			Date current = new Date();
			String sDate = updateCouponForm.data().get("dateExpire");	
			Date date = null;
			//In case user did not enter date, date will stay still.
			if(sDate.isEmpty()){
				date = coupon.dateExpire;
			}else{
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				date = formatter.parse(sDate);				
			}
			
			if (date != null) {
				if (date.before(current)) {
					flash("error", "Enter a valid expiration date");
					Logger.info(session("name")	+ " entered a invalid date in coupon update");
					return ok(updateCouponView.render(session("name"), coupon,  photos));
			}
				coupon.dateExpire = date;
			}
			/* category */
			String newCategory = couponForm.bindFromRequest().field("newCategory").value();
			String category = couponForm.bindFromRequest().field("category").value();
			if (!category.equals("New Category")) {
				coupon.category = Category.findByName(category);
			} else {
				if (newCategory.isEmpty()) {
					flash("error", "Enter new Category name");
					return ok(updateCouponView.render(session("name"), coupon, photos));
				}
				coupon.category = Category.find(Category.createCategory(newCategory));
			}
			coupon.description = couponForm.bindFromRequest().field("description").value();
			coupon.remark = couponForm.bindFromRequest().field("remark").value();
			coupon.minOrder = Integer.valueOf(couponForm.bindFromRequest().field("minOrder").value());
			/* file upload only if its changed */
			String assetsPath = FileUpload.imageUpload("coupon_photos");
			if (!StringUtils.isNullOrEmpty(assetsPath)) {
				coupon.picture = assetsPath;
			}
			int status;
			if (Sesija.adminCheck(ctx()) == true) {
				status = Coupon.Status.ACTIVE;
			} else {
				status = Coupon.Status.DEFAULT;
			}
			coupon.status = status;
			
			Coupon.updateCoupon(coupon);
			Logger.info(session("name") + " updated coupon: " + coupon.id);
			flash("success", "Coupon updated");
			return ok(updateCouponView.render(session("name"), coupon, photos));
		} catch (Exception e) {
			flash("error", "Error while updating coupon. If you're admin please check logs");
			Logger.error("Error at updateCoupon: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Search method for coupons. If search is unsuccessful a flash message is
	 * sent
	 * 
	 * @param string
	 * @return renders index with matching coupons //TODO render a different
	 *         view for search result
	 */
	public static Result search(String q) {
		List<Coupon> all = Coupon.find.where().ilike("name", "%" + q + "%")
				.findList();
		//Getting only activated coupons from search result.
		List<Coupon> coupons = new ArrayList<Coupon>();
		for(Coupon coupon: all){
			if(coupon.status == Coupon.Status.ACTIVE){
				coupons.add(coupon);
			}
		}	
		if (coupons.isEmpty()) {
			flash("error", "No such coupon");		
			return badRequest(searchFilter.render(Coupon.approvedCoupons()
													, Category.all()));
		}
		Logger.info(session("name") + " searched for: \"" + q + "\"");
		return ok(searchFilter.render(coupons, Category.all()));
	}
	/**
	 * Method for sorting coupon result. Can sort list of all coupons on index,
	 * or just searched result of coupons.
	 * 
	 * @param ids
	 * @return
	 */
	public static Result sort(String ids) {
		try {			
			/*
			 * Getting all ids of coupons and adding them to list we are going
			 * to sort.
			 */
			String[] couponIds = ids.split(",");
			List<Coupon> coupons = new ArrayList<Coupon>();
			for (String id : couponIds) {
				long currentID = Long.valueOf(id);
				Coupon currentCoupon = Coupon.find(currentID);
				coupons.add(currentCoupon);
			}

			DynamicForm sortForm = Form.form().bindFromRequest();
			String orderBy = sortForm.data().get("orderby");

			// Getting sort method.
			String parseMethod = sortForm.data().get("method");
			int method = 0;
			if (parseMethod.equalsIgnoreCase("ascending")) {
				method = Coupon.SORT_ASCENDING;
			} else if (parseMethod.equalsIgnoreCase("descending")) {
				method = Coupon.SORT_DESCENDING;
			} else {
				Logger.debug("Method went wrong");
			}

			List<Coupon> sorted;

			if (orderBy.equalsIgnoreCase("Category")) {
				sorted = Coupon.sortByCategory(coupons, method);
			} else if (orderBy.equalsIgnoreCase("Price")) {
				sorted = Coupon.sortByPrice(coupons, method);
			} else if (orderBy.equalsIgnoreCase("Date")) {
				sorted = Coupon.sortByDate(coupons, method);
			} else {
				Logger.error("Wrong orderby type");
				flash("warning", "Something went wrong, try again.");
				return redirect("/");
			}
			return ok(index.render(sorted, Category.all()));
		} catch (Exception e) {
			flash("error",
					"Error occured while sorting results. If you're admin please check logs.");
			Logger.error("Error at sort: " + e.getMessage());
			return redirect("/");
		}
	}

	// TODO move this method in coupon model.
	public static boolean isExpired(long id) {
		Date current = new Date();
		Date expDate = Coupon.find(id).dateExpire;

		return current.after(expDate);
	}

	/**
	 * First checks if the coupon form has errors. Creates a new coupon or
	 * renders the view again if any error occurs.
	 * 
	 * @return redirect to create coupon view
	 * @throws ParseException
	 */
	@Security.Authenticated(SuperUserFilter.class)
	public static Result addCoupon() {

		Form<Coupon> addCouponForm = Form.form(Coupon.class).bindFromRequest();
		List<Category> categories = Category.all();	
		String name = couponForm.bindFromRequest().field("name").value();

		if (Sesija.companyCheck(ctx())){		
			result = ok(couponPanel.render(session("name"), categories, addCouponForm));
		}else{
			result = ok(adminCouponPanel.render(session("name"), categories, addCouponForm));
		}
	
		if (couponForm.hasErrors() || addCouponForm.hasGlobalErrors()) {
			Logger.debug("Error adding coupon");
			return result;
		}

		try{		
			double price = couponForm.bindFromRequest().get().price;	
			Date date = couponForm.bindFromRequest().get().dateExpire;			
			String newCategory = couponForm.bindFromRequest().field("newCategory").value();
			String categoryString = couponForm.bindFromRequest().field("category").value();
			String description = couponForm.bindFromRequest().field("description").value();
			String remark = couponForm.bindFromRequest().field("remark").value();
			int minOrder = Integer.valueOf(couponForm.bindFromRequest().field("minOrder").value());	
			int maxOrder = Integer.valueOf(couponForm.bindFromRequest().field("maxOrder").value());
			Date usage = couponForm.bindFromRequest().get().usage;
			
			Category category = Category.findByName(categoryString);
			
			int status = Coupon.Status.DEFAULT;
			if (Sesija.adminCheck(ctx())) {
				status = Coupon.Status.ACTIVE;
			}	
			//In case admin posted coupon.
			Company company = Company.find(session("name"));
			if(company == null){
				company = CompanyController.COMPANY_ADMIN;
			}
			/*
			 * Managing file upload.
			 */
			// Path for saving file.
			String assetsPath = FileUpload.imageUpload("coupon_photos");
			if (!StringUtils.isNullOrEmpty(assetsPath)) {
				long id = Coupon.createCoupon(name, price, date, assetsPath,
						category, description, remark, minOrder, maxOrder, usage, company, status);
				Logger.info(session("name") + " created coupon " + id);
				flash("success", "Coupon successfuly created.");
				return redirect("/couponPanel");
			} else {
				//In case user didn't upload photo of coupon
				//we add default photo of this category for this coupon.
				flash("success", "Coupon created without image");
				long id = Coupon.createCoupon(name, price, date,
						category.picture,category, description, remark, minOrder, maxOrder, usage, company, status);
				Logger.info(session("name") + " created coupon " + id
						+ " without image");
				return redirect("/couponPanel");
			}
		}catch(Exception e){
		flash("error", "Error occured while adding coupon. If you're admin please check logs.");
		Logger.error("Error att addCoupon: " + e.getMessage(), e);
		return result;
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
	@Security.Authenticated(SuperUserFilter.class)
	public static Result galleryUpload(long couponId) {
		try {
			/*
			 * Save path where our photos are going to be saved. Each coupon
			 * gets his own folder with name cpn(+ID of coupon)
			 */
			String savePath = FileUpload.IMAGES_FOLDER + "coupon_photos"
					+ File.separator + "cpn" + couponId + File.separator;

			Coupon coupon = Coupon.find(couponId);
			int photos = Photo.photoStackLength(coupon);

			/*
			 * Checking if coupon has fulfilled his stack for photos and if user
			 * has chosen more then available number of photos.
			 */
			if (photos >= 4) {
				Logger.info(session("name") + " tried to add too many photos");
				flash("error",
						"You already fullfilled this coupons photos. Delete some to add more.");
				return redirect("/editCoupon/" + coupon.id);
			}
			MultipartFormData body = request().body().asMultipartFormData();
			List<FilePart> photoParts = body.getFiles();
			if (photoParts.size() > (4 - photos)) {
				flash("error", "You selected " + photoParts.size()
						+ " photos but you can upload only " + (4 - photos)
						+ " more.");
				return redirect("/editCoupon/" + coupon.id);
			}

			/*
			 * Once all checks are passed, we create folder for this coupon and
			 * add photos user selected. Also if user uploaded files which are
			 * not photos they're not going to be accepted.
			 */
			new File(savePath).mkdir();
			for (FilePart part : photoParts) {
				if (FileUpload.confirmImage(part) != null) {
					File temp = FileUpload.confirmImage(part);
					String extension = FileUpload.getExtension(part);
					String name = UUID.randomUUID().toString();
					File saveFile = new File(savePath + name + extension);

					// Resizing photos.
					BufferedImage img;
					try {
						img = ImageIO.read(temp);
						BufferedImage resizedImg = FileUpload.resize(img, 600,
								400);
						ImageIO.write(resizedImg, "jpg", temp);
					} catch (IOException e1) {
						Logger.error("Failed to resize image");
					}

					// Moving file.
					try {
						Files.move(temp, saveFile);
					} catch (IOException e) {
						Logger.error("File " + saveFile.getName()
								+ " failed to move.");
					}
					String assetsPath = "images" + File.separator
							+ "coupon_photos" + File.separator + "cpn"
							+ couponId + File.separator + saveFile.getName();

					Photo.create(assetsPath, saveFile.getPath(), coupon);
				}
			}
			flash("success", "Successfully uploaded photos.");			
			return ok(updateCouponView.render(session("name"), coupon, Photo.photosByCoupon(coupon)));
		} catch (Exception e) {
			flash("error", "Error occured while uploading gallery photos."
					+ " If you're admin please check logs.");
			Logger.error("Error at galleryUpload: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Method deletes photo from database and from folder where photo is.
	 * 
	 * @param id
	 *            of photo
	 * @return returns to editCoupon view.
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result deletePhoto(int id) {		
		try {
			Photo photo = Photo.find(id);
			Coupon coupon = photo.coupon;
			File image = new File(photo.savePath);
			image.delete();
			photo.delete();
			photo.save();
			Logger.info(session("name") + " deleted image id:\"" + id
					+ "\" from " + coupon.name);
			flash("succes", "You have successfuly deleted photo.");
			return redirect("/editCoupon/" + coupon.id);
		} catch (Exception e) {
			flash("error",
					"Error occured while deleting photo. If you're admin please check loggs.");
			Logger.error("Error at deletePhoto " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Returns all searched coupons on view searchFilter
	 * 
	 * @return
	 */
	public static Result searchPage() {
		List<Coupon> coupons = Coupon.all();
		List<Category> categories = Category.all();	
		//Handling exceptions.
		if(coupons == null || categories == null ){
			flash("error", "Ooops, error has occured. Plealse try again later.");
			return redirect("/");
		}
		return ok(searchFilter.render(coupons, categories));

	}
	
	/**
	 * This method get string, and convert string to list of Coupons, Then
	 * check if any of coupons is in category that user chose and return new
	 * list of coupons
	 * 
	 * @param ids
	 *            (list of ids that user searched)
	 * @return new filtered list of coupons
	 */
	public static Result filterCategory(String ids) {
		try {
			/*
			 * Getting all ids of coupons and adding them to list we are going
			 * to sort.
			 */
			String[] couponIds = ids.split(",");
			List<Coupon> coupons = new ArrayList<Coupon>();
			for (String id : couponIds) {
				long currentID = Long.valueOf(id);
				Coupon currentCoupon = Coupon.find(currentID);
				coupons.add(currentCoupon);
			}

			DynamicForm filterForm = Form.form().bindFromRequest();
			String categoryChosed = filterForm.data().get("category");
			List<Coupon> list = new ArrayList<Coupon>();

			for (Coupon coupon : coupons) {
				if (coupon.category.name.equalsIgnoreCase(categoryChosed)) {
					list.add(coupon);
				}
			}
			if (list.isEmpty()) {
				flash("error", "No new result");
				return ok(index.render(Coupon.all(), Category.all()));
			}

			return ok(index.render(list, Category.all()));
		} catch (Exception e) {
			flash("error", "Error occured while filtrering categories."
					+ "If you're admin please check your logs.");
			Logger.error("Error at filterCategory: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * This methode get string, and convert string to list of Coupons, Then
	 * check if any of coupons is between prices that User chose and return new
	 * list of coupons
	 * 
	 * @param ids
	 *            (list of ids that user searched)
	 * @return new filtered list of coupons
	 */
	public static Result filterPrice(String ids) {
		try {
			/*
			 * Getting all ids of coupons and adding them to list we are going
			 * to sort.
			 */
			String[] couponIds = ids.split(",");
			List<Coupon> coupons = new ArrayList<Coupon>();
			for (String id : couponIds) {
				long currentID = Long.valueOf(id);
				Coupon currentCoupon = Coupon.find(currentID);
				coupons.add(currentCoupon);
			}

			DynamicForm filterForm = Form.form().bindFromRequest();
			List<Coupon> list = new ArrayList<Coupon>();

			// PRICE Filter
			String startP = filterForm.data().get("start_price");
			String endP = filterForm.data().get("end_price");
			double startPrice = 0;
			double endPrice = Double.MAX_VALUE;

			// In case one of price bind from request are null.
			if (!startP.isEmpty()) {
				startPrice = Double.valueOf(startP);
			}
			if (!endP.isEmpty()) {
				endPrice = Double.valueOf(endP);
			}

			if (startPrice > endPrice) {
				flash("error", "Please input correct values");
				return ok(index.render(Coupon.all(), Category.all()));
			}

			for (Coupon coupon : coupons) {
				if (coupon.price >= startPrice && coupon.price <= endPrice) {
					list.add(coupon);
				}
			}

			if (list.isEmpty()) {
				flash("error", "No new result");
				return ok(index.render(Coupon.all(), Category.all()));
			}

			return ok(index.render(list, Category.all()));
		} catch (Exception e) {
			flash("error", "Error occured while filtrering by price."
					+ "If you're admin please check your logs.");
			Logger.error("Error at filterPrice: " + e.getMessage());
			return redirect("/");
		}

	}

	/**
	 * This method get string, and convert string to list of Coupons, Then check
	 * if any of coupons is before the date that user chosed and return new list
	 * of coupons
	 * 
	 * @param ids
	 *            (list of ids that user searched)
	 * @return new filtered list of coupons
	 */
	public static Result filterDate(String ids) {
		try {
			/*
			 * Getting all ids of coupons and adding them to list we are going
			 * to sort.
			 */
			String[] couponIds = ids.split(",");
			List<Coupon> coupons = new ArrayList<Coupon>();
			for (String id : couponIds) {
				long currentID = Long.valueOf(id);
				Coupon currentCoupon = Coupon.find(currentID);
				coupons.add(currentCoupon);
			}

			List<Coupon> list = new ArrayList<Coupon>();

			// Date Filter
			Date date = couponForm.bindFromRequest().get().dateExpire;
			Date current = new Date();

			if (date == null) {
				flash("error", "Enter a valid expiration date");
				return badRequest(index.render(list, Category.all()));
			}

			if (date.before(current)) {
				Logger.info("entered a invalid date");
				flash("error", "Enter a valid expiration date");
				return badRequest(index.render(list, Category.all()));
			}

			for (Coupon coupon : coupons) {
				Date couponDate = coupon.dateExpire;
				if (couponDate.before(date)) {
					list.add(coupon);
				}
			}

			if (list.isEmpty()) {
				flash("error", "No new result");
				return ok(index.render(Coupon.all(), Category.all()));
			}

			return ok(index.render(list, Category.all()));

		} catch (Exception e) {
			flash("error", "Error occured while filtrering by date."
					+ "If you're admin please check your logs.");
			Logger.error("Error at filterDate: " + e.getMessage());
			return redirect("/");
		}

	}

	/**
	 * Returns all non expired coupons to view couponsAll
	 * 
	 * @return list of non expired coupons
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result listCoupons() {
		List<Coupon> approvedCoupons = Coupon.approvedCoupons();
		List<Coupon> nonApprovedCoupons = Coupon.nonApprovedCoupons();
		// Handling case lists are null.
		if (approvedCoupons == null)
			approvedCoupons = new ArrayList<Coupon>();
		if (nonApprovedCoupons == null)
			nonApprovedCoupons = new ArrayList<Coupon>();
		
		if (request().accepts("text/html")) {
			return ok(couponsAll.render(approvedCoupons, nonApprovedCoupons));
		}
		return ok(JSonHelper.couponListToJson(approvedCoupons));
	}
	
	
	public static int leftToSell(long couponId) {
		Coupon coupon = Coupon.find(couponId);
		int couponsLeft = coupon.maxOrder - coupon.minOrder;
		if (couponsLeft > 0)
			return couponsLeft;
		return 0;
	}
	
	
}
