package models;

import java.util.Date;
import java.util.List;

import models.comments.Comment;
import models.comments.Report;
import models.questions.Question;

import org.junit.*;

import controllers.QuestionController;
import play.Play;
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
		long id = User.createUser("Neil", "Armstrong", new Date(), "male", "adress", "city", "neil@mail.com", "123456", false, "picture");
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
		long id = User.createUser("Jack", "Sparrow", new Date(), "male", "adress", "city", "neil@mail.com", "123456", false, "picture");
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
		assertEquals(c.status, Coupon.Status.DELETED);
		
	}

	
	
	@Test
	public void updateUser(){
		long id = User.createUser("Jack", "Sparrow", new Date(), "male", "adress", "city", "neil@mail.com", "123456", false, "picture");
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
	
	@Test
	public void createQuestion() {
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		long qid = Question.create("question", "", Coupon.find(cid), User.find(uid));
		Question test = Question.findById(qid);
		assertNotNull(test);
	}
	
	@Test
	public void deleteQuestion() {
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		long qid = Question.create("question", "", Coupon.find(cid), User.find(uid));
		Question.delete(qid);
		assertNull(Question.findById(qid));
	}
	
	@Test
	public void createComment() {
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		long commentId = Comment.create("Comment test", Coupon.find(cid), User.find(uid));
		assertNotNull(Comment.findById(commentId));
	}
	
	@Test 
	public void deleteComment() {
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		long commentId = Comment.create("Comment test", Coupon.find(cid), User.find(uid));
		Comment.delete(commentId);
		assertNull(Comment.findById(commentId));
	}
	
	
	@Test
	public void reportComment() {
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		long commentId = Comment.create("Comment test", Coupon.find(cid), User.find(uid));
		Report report = new Report("Some report", Comment.findById(commentId), User.find(uid));
		report.save();
		assertEquals(report.comment, Comment.findById(commentId));
	}
	
	@Test
	public void findReportByComment(){
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		Comment comment = new Comment("Comment test", Coupon.find(cid), User.find(uid));
		comment.save();
		Report report = new Report("Some report", comment, User.find(uid));
		report.save();
		List<Report> reports = Report.findByComment(comment); 
		assertEquals(report, reports.get(0));
	}

	@Test
	public void deleteReport() {
		long cid = Coupon.createCoupon("Test", 55.3, null, "", null, "", "");
		long uid = User.createUser("Neil", "Tester", null, "", "", "", "", "", false, "");
		long commentId = Comment.create("Comment test", Coupon.find(cid), User.find(uid));
		long rid = Report.create("Report test", Comment.findById(commentId), User.find(uid));
		Report.delete(rid);
		assertNull(Report.findById(rid));	
	}

	
	
}

