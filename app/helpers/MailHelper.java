package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import models.Coupon;
import models.Subscriber;
import models.User;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;

public class MailHelper {
	
	private static final String MAIL_FROM = Play.application().configuration().getString("emailFrom");
	private static final String ADD_TO = Play.application().configuration().getString("emailAddTo");

	public static void send(String subject, String email, String message) {

		/**
		 * Set subject, body and sender of mail and send mail
		 */
		try {
			HtmlEmail mail = new HtmlEmail();
			mail.setSubject(subject);
			mail.setFrom(MAIL_FROM);
			mail.addTo(ADD_TO);
			mail.addTo(email);
			mail.setMsg(message);
			mail.setHtmlMsg(String
					.format("<html><body><strong> %s </strong>: <p> %s </p> </body></html>",
							email, message));
			mail.setHostName("smtp.gmail.com");
		//	mail.setSmtpPort(587);
			mail.setStartTLSEnabled(true);
			mail.setSSLOnConnect(true);
			mail.setAuthenticator(new DefaultAuthenticator(
					Play.application().configuration().getString("EMAIL_USERNAME_ENV"), 
					Play.application().configuration().getString("EMAIL_PASSWORD_ENV")
					));

			mail.send();

		} catch (EmailException e) {
			Logger.error(e.getMessage());
		}

	}
	
	
	/**
	 * This is a specific mail sender for contact form, used for sending feedback to admins.
	 * @param email
	 * @param name
	 * @param phone
	 * @param message
	 */
	public static void sendFeedback(String email, String name, String phone,
			String message) {

		/**
		 * Set subject, body and sender of mail and send mail
		 */
		try {
			HtmlEmail mail = new HtmlEmail();
			mail.setSubject("bitCoupon.ba Feedback!");
			mail.setFrom(email);

			mail.addTo(ADD_TO);

			List<String> emailList = User.allAdminMails();

			for (String e : emailList) {
				mail.addTo(e);
			}

			mail.setMsg(message);
			mail.setHtmlMsg(String.format("<html>" + "<body>"
					+ "<strong> My email </strong>: " + "%s" + "<br></br>"
					+ "<strong> My name  </strong>: " + "%s" + "<br></br>"
					+ "<strong> My phone number </strong>: " + "%s"
					+ "<br></br>" + "<p> %s </p> " + "</body>" + "</html>",
					email, name, phone, message));
			mail.setHostName("smtp.gmail.com");
			mail.setStartTLSEnabled(true);
			mail.setSSLOnConnect(true);
			mail.setAuthenticator(new DefaultAuthenticator(
						Play.application().configuration().getString("EMAIL_USERNAME_ENV"), 
						Play.application().configuration().getString("EMAIL_PASSWORD_ENV")
						));
			mail.send();

		} catch (EmailException e) {
			Logger.error(e.getMessage());
		}

	}
	
	/**
	 *Helper which sends newsletter email. 
	 *Each email gets different template.
	 *Method reads HTML from Email/index.html folder and use Jsoup libraries
	 *for parsing HTML.
	 * @param emails
	 * @param subject
	 * @param coupons
	 */
	public static void sendNewsletter(List<String> emails, String subject,
			List<Coupon> coupons, String addHTML) {

		try {
			HtmlEmail mail = new HtmlEmail();
			mail.setSubject(subject);
			mail.setFrom(MAIL_FROM);
			mail.addTo(ADD_TO);

			// READING HTML FROM FILE
			String message = "";
			Scanner sc = null;
			try {
				sc = new Scanner(new File("./public/Email/index.html"));
				while (sc.hasNextLine()) {
					message += sc.nextLine();
				}
			} catch (FileNotFoundException e) {
				Logger.error("COULD'T READ EMAIL FILE");
			} finally {
				sc.close();
			}

			Document preparedHTML;
			for (String email : emails) {
				mail.addTo(email);
				preparedHTML = getPreparedHTML(email, message, coupons);
				preparedHTML.getElementById("appendableText").append(addHTML);
				mail.setHtmlMsg(preparedHTML.toString());
				mail.setHostName("smtp.gmail.com");
				mail.setStartTLSEnabled(true);
				mail.setSSLOnConnect(true);
				mail.setAuthenticator(new DefaultAuthenticator(Play
						.application().configuration()
						.getString("EMAIL_USERNAME_ENV"), Play.application()
						.configuration().getString("EMAIL_PASSWORD_ENV")));
				mail.send();
			}

		} catch (EmailException e) {
			Logger.error(e.getMessage());
		}

	}
	
