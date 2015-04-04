package helpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import models.Coupon;
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
	
	public static void sendNewsletter(List<String> emails, String subject, List<Coupon> coupons) {

		Email mail = new Email();
		mail.setSubject(subject);
		mail.setFrom("bitCoupon.ba <bit.play.test@gmail.com>");
		mail.addTo("bitCoupon.ba Contact <bit.play.test@gmail.com>");
		
		for(String email: emails){
			mail.addTo(email);
		}	
		
		//READING HTML FROM FILE
		String message = "";
		Scanner sc = null;
		try {
			sc = new Scanner(new File("./public/mailer.html"));			
			while(sc.hasNext()){
				message += sc.next();
			}
		} catch (FileNotFoundException e) {
			Logger.error("COULD'T READ EMAIL FILE");
		}finally{
			sc.close();
		}
		message.replace("@1",coupons.get(0).picture);
		message.replace("@2",coupons.get(1).picture);
		message.replace("@3",coupons.get(2).picture);
		Logger.debug(message);
		
		
		mail.setBodyHtml(message);
		MailerPlugin.send(mail);

	}	
	
	
}