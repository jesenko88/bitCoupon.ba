package controllers;

import helpers.MailHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Coupon;
import models.TransactionCP;
import models.User;
import nl.bitwalker.useragentutils.UserAgent;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;
import views.html.coupon.*;
import views.html.mobile.*;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Refund;
import com.paypal.api.payments.Sale;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalController extends Controller {

    static String PATH = Play.application().configuration().getString("PATH");
	static final String ERROR_MSG_ADMIN = Messages.get("error.msg.00");
	static final String ERROR_MSG_CLIENT = Messages.get("error.msg.01");

	private static User currentUser; // = User.find(session("name"));
	private static Coupon coupon;
	private static List<String> details;
	private static APIContext apiContext;
	private static PaymentExecution paymentExecution;
	private static Payment payment;
	private static double totalPrice;
	private static int quantity;
	private static String paymentID, token;
	
	private static final String CLIENT_ID = Play.application().configuration()
			.getString("cliendID");
	private static final String CLIENT_SECRET = Play.application().configuration()
			.getString("cliendSecret");
	private static UserAgent userAgent = null;
	private static String deviceType = null;

	/**
	 * Method starts the purchase process. First it collects the data from the
	 * submitted form, builds up a transaction with basic details and
	 * description. Sets the return URL-s for the 'cancel' and 'approve' case.
	 * If the 
	 * 
	 * @return
	 */
	public static Result purchaseProcessing() {

		try {
			String accessToken = new OAuthTokenCredential(CLIENT_ID,
					CLIENT_SECRET).getAccessToken();
			
			Map<String, String> sdkConfig = new HashMap<String, String>();
			sdkConfig.put("mode", "sandbox");
			apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);
			Amount amount = new Amount();	
			
			DynamicForm buyForm = Form.form().bindFromRequest();						
			coupon = Coupon.find(Long.parseLong(buyForm.data().get("coupon_id")));
			long userId = Long.parseLong(buyForm.data().get("user_id"));
			/* if the received userId value is '-1', it means that the purchase is 
			 * started as a gift for someone who doesn't exist in the database
			 * and the form is especially submitted for the recipient details*/
			if (userId == -1){
				String email = buyForm.data().get("email");
				String username = buyForm.data().get("name");
				String surname = buyForm.data().get("surname");
				currentUser = User.createTempUser(email, username, surname);
				currentUser.id = -1;
			}else{
				currentUser = User.find(userId);
			}
			
			quantity = Integer.parseInt(buyForm.data().get("quantity"));
			if (quantity > coupon.maxOrder) {
				flash("info", "There are only " + coupon.maxOrder + " left");
				return ok(coupontemplate.render(coupon));
			}
			
			totalPrice = coupon.price * quantity;	
			String totalPriceString = String.format("%1.2f",totalPrice);
			String couponName = coupon.name;
			if (couponName.length() > 65)
				couponName = couponName.substring(0, 65) + "...";
			
			amount.setTotal(totalPriceString);
			amount.setCurrency("USD");
			/*
			 * Formating description to send to the PayPal checkout page
			 */
			String description = String.format("Coupon: %s\n"
					+ "Price: %s\n"
					+ "Quantity: %d\n"
					+ "Total: %s", couponName, coupon.price, quantity, totalPriceString);
		
			Transaction transaction = new Transaction();
			transaction.setDescription(description);
			transaction.setAmount(amount);

			/* details to render in the success view */
			details = new ArrayList<String>();
			details.add(Messages.get("quantity") + " " + Integer.toString(quantity));
			details.add(Messages.get("priceTotal") + " " + totalPriceString);

			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);

			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");

			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);
			
			RedirectUrls redirectUrls = new RedirectUrls();

			userAgent = UserAgent.parseUserAgentString(Http.Context.current().request().getHeader("User-Agent"));
			deviceType = userAgent.getOperatingSystem().getDeviceType().toString();
			
			/*
			 * Checking the device type in case the purchase is 
			 * started from a web view from a mobile or tablet device
			 */
			if (deviceType.equals("MOBILE") || deviceType.equals("TABLET")){
				redirectUrls.setCancelUrl(Play.application().configuration()
						.getString("APIcancelURL"));
				redirectUrls.setReturnUrl(Play.application().configuration()
						.getString("APIreturnURL"));			
			}else{
				redirectUrls.setCancelUrl(Play.application().configuration()
						.getString("cancelURL"));
				redirectUrls.setReturnUrl(Play.application().configuration()
						.getString("returnURL"));
			}
			payment.setRedirectUrls(redirectUrls);
			Payment createdPayment = payment.create(apiContext);
			
			
			/*Iterating through the url lists received from the paypal response
			 * and checking if we got a approval_url
			 * If a approval url is found, we can redirect the client to the
			 * paypal checkout page*/
			
			Iterator<Links> itr = createdPayment.getLinks().iterator();
			while (itr.hasNext()) {
				Links link = itr.next();
				if (link.getRel().equals("approval_url"))

					return redirect(link.getHref());
			}
			Logger.debug(createdPayment.toJSON());

			flash("error", ERROR_MSG_CLIENT);
			return ok(index.render(Coupon.all(), Category.all()));

		} catch (Exception e) {
			flash("error", ERROR_MSG_CLIENT);
			Logger.error("Error at purchaseProcessing: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * This is the method that is processed if the "success" return url is used,
	 * in other words, if the user continues to approve transaction during the 
	 * paypal checkout
	 * 
	 * @return
	 */
	public static Result couponSuccess() {
		String payerID;
		try {
			DynamicForm paypalReturn = Form.form().bindFromRequest();
			paymentID = paypalReturn.get("paymentId");
			payerID = paypalReturn.get("PayerID");
			token = paypalReturn.get("token");
			String accessToken = new OAuthTokenCredential(CLIENT_ID,
					CLIENT_SECRET).getAccessToken();
			Map<String, String> sdkConfig = new HashMap<String, String>();
			sdkConfig.put("mode", "sandbox");
			apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);
			payment = Payment.get(accessToken, paymentID);
			paymentExecution = new PaymentExecution();
			paymentExecution.setPayerId(payerID);
			
			/*when the payment is built, the client is redirected to the
			 * approval page */
			
			userAgent = UserAgent.parseUserAgentString(Http.Context.current().request().getHeader("User-Agent"));
			deviceType = userAgent.getOperatingSystem().getDeviceType().toString();
			if (deviceType.equals("MOBILE") || deviceType.equals("TABLET")){
				flash("info", Messages.get("transaction.approve"));
				return ok(api_transactionApprove.render(currentUser, coupon, details));	
			}			
			flash("info", Messages.get("transaction.approve"));
			return ok(couponResult.render(currentUser, coupon, details));
		} catch (Exception e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.debug("Error at couponSucess: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Method renders the couponTemplate and shows a flash notification
	 * message if the transaction is canceled during the procedure on the
	 * paypal page. It's called with the "fail" return url
	 * 
	 * @return
	 */
	public static Result couponFail() {
		userAgent = UserAgent.parseUserAgentString(Http.Context.current().request().getHeader("User-Agent"));
		deviceType = userAgent.getOperatingSystem().getDeviceType().toString();
		if (deviceType.equals("MOBILE") || deviceType.equals("TABLET")){
			return redirect("/api/backToMobile");	
		}	
		flash("error", Messages.get("transaction.canceled"));
		return badRequest(coupontemplate.render(coupon));
	}

	/**
	 * Method is if the user approves the transaction on the couponResult page.
	 *  It executes the payment and finalizes the transaction.
	 *  For registered users, the transaction details contains a transacition.buyer
	 *  value, otherwise, if the user is "-1", it means the user is a unregistered user,
	 *  a transaction with name and surname is made and the transaction.buyer is set to null.
	 * 
	 * @return render index page with a flash message
	 */
	public static Result approveTransaction() {
		
		try {
			Payment response = payment.execute(apiContext, paymentExecution);
			String saleId = response.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();
			if(currentUser.id != -1) {
			TransactionCP.createTransaction(paymentID, saleId, coupon.price, quantity,
					totalPrice, token, currentUser, coupon);
			}else{
				TransactionCP.createTransactionForUnregisteredUser(paymentID, saleId, coupon.price, quantity,
						totalPrice, token, currentUser.username, currentUser.surname, currentUser.email, coupon);
			}		
			coupon.statistic.bought(quantity);
			/* decrementing available coupons */
			coupon.maxOrder = coupon.maxOrder - quantity;
			Coupon.updateCoupon(coupon);
			MailHelper.send(coupon.seller.email,
					"Congratulations. Your coupon " + coupon.name
							+ " is purchased. "
							+ "To see details go to your profile. <br>"
							+ "http://" + PATH + "/loginpage");
			
			coupon.seller.notifications ++;
			coupon.seller.save();
			
			userAgent = UserAgent.parseUserAgentString(Http.Context.current().request().getHeader("User-Agent"));
			deviceType = userAgent.getOperatingSystem().getDeviceType().toString();
			if (deviceType.equals("MOBILE") || deviceType.equals("TABLET")){
				return redirect("/api/backToMobile");	
			}	
			flash("success", Messages.get("transaction.complete"));
			return ok(index.render(Coupon.all(), Category.all()));

		} catch (PayPalRESTException e) {
			flash("error", ERROR_MSG_ADMIN);
			Logger.debug("Error at approveTransaction: " + e.getMessage() + e);
			return redirect("/");
		}
	}

	/**
	 * Method for refunding
	 * With this method company or admin refund all buyers of certain coupon
	 * @param couponId - coupon id
	 * @return redirect to the index page
	 */
	public static Result refundProcessing(long couponId) {

		try {
			String accessToken = new OAuthTokenCredential(CLIENT_ID,
					CLIENT_SECRET).getAccessToken();

			Map<String, String> sdkConfig = new HashMap<String, String>();
			List<Map<Sale, Refund>> listOfRefunds = new ArrayList<Map<Sale, Refund>>();
			sdkConfig.put("mode", "sandbox");
			apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);

			coupon = Coupon.find(couponId);
			List<TransactionCP> transactions = TransactionCP.find.where()
					.eq("coupon_id", coupon.id).findList();
			for (int i = 0; i < transactions.size(); i++) {
				System.out.println(transactions.get(i).token);
				totalPrice = transactions.get(i).quantity * coupon.price;
				String totalPriceString = String.format("%1.2f", totalPrice);
				Map<Sale, Refund> refundObject = new HashMap<Sale, Refund>();
				Sale sale = new Sale();
				sale.setId(transactions.get(i).sale_id);
				Refund refund = new Refund();
				Amount amount = new Amount();
				amount.setCurrency("USD");
				amount.setTotal(totalPriceString);
				refund.setAmount(amount);
				refundObject.put(sale, refund);

				listOfRefunds.add(refundObject);

			}
			for (int i = 0; i < listOfRefunds.size(); i++) {
				for (Map.Entry<Sale, Refund> e : listOfRefunds.get(i)
						.entrySet()) {
					Sale sale = e.getKey();
					Refund refund = e.getValue();

					sale.refund(apiContext, refund);
				}
			}
			flash("success", Messages.get("transaction.refund.success"));
			return ok(index.render(Coupon.all(), Category.all()));

		} catch (PayPalRESTException e) {
			flash("error", Messages.get("error.msg.02"));
			Logger.error("Error at purchaseProcessing: " + e.getMessage());
			return redirect("/");
		}
	}
}
