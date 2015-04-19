package models;

import java.util.Date;
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
	public void testCreateUser() {
		long id = User.createUser("Neil", "Armstrong", new Date(), "male", "adress", "city", "neil@mail.com", "123456", false);
		User u = User.find(id);
		assertNotNull(u);
		assertEquals(u.username, "Neil");
		assertEquals(u.email, "neil@mail.com");
		assertEquals(u.password, "123456");
	}
	
	@Test
	public void testFindNonExistingUser() {
		User u = User.find(1500);	
		assertNull(u);
	}
	
	@Test
	public void testDelete() {
		long id = User.createUser("Jack", "Sparrow", new Date(), "male", "adress", "city", "neil@mail.com", "123456", false);
		User.delete(id);
		User b = User.find(id);
		assertNull(b);
	}
	
	@Test
	public void testCouponCreate(){
		Category science = new Category("Science");
		science.save();
		long id = Coupon.createCoupon("Test", 55.3, new Date(), "url", science, "description", "remark");
		Coupon c = Coupon.find(id);
		assertNotNull(c);
		
	}
	
	@Test
	public void testFindNonExistingCoupon(){
		Coupon c = Coupon.find(1500);
		assertNull(c);
	}
	
	@Test
	public void testDeleteCoupon(){
		Category mix = new Category("Mix");
		mix.save();
		long id = Coupon.createCoupon("test", 2.22, new Date(), "testurl", mix, "description", "remark");
		Coupon.delete(id);
		Coupon c = Coupon.find(id);
		assertNull(c);
		
	}

	
	
	@Test
	public void updateUser(){
		long id = User.createUser("Jack", "Sparrow", new Date(), "male", "adress", "city", "neil@mail.com", "123456", false);
		User user = User.find(id);
		user.username = "Daniels";
		user.isAdmin = true;
		user.save();
		assertEquals(user.username, "Daniels");
		assertEquals(user.isAdmin, true);
		
	}
	
	@Test
	public void testCategoryCreate(){
		long id = Category.createCategory("Test Category");
		Category category=Category.find(id);
		assertNotNull(category);
	}
	
	@Test public void testFindNonExistingCategory(){
		Category category=Category.find(1500);
		assertNull(category);
	}
	
	@Test
	public void deleteCategory(){
		long id = Category.createCategory("The black sheep");
		Category category = Category.find(id);
		assertNotNull(category);
		Category.delete(id);
		Category c = Category.find(id);
		assertNull(c);
	}


	@Test
	public  void updateCoupon(){
		long id = Coupon.createCoupon("Rucak", 15, null, null, null, "Rucak za dvoje", "Test za rucak");
		Coupon coupon = Coupon.find(id);
		coupon.name="Vecera";
		coupon.description = "Vecera za troje";
		coupon.remark = "Test update rucak";
		coupon.save();
		assertEquals(coupon.name,"Vecera");
		assertEquals(coupon.description,"Vecera za troje");
		assertEquals(coupon.remark,"Test update rucak");
	}
	
	@Test
	public void createFAQ(){
		int id = FAQ.createFAQ("faqQuestion", "faqAnswer");
		FAQ newFAQ = FAQ.find(id);
		assertNotNull(newFAQ);
		assertEquals(newFAQ.question,"faqQuestion");
		assertEquals(newFAQ.answer,"faqAnswer");
	}
	
	@Test
	public void updateFAQ(){
		int id = FAQ.createFAQ("what question", "what answer");
		FAQ newFAQ = FAQ.find(id); 
		newFAQ.question = "where question";
		newFAQ.answer = "where answer";
		FAQ.update(newFAQ);
		assertEquals(newFAQ.question,"where question");
		assertEquals(newFAQ.answer,"where answer");
	}
	
	@Test
	public void deleteFAQ(){
		int id = FAQ.createFAQ("faqQuestion", "faqAnswer");
		FAQ newFAQ = FAQ.find(id);
		assertNotNull(newFAQ);
		FAQ.delete(id);
		FAQ test = FAQ.find(id);
		assertNull(test);
	}
	
	
}
