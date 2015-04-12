package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;









import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;





import controllers.UserController;
import models.Coupon;
import models.Subscriber;
import models.User;
import play.Logger;
import play.libs.mailer.Email;
import play.libs.mailer.MailerPlugin;

public class MailHelper {

	public static void send(String email, String message) {

		/**
		 * Set subject, body and sender of mail and send mail
		 */
		Email mail = new Email();
		mail.setSubject("Mail for registration to bitCoupon.ba");
		mail.setFrom("bitCoupon.ba <bit.play.test@gmail.com>");
		mail.addTo("bitCoupon.ba Contact <bit.play.test@gmail.com>");
		mail.addTo(email);
		
		mail.setBodyText(message);
		mail.setBodyHtml(String
				.format("<html><body><strong> %s </strong>: <p> %s </p> </body></html>",
						email, message));
		MailerPlugin.send(mail);

	}	
	
	
	/**
	 * This is a specific mail sender for contact form, used for sending feedback to admins.
	 * @param email
	 * @param name
	 * @param phone
	 * @param message
	 */
	public static void sendFeedback(String email, String name, String phone, String message) {

		String adminMail = "haris.krkalic@bitcamp.ba";
		
		/**
		 * Set subject, body and sender of mail and send mail
		 */
		Email mail = new Email();
		mail.setSubject("bitCoupon.ba Feedback!");
		mail.setFrom(email);
		
		mail.addTo("bitCoupon.ba Feedback <bit.play.test@gmail.com>");
		
		List<String> emailList = User.allAdminMails();
		
		for(String e : emailList){
			mail.addTo(e);
		}
	
		mail.setBodyText(message);
		mail.setBodyHtml(String
				.format("<html>"
						+ "<body>"
						+ "<strong> My email </strong>: " + "%s"
						+ "<br></br>"
						+ "<strong> My name  </strong>: " + "%s"
						+ "<br></br>"
						+ "<strong> My phone number </strong>: " + "%s" 
						+ "<br></br>"
						+ "<p> %s </p> "
						+ "</body>"
						+ "</html>",
						email, name, phone, message));
		MailerPlugin.send(mail);
		
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
	public static void sendNewsletter(List<String> emails, String subject, List<Coupon> coupons, String addHTML)  {
		copyImages(coupons);
		
		Email mail = new Email();
		mail.setSubject(subject);
		mail.setFrom("bitCoupon.ba <bit.play.test@gmail.com>");
		mail.addTo("bitCoupon.ba Contact <bit.play.test@gmail.com>");		
		
		//READING HTML FROM FILE
		String message = "";
		Scanner sc = null;
		try {
			sc = new Scanner(new File("./public/Email/index.html"));			
			while(sc.hasNextLine()){
				message +=sc.nextLine();
			}
		} catch (FileNotFoundException e) {
			Logger.error("COULD'T READ EMAIL FILE");
		}finally{
			sc.close();
		}
		
		Document preparedHTML;
		for(String email: emails){
			mail.addTo(email);
			preparedHTML = getPreparedHTML(email, message, coupons);
			preparedHTML.getElementById("appendableText").append(addHTML);
			mail.setBodyHtml(preparedHTML.toString());
			MailerPlugin.send(mail);			
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
		
		Element couponTwoName = doc.getElementById("Cp2-name");
		couponTwoName.appendText(coupons.get(1).name);		
		Element couponTwoPrice = doc.getElementById("Cp2-price");
		couponTwoPrice.appendText(coupons.get(1).price +"€");
		
		Element couponThreeName = doc.getElementById("Cp3-name");
		couponThreeName.appendText(coupons.get(2).name);		
		Element couponThreePrice = doc.getElementById("Cp3-price");
		couponThreePrice.appendText(coupons.get(2).price +"€");
		
		Element unsubscribe = doc.getElementById("unsubscribe");
		String token = Subscriber.getToken(email);
		String unsubscribePath = "http://localhost:9000" +"/unsubscribe/" +token;
		Logger.debug(unsubscribePath);
		unsubscribe.attr("href", unsubscribePath);
		
		
		return doc;
	}
	
	
	/**
	 * Method takes pictures from coupons and add them
	 * into img folder for email template.
	 * TODO: Less hardcode
	 * @param coupons
	 */
	private static void copyImages(List<Coupon> coupons) {
		String photoOne = "./public/" +coupons.get(0).picture;
		String photoTwo = "./public/" +coupons.get(1).picture;
		String photoThree = "./public/" +coupons.get(2).picture;
		
		FileUpload.CopyFile(photoOne, "./public/Email/img/twocolimg1.png");
		FileUpload.CopyFile(photoTwo, "./public/Email/img/twocolimg2.png");
		FileUpload.CopyFile(photoThree, "./public/Email/img/twocolimg3.png");
	}
	
}