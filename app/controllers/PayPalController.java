package controllers;

import java.awt.ItemSelectable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.Coupon;
import models.User;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
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
	
	static Coupon coupon;
	static List<String> details;
	
	/*  paypal  */
	
	//get route for purchase //ovo nam ne treba, koristit cemo buy na coupon template-u
	public static Result showPurchase(){
		return TODO;
	}

	/**
	 * 
	 * @return
	 */
	public static Result purchaseProcessing(){

		try { 
			String accessToken = new OAuthTokenCredential(
					"AXefj_ltBrqquxwtgvio9GBcfFMxFDh7GP8FTfXi489Vt0xCL7OmnKq6IRyXISYVKD98bVutaHBMwN9h",
					"EPlX3tMGxjQYv0Wf2de9c-QeMlc8PT22jqWDAnexpFTbk1WJNlOgvS2ZQXfhrlQ_7DCbPYl1ElEDYDH9")
					.getAccessToken();
			
			Map<String, String> sdkConfig = new HashMap<String, String>();
			sdkConfig.put("mode", "sandbox");
				
			APIContext apiContext = new APIContext(accessToken);
			apiContext.setConfigurationMap(sdkConfig);
			
			Amount amount = new Amount();
			
			DynamicForm buyForm = Form.form().bindFromRequest();		
			coupon = Coupon.find(Long.parseLong((buyForm.data().get("coupon_id"))));
			int quantity = Integer.parseInt(buyForm.data().get("quantity"));
			Double totalPrice = coupon.price * quantity;
			
			String totalPriceString = String.format("%1.2f",totalPrice);
			
			amount.setTotal(totalPriceString);
			amount.setCurrency("USD");
			
			String description = String.format("Coupon: %s\n"
					+ "Price: %s\n"
					+ "Quantity: %d\n"
					+ "Total: %s", coupon.name, coupon.price, quantity, totalPriceString);
					
			Transaction transaction = new Transaction();
			transaction.setDescription(description);
			transaction.setAmount(amount);
			
			/* details to render in the success view*/
			details = new ArrayList<String>();
			details.add("Quantity: " + Integer.toString(quantity));
			details.add("Todal price: " + totalPriceString);
			
			List<Transaction> transactions = new ArrayList<Transaction>();
			transactions.add(transaction);
			
			Payer payer = new Payer();
			payer.setPaymentMethod("paypal");
			
			Payment payment = new Payment();
			payment.setIntent("sale");
			payment.setPayer(payer);
			payment.setTransactions(transactions);
			RedirectUrls redirectUrls = new RedirectUrls();
			redirectUrls.
						setCancelUrl("http://localhost:9000/couponfail");
			redirectUrls.
						setReturnUrl("http://localhost:9000/couponsuccess");
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
			return ok(index.render(currentUser, Coupon.all()));
			
			
		} catch (PayPalRESTException e){
			Logger.warn(e.getMessage());
		}
		
		flash("error", "Something went wrong, please try again later");
		User currentUser = User.find(session("name"));
		return ok(index.render(currentUser, Coupon.all()));
	}
	
	/**
	 * 
	 * @return
	 */
	public static Result couponSuccess(){
		
		DynamicForm paypalReturn = Form.form().bindFromRequest();
		
		String paymentID;
		String payerID;
		String token;
		
		paymentID = paypalReturn.get("paymentId");
		payerID = paypalReturn.get("PayerID");
		token = paypalReturn.get("token");
	try{
		String accessToken = new OAuthTokenCredential(
				"AXefj_ltBrqquxwtgvio9GBcfFMxFDh7GP8FTfXi489Vt0xCL7OmnKq6IRyXISYVKD98bVutaHBMwN9h",
				"EPlX3tMGxjQYv0Wf2de9c-QeMlc8PT22jqWDAnexpFTbk1WJNlOgvS2ZQXfhrlQ_7DCbPYl1ElEDYDH9")
				.getAccessToken();
		
		Map<String, String> sdkConfig = new HashMap<String, String>();
		sdkConfig.put("mode", "sandbox");		
		APIContext apiContext = new APIContext(accessToken);
		apiContext.setConfigurationMap(sdkConfig);
		
		Payment payment = Payment.get(accessToken, paymentID);
		
		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerID);
		
		Payment newPayment = payment.execute(apiContext, paymentExecution);
		
	} catch(Exception e){
		Logger.debug(e.getMessage());
	}
		flash("success","Transaction successful");
		User currentUser = User.find(session("name"));
		return ok(couponResult.render(currentUser, coupon, details));
	}
	
	
	public static Result couponFail(){
		User currentUser = User.find(session("name"));
		flash("error","Transaction canceled");
		return ok(coupontemplate.render(currentUser, coupon));
	}
	
}
