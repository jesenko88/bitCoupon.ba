package api;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import play.libs.Json;
import models.Category;
import models.Company;
import models.Coupon;
import models.FAQ;
import models.SuperUser;
import models.TransactionCP;
import models.User;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A helper class for converting provided parameters to JSon
 *
 */

 //TODO complete this helper after the used models are finished

public class JSonHelper {
	
	
	public static final String SUPERUSER_USER = "user";
	public static final String SUPERUSER_COMPANY = "company";

	
	
	/**
	 * Helper method used for sending messages in JSon format
	 * receives a tag and value as String and
	 * returns a JSon as ObjectNode 
	 * @param tag String
	 * @param value String
	 * @return ObjectNode
	 */
	public static ObjectNode messageToJSon(String tag, String value) {
		ObjectNode jsnNode = Json.newObject();
		jsnNode.put(tag, value);
		return jsnNode;
	}
	
	
	public static ObjectNode userToJSon(User u) {
		
		ObjectNode userNode = Json.newObject();
		userNode.put("id", u.id);
		userNode.put("name", u.username);
		userNode.put("surname", u.surname);
		userNode.put("email", u.email);
		userNode.put("adress", u.adress);
		userNode.put("city", u.city);
		userNode.put("picture", u.profilePicture);
		return userNode;
	}
	
	
	public static ObjectNode companyToJSon(Company company) {
		
		ObjectNode companyNode = Json.newObject();
		companyNode.put("id", company.id);
		companyNode.put("name", company.name);
		companyNode.put("email", company.email);
		companyNode.put("adress", company.adress);
		companyNode.put("city", company.city);
		companyNode.put("contact", company.contact);
		companyNode.put("logo", company.logo);
		return companyNode;
	}
	
	
	public static ObjectNode couponToJSon(Coupon coupon) {
		if (coupon == null){
			Logger.error("error","Coupon null at couponToJSon()");
			return JSonHelper.messageToJSon("error", "Error occured");
		}
		ObjectNode couponNode = Json.newObject();
		couponNode.put("id", coupon.id);
		couponNode.put("name", coupon.name);
		couponNode.put("price", coupon.price);
		couponNode.put("expiration", coupon.dateExpire.toString());
		couponNode.put("picture", coupon.picture);
		couponNode.put("categoryName", coupon.category.name);
		couponNode.put("description", coupon.description);
		couponNode.put("remark", coupon.remark);
		couponNode.put("seller", coupon.seller.name);
		couponNode.put("minOrder", coupon.minOrder);
		return couponNode;
	}
	
	
	/**
	 * Method receives a list of SuperUser-s and converts it to a
	 * arrayNode. 
	 * @param List<SuperUser>
	 * @return ArrayNode (JSon content)
	 */
	public static ArrayNode superUserListToJson( List<SuperUser> superUsers) {
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (SuperUser curUser : superUsers) {
			ObjectNode superUserNode = Json.newObject();
			superUserNode.put("id", curUser.id); 
			superUserNode.put("email", curUser.email);
			if (curUser instanceof User){
				superUserNode.put("type", SUPERUSER_USER);
			} else{
				superUserNode.put("type", SUPERUSER_COMPANY);
			}
			arrayNode.add(superUserNode);
		}
		return arrayNode;
	}
	
	/**
	 * Returns the provided list of users as JSon content
	 * @param List<User>
	 * @return ArrayNode (JSon)
	 */
	public static ArrayNode userListToJSon( List<User> users) {	
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (User u : users) {
			ObjectNode userNode = Json.newObject();
			userNode.put("id", u.id);
			userNode.put("username", u.username);
			userNode.put("email", u.email);
			arrayNode.add(userNode);
		}
		return arrayNode;
		
	}
	
