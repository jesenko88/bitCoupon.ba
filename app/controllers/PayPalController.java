package controllers;

import java.awt.ItemSelectable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.Company;
import models.Category;
import models.Coupon;
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
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

public class PayPalController extends Controller {
	
	static User currentUser = User.find(session("name"));
	static Company company = Company.find(session("name"));
	static Coupon coupon;
	static List<String> details;
	static APIContext apiContext;
	static PaymentExecution paymentExecution;
	static Payment payment;
	static double totalPrice;
	static int quantity;
	static String paymentID, token;
	static final String CLIENT_ID = Play.application().configuration().getString("cliendID");
	static final String CLIENT_SECRET = Play.application().configuration().getString("cliendSecret");

	/**
	 * Method starts the purchase process.
	 * First it collects the data from the submitted form, builds up a transaction with
	 * basic details and description. Sets the return URL-s for the 'cancel' and 'approve' case.
	 * @return
	 */
	public static Result purchaseProcessing(){

		try { 
			String accessToken = new OAuthTokenCredential(CLIENT_ID,CLIENT_SECRET).getAccessToken();
			
			Map<String, String> sdkConfig = new HashMap<String, String>();
			sdkConfig.put("mode", "sandbox");			
			apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);
			
			Amount amount = new Amount();		
			DynamicForm buyForm = Form.form().bindFromRequest();		
			coupon = Coupon.find(Long.parseLong((buyForm.data().get("coupon_id"))));
			quantity = Integer.parseInt(buyForm.data().get("quantity"));
			totalPrice = coupon.price * quantity;
			
			String totalPriceString = String.format("%1.2f",totalPrice);			
			amount.setTotal(totalPriceString);
			amount.setCurrency("USD");
			
			/*
			 * Formating description to send to the PayPal checkout page
			 * >temporary solution?<
			 */
			String description = String.format("Coupon: %s\n"
					+ "Price: %s\n"
					+ "Quantity: %d\n"
					+ "Total: %s", coupon.name, coupon.price, quantity, totalPriceString);
					
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
			redirectUrls.setCancelUrl(Play.application().configuration().getString("cancelURL"));		
			redirectUrls.setReturnUrl(Play.application().configuration().getString("returnURL"));
			payment.setRedirectUrls(redirectUrls);
			
			Payment createdPayment = payment.create(apiContext);
			
			Iterator<Links> itr = createdPayment.getLinks().iterator();
			while(itr.hasNext()){
				Links link = itr.next();
				if (link.getRel().equals("approval_url"))
					
					return redirect(link.getHref());
			}
			
			Logger.debug(createdPayment.toJSON());		
			flash("error", "Something went wrong, please try again later");
			User currentUser = User.find(session("name"));
			return ok(index.render(Coupon.all(), Category.all()));			
			
		} catch (PayPalRESTException e){
			Logger.warn(e.getMessage());
		}
		
		flash("error", "Something went wrong, please try again later");
		User currentUser = User.find(session("name"));
		return ok(index.render( Coupon.all(), Category.all()));
	}
	
	/**
	 * Method is called if the 'approval_url' is returned
	 * 
	 * @return
	 */
	public static Result couponSuccess(){	
		DynamicForm paypalReturn = Form.form().bindFromRequest();

		String payerID;
		paymentID = paypalReturn.get("paymentId");
		payerID = paypalReturn.get("PayerID");
		token = paypalReturn.get("token");
		try{
			String accessToken = new OAuthTokenCredential(CLIENT_ID,CLIENT_SECRET).getAccessToken();	
			Map<String, String> sdkConfig = new HashMap<String, String>();
			sdkConfig.put("mode", "sandbox");		
			apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);	
			payment = Payment.get(accessToken, paymentID);
			
			paymentExecution = new PaymentExecution();
			paymentExecution.setPayerId(payerID);
					
		} catch(Exception e){
			Logger.debug(e.getMessage());
		}
			flash("info","Approve transaction");
			return ok(couponResult.render(currentUser, coupon, details));
	}
	
	/**
	 * Method renders the couponTemplate again and sends a flash
	 * notification message if the transaction is canceled
	 * @return
	 */
	public static Result couponFail(){
		flash("error","Transaction canceled");
		return badRequest(coupontemplate.render(company, coupon));
	}
	
	/**
	 * Method is called after the PayPal process is successful and if the user 
	 * approves the transaction on the couponResult page. It executes the
	 * payment and finalizes the transaction.
	 * @return render index page with a flash message
	 */
	public static Result approveTransaction(){
		
		try {	
			payment.execute(apiContext, paymentExecution);			
			TransactionCP.createTransaction( paymentID,coupon.price, quantity, totalPrice, token, currentUser, coupon);
				
		} catch (PayPalRESTException e) {
			Logger.debug(e.getMessage());
		}
		Logger.info(session("name") + " approved transaction: //TODO");
		flash("success","Transaction complete");

		User currentUser = User.find(session("name"));
		return ok(index.render( Coupon.all(), Category.all()));

	}
	
}
