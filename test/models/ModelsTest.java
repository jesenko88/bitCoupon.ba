package models;

import java.util.Date;

import helpers.HashHelper;

import org.junit.*;

import play.test.WithApplication;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class ModelsTest extends WithApplication {
	@Before
	public void setUp() {
		
		fakeApplication( inMemoryDatabase()); //,fakeGlobal() ne radi u play verzijama > 2.0, uzeti u obzir admina na mjestu 1 zbog global klase
	}
	
	@Test
	public void testCreate() {
		User.createUser("tester", "test@mail.com", "654321", false); //already 2 users in global class
		User u = User.find(3);
		assertNotNull(u);
		assertEquals(u.username, "tester");
		assertEquals(u.email, "test@mail.com");
		assertEquals(u.password, "654321");
	}
	
	@Test
	public void testFindNonExisting() {
		User u = User.find(1000);
		
		assertNull(u);
	}
	
	@Test
	public void testDelete() {
		User.createUser("test", "test@bitcamp.ba", HashHelper.createPassword("54321"), false);
		User.delete(1);
		User b = User.find(1);
		assertNull(b);
	}
	
	@Test
	public void testCouponCreate(){
		Category food = new Category("Food");
		food.save();
		Coupon.createCoupon("Test", 55.3, new Date(), "url", food, "description", "remark");
		Coupon c = Coupon.find(4);
		assertNotNull(c);
		
	}
	
	@Test
	public void testFindNonExistingCoupon(){
		Coupon c = Coupon.find(1500);
		assertNull(c);
	}
	
	@Test
	public void deleteCoupon(){
		Category food = new Category("Food");
		food.save();
		Coupon.createCoupon("test", 2.22, new Date(), "testurl", food, "description", "remark");
		Coupon.delete(4);
		Coupon c = Coupon.find(4);
		assertNull(c);
		
	}
	@Test
	public void deleteExistingCoupon(){  //tests delete coupon which is made in Global class
		Coupon.delete(2);
		Coupon c = Coupon.find(2);
		assertNull(c);
	}
	
	
	@Test
	public void updateUser(){
		User.createUser("tester", "tester@bitcamp.ba",
				HashHelper.createPassword("123456"), false);
		EmailVerification setVerified = new EmailVerification(2, true);
		setVerified.save();
		
		User user = User.find(2);
		user.username = "fixer";
		user.isAdmin = true;
		assertEquals(user.username, "fixer");
		assertEquals(user.isAdmin, true);
		
	}
	
	@Test
	public void testCategoryCreate(){
		Category.createCategory("Test Category");
		Category category=Category.findByName("Test Category");
		assertNotNull(category);
	}
	
	@Test public void testFindNonExistingCategory(){
		Category category=Category.find(1333);
		assertNull(category);
	}
	
	@Test
	public void deleteCategory(){
		long id=Category.createCategory("New Category");
		Category category=Category.find(id);
		assertNotNull(category);
		Category.delete(id);
		Category c=Category.find(id);
		assertNull(c);
	}

	
	@SuppressWarnings("deprecation")
	@Test
	public  void updateCoupon(){
		//Coupon.createCoupon("Rucak", 15, null, null, null, "Rucak","Test za rucak");
		Coupon c = new Coupon("Rucak", 15, null, null, null, "Rucak za dvoje", "Test za rucak");
		c.save();
		Coupon coupon=Coupon.find(4);
		coupon.name="Vecera";
		coupon.description = "Rucak za troje";
		coupon.remark = "Test za rucak promjena";
		coupon.save();
		assertEquals(coupon.name,"Vecera");
		assertEquals(coupon.description,"Rucak za troje");
		assertEquals(coupon.remark,"Test za rucak promjena");
	}
	
	@Test
	public void createFAQ(){
		FAQ.createFAQ("faqQuestion", "faqAnswer");
		FAQ newFAQ = FAQ.find(4); // 3 FAQ-s are created in global class already
		assertNotNull(newFAQ);
		assertEquals(newFAQ.id, 4);
		assertEquals(newFAQ.question,"faqQuestion");
		assertEquals(newFAQ.answer,"faqAnswer");
	}
	
	@Test
	public void updateFAQ(){
		FAQ.createFAQ("what question", "what answer");
		FAQ newFAQ = FAQ.find(4); // 3 FAQ-s are created in global class already
		newFAQ.question = "where question";
		newFAQ.answer = "where answer";
		FAQ.update(newFAQ);
		assertEquals(newFAQ.question,"where question");
		assertEquals(newFAQ.answer,"where answer");
	}
	
	@Test
	public void deleteFAQ(){
		FAQ.createFAQ("faqQuestion", "faqAnswer");
		FAQ newFAQ = FAQ.find(4); // 3 FAQ-s are created in global class already
		assertNotNull(newFAQ);
		FAQ.delete(4);
		FAQ test = FAQ.find(4);
		assertNull(test);
		
	}
	
	
}