	/**
	 * Returns the provided list of Companies as JSon content
	 * @param List<Company>
	 * @return
	 */
	public static ArrayNode companyListToJSon( List<Company> companies) {	
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (Company company : companies) {
			ObjectNode companyNode = Json.newObject();
			companyNode.put("id", company.id);
			companyNode.put("name", company.name);
			companyNode.put("logo", company.logo); 
			arrayNode.add(companyNode);
		}
		return arrayNode;
		
	}
	
	/**
	 * Receives a list of Coupons-s and converts it to a
	 * arrayNode. 
	 * @param List<Coupon>
	 * @return  ArrayNode (JSon content)
	 */
	public static ArrayNode couponListToJson( List<Coupon> coupons) {
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (Coupon c : coupons) {
			ObjectNode couponNode = Json.newObject();
			couponNode.put("id", c.id);
			couponNode.put("name", c.name);
			couponNode.put("price", c.price);
			couponNode.put("picture", c.picture);
			arrayNode.add(couponNode);
		}
		return arrayNode;
	}
	
	
	/**
	 * Receives two lists of Coupons-s, converts it to a single list and
	 * returns a JSon content of the list
	 * @param List<Coupon> List<Coupon>
	 * @return  ArrayNode (JSon content)
	 */
	public static ArrayNode couponListsToJson( List<Coupon> coupons1, List<Coupon> coupons2) {
		
		List<Coupon> coupons = new ArrayList<Coupon>();
		coupons.addAll(coupons1);
		coupons.addAll(coupons2);	
		
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (Coupon c : coupons) {
			ObjectNode couponNode = Json.newObject();
			couponNode.put("id", c.id);
			couponNode.put("name", c.name);
			couponNode.put("price", c.price);
			couponNode.put("picture", c.picture);	
			arrayNode.add(couponNode);
		}
		return arrayNode;
	}
	
	/**
	 * Receives a list of Categories and converts it to a
	 * arrayNode. 
	 * @param List<Category>
	 * @return  ArrayNode (JSon content)
	 */
	public static ArrayNode categoryListToJSon ( List<Category> categories ) {
		
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (Category category : categories) {
			ObjectNode categoryNode = Json.newObject();
			categoryNode.put("id", category.id); 
			categoryNode.put("name", category.name);
			categoryNode.put("picture", category.picture); //??
			arrayNode.add(categoryNode);
		}
		return arrayNode;
	}
	
	/**
	 * Receives a list of FAQ-s and converts it to a
	 * arrayNode. 
	 * @param List<FAQ>
	 * @return  ArrayNode (JSon content)
	 */
	public static ArrayNode faqListToJSon ( List<FAQ> questions ) {
		
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (FAQ faq : questions) {
			ObjectNode faqNoe = Json.newObject();
			faqNoe.put("id", faq.id); 
			faqNoe.put("question", faq.question);
			faqNoe.put("answer", faq.answer); 
			arrayNode.add(faqNoe);
		}
		return arrayNode;
	}
	
	
	/**
	 * Receives a list of TransactioCp-s and converts it to a
	 * arrayNode. 
	 * @param List<TransactioCp>
	 * @return  ArrayNode (JSon content)
	 */
	public static ArrayNode transactionListToJSon ( List<TransactionCP> transactions ) {
		
		ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
		for (TransactionCP tran : transactions) {
			ObjectNode transactionNode = Json.newObject();
			transactionNode.put("id", tran.id); 
			transactionNode.put("couponId", tran.coupon.id);
			transactionNode.put("name", tran.coupon.name);
			transactionNode.put("description", tran.coupon.description);
			transactionNode.put("price", tran.coupon.price);
			transactionNode.put("quantity", tran.quantity);
			transactionNode.put("picture", tran.coupon.picture);
			transactionNode.put("totalPrice", tran.totalPrice); 
			transactionNode.put("transactionDate", tran.date.toString());
			transactionNode.put("paymentId", tran.payment_id);
			transactionNode.put("token", tran.token);
			arrayNode.add(transactionNode);
		}
		return arrayNode;
	}
	
}