	/**
	 * Method gets html template, email and list of coupons.
	 * For each email it generates different HTML. 
	 * TODO less hardcode.
	 * @param html
	 * @param coupons
	 * @return
	 */
	private static Document getPreparedHTML(String email,String html, List<Coupon> coupons){
		
		//Parsing html document and adding coupon names
		//and prices into html.
		//At this point its hardcoded and MUST BE changed.
		Document doc =  Jsoup.parse(html);
		Element couponOneName = doc.getElementById("Cp1-name");
		couponOneName.appendText(coupons.get(0).name);		
		Element couponOnePrice = doc.getElementById("Cp1-price");
		couponOnePrice.appendText(coupons.get(0).price +"€");
		Element couponOneImage = doc.getElementById("image_one");
		couponOneImage.attr("src", coupons.get(0).picture);
		
		Element couponTwoName = doc.getElementById("Cp2-name");
		couponTwoName.appendText(coupons.get(1).name);		
		Element couponTwoPrice = doc.getElementById("Cp2-price");
		couponTwoPrice.appendText(coupons.get(1).price +"€");
		Element couponTwoImage = doc.getElementById("image_two");
		couponTwoImage.attr("src", coupons.get(1).picture);
		
		Element couponThreeName = doc.getElementById("Cp3-name");
		couponThreeName.appendText(coupons.get(2).name);		
		Element couponThreePrice = doc.getElementById("Cp3-price");
		couponThreePrice.appendText(coupons.get(2).price +"€");
		Element couponThreeImage = doc.getElementById("image_three");
		couponThreeImage.attr("src", coupons.get(2).picture);
		
		Element unsubscribe = doc.getElementById("unsubscribe");
		String token = Subscriber.getToken(email);
		String unsubscribePath = Play.application().configuration().getString("PATH") +token;
		Logger.debug(unsubscribePath);
		unsubscribe.attr("href", unsubscribePath);
		
		
		return doc;
	}

	/**
	 * Method for sending purchase details to buyer email
	 * @param buyerName String
	 * @param buyerSurname String
	 * @param email String
	 * @param price double
	 * @param quantity int
	 * @param token String
	 * @param paymentId String
	 * @param link String
	 */
	public static void sendPurchaseInfo(String buyerName, String buyerSurname, String email,
			double price, int quantity, String token, String paymentId, String link) {
		String  couponPurchase = Messages.get("couponPurchase");
		String  congratulations = Messages.get("congratulations");
		String	yourNewCouponIsReady = Messages.get("yourNewCouponIsReady");
		String	visitUsAgain = Messages.get("visitUsAgain");
		String	yourBitCoupon = Messages.get("yourBitCoupon");
		MailHelper.send(couponPurchase, email,
				congratulations + "<br>" +
				yourNewCouponIsReady + "<br>" +
				visitUsAgain + " " + yourBitCoupon 
				+ "<br><br>"
				+ Messages.get("buyerDetails")
				+ "<br>"
				+ Messages.get("buyer") + ": " + buyerName
				+ " " + buyerSurname
				+ "<br>"
				+ Messages.get("email") + ": " + email
				+ "<br><br>"
				+ Messages.get("paymentDetails")
				+ "<br>"
				+ Messages.get("price") + ": " +price
				+ "<br>"
				+ Messages.get("quantity") + ": " +  quantity
				+ "<br>"
				+ Messages.get("token") + ": " + token
				+ "<br>"
				+ Messages.get("paymentId") + ": " + paymentId
				+ "<br>"
				+ Messages.get("toSeeCouponClickLinkBelow") 
				+ "<br>"
				+ link);
	}
	
}