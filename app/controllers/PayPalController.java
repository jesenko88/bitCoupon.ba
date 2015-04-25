package controllers;

import helpers.MailHelper;

import java.awt.ItemSelectable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.Category;
import models.Company;
import models.Coupon;
import models.SuperUser;
import models.TransactionCP;
import models.User;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;
import views.html.coupon.*;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Details;
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


	static User currentUser; // = User.find(session("name"));
	static Company currentCompany = Company.find(session("name"));
	static SuperUser su;
	static Coupon coupon;
	static List<String> details;
	static APIContext apiContext;
	static PaymentExecution paymentExecution;
	static Payment payment;
	static double totalPrice;
	static int quantity;
	static String paymentID, token;
	static final String CLIENT_ID = Play.application().configuration()
			.getString("cliendID");
	static final String CLIENT_SECRET = Play.application().configuration()
			.getString("cliendSecret");

	/**
	 * Method starts the purchase process. First it collects the data from the
	 * submitted form, builds up a transaction with basic details and
	 * description. Sets the return URL-s for the 'cancel' and 'approve' case.
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
			
			coupon = Coupon.find(Long.parseLong((buyForm.data().get("coupon_id"))));
			currentUser = User.find(Long.parseLong(buyForm.data().get("user_id")));

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
			 * >temporary solution?<
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
			details.add("Quantity: " + Integer.toString(quantity));
			details.add("Total price: " + totalPriceString);

			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);

			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");

			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);
			RedirectUrls redirectUrls = new RedirectUrls();
			redirectUrls.setCancelUrl(Play.application().configuration()
					.getString("cancelURL"));
			redirectUrls.setReturnUrl(Play.application().configuration()
					.getString("returnURL"));
			payment.setRedirectUrls(redirectUrls);

			Payment createdPayment = payment.create(apiContext);

			Iterator<Links> itr = createdPayment.getLinks().iterator();
			while (itr.hasNext()) {
				Links link = itr.next();
				if (link.getRel().equals("approval_url"))

					return redirect(link.getHref());
			}
			Logger.debug(createdPayment.toJSON());

			flash("error", "Something went wrong, please try again later");
			return ok(index.render(Coupon.all(), Category.all()));

		} catch (PayPalRESTException e) {
			flash("error", "Error occured while purchasing through paypal."
					+ " If you're admin please check your logs");
			Logger.error("Error at purchaseProcessing: " + e.getMessage());
			return redirect("/");
		}
	}

	/**
	 * Method is called if the 'approval_url' is returned
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
			
			//su = SuperUser.getSuperUser(currentUser.email);
			flash("info", "Approve transaction");
			return ok(couponResult.render(currentUser, coupon, details));
		} catch (Exception e) {
			flash("error",
					"Error occoured. If you're admin please check your logs.");
			Logger.debug("Error at couponSucess: " + e.getMessage(), e);
			return redirect("/");
		}
	}

	/**
	 * Method renders the couponTemplate again and sends a flash notification
	 * message if the transaction is canceled
	 * 
	 * @return
	 */
	public static Result couponFail() {
		flash("error", "Transaction canceled");
		return badRequest(coupontemplate.render(coupon));
	}

	/**
	 * Method is called after the PayPal process is successful and if the user
	 * approves the transaction on the couponResult page. It executes the
	 * payment and finalizes the transaction.
	 * 
	 * @return render index page with a flash message
	 */
	public static Result approveTransaction() {

		try {
			Payment response = payment.execute(apiContext, paymentExecution);
			
			TransactionCP.createTransaction(paymentID, coupon.price, quantity,
					totalPrice, response.getTransactions().get(0).getRelatedResources().get(0).getSale().getId(), currentUser, coupon);
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
			Logger.info(session("name") + " approved transaction: //TODO");
			flash("success", "Transaction complete");
			return ok(index.render(Coupon.all(), Category.all()));

		} catch (PayPalRESTException e) {
			flash("error", "Error occured while approving transaction. "
					+ "If you're admin please check your logs.");
			Logger.debug("Error at approveTransaction: " + e.getMessage() + e);
			return redirect("/");
		}
	}

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
				sale.setId(transactions.get(i).token);
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

			flash("success", "All buyers of this coupon are successfully refunded!");
			return ok(index.render(Coupon.all(), Category.all()));

		} catch (PayPalRESTException e) {
			flash("error", "Error occured while purchasing through paypal."
					+ " If you're admin please check your logs");
			Logger.error("Error at purchaseProcessing: " + e.getMessage());
			return redirect("/");
		}
	}
}
