import java.util.Date;
import helpers.HashHelper;
import models.Category;
import models.Coupon;
import models.EmailVerification;
import models.User;
import org.junit.*;
import play.test.*;
import play.libs.F.*;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


public class IntegrationTest {

	/**
	 * add your integration test here in this example we just check if the
	 * welcome page is being shown
	 */
	

	@Test
	public void test() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						browser.goTo("http://localhost:3333");					
						assertThat(browser.pageSource()).contains("Registration");
						assertThat(browser.pageSource()).contains("Login");

					}
				});
	}

	/**
	 * Test for User registration
	 */
	@Test
	public void testRegistration() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {

						browser.goTo("http://localhost:3333/signup");
						browser.fill("#username").with("tester");
						browser.fill("#email").with("tester@mail.com");
						browser.fill("#password").with("123456");
						browser.fill("#confirmPassword").with("123456");
						browser.submit("#submit");
						assertThat(browser.pageSource()).contains("Registration");
						assertThat(browser.pageSource()).contains("Login");
						assertThat(browser.pageSource()).contains("A verification mail has been sent to your email address");

					}
				});
	}

	/**
	 * Test for User login
	 */
	@Test
	public void testLogin() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						// User.createUser("tester", "tester@mail.com",
						// "123456");

						User.createUser("tester", "tester@bitcamp.ba",
								HashHelper.createPassword("123456"), true);
						EmailVerification setVerified = new EmailVerification(3, true);
						setVerified.save();

						browser.goTo("http://localhost:3333/loginpage");
						browser.fill("#email").with("tester@bitcamp.ba");
						browser.fill("#password").with("123456");
						browser.submit("#submit");
						assertThat(browser.pageSource()).contains("tester");
						assertThat(browser.pageSource()).contains("Home");
						assertThat(browser.pageSource()).contains("Available Coupons");

					}
				});

	}

	/**
	 * Tests showing coupon which is made in this test
	 */
	@Test
	public void testShowAddedCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						Category food = new Category("Food");
						food.save();
						Coupon.createCoupon("TestCoupon", 55.4, 
								new Date(), "url", food, "description",
								"remark");
						browser.goTo("http://localhost:3333/");
						assertThat(browser.pageSource()).contains("TestCoupon");
						assertThat(browser.pageSource()).contains(
								"Only 55.40 KM");

					}
				});

	}

	/**
	 * Tests deleting coupon which is made in Global class
	 */
	@Test
	public void testDeleteCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						Coupon.delete(1);
						browser.goTo("http://localhost:3333/");
						assertThat(!browser.pageSource().contains(
								"Vikend u Neumu"));

					}
				});

	}

	/**
	 * Tests deleting coupon which is made in this test
	 */
	@Test
	public void testDeleteAddedCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						Category food = new Category("Food");
						food.save();
						long couponId = Coupon.createCoupon("TestCoupon", 55.8, new Date(), "url", food,
								"description", "remark");
						Coupon.delete(couponId);
						browser.goTo("http://localhost:3333/coupon/" + couponId);
						browser.submit("#delete");
						assertThat(!browser.pageSource().contains("TestCoupon"));
					}
				});

	}
	
	/**
	 * Testing if user (non-admin) can accesse to admin panel
	 */
	@Test
	public void testSecurityAdmin() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						User.createUser("tester", "tester@bitcamp.ba",
								HashHelper.createPassword("123456"), false);
						EmailVerification setVerified = new EmailVerification(3, true);
						setVerified.save();

						browser.goTo("http://localhost:3333/loginpage");
						browser.fill("#email").with("tester@bitcamp.ba");
						browser.fill("#password").with("123456");
						browser.submit("#submit");
						
						browser.goTo("http://localhost:3333/control-panel/user/2");
						assertThat(browser.pageSource().contains(":( Login to complete this action"));

					
					}
					});

		}
	/**
	 * Test if admin can accesse to loginpage
	 */
	@Test
	public void testSecurityAdmin2() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						

						browser.goTo("http://localhost:3333/loginpage");
						browser.fill("#email").with("bitcoupon@gmail.com");
						browser.fill("#password").with("bitadmin");
						browser.submit("#submit");
						
						browser.goTo("http://localhost:3333//userPanel");
						assertThat(browser.pageSource().contains("List of Users"));

					
					}
					});

		}
	/**
	 * Test if admin can accesse to couponePanel
	 */
	@Test
	public void testSecurityAdmin3() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						

						browser.goTo("http://localhost:3333/loginpage");
						browser.fill("#email").with("admin@mail.com");
						browser.fill("#password").with("bitadmin");
						browser.submit("#submit");
						
						browser.goTo("http://localhost:3333//couponPanel");
						assertThat(browser.pageSource().contains("COUPON CREATION"));

					
					}
					});

		}
	/*
	 * 
	 * @Test for search user
	 */
	
	@Test
	public void testSearchUser() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				HTMLUNIT, new Callback <TestBrowser>() {
					public void invoke(TestBrowser browser) {
						
						browser.goTo("http://localhost:3333/loginpage");
						browser.fill("#email").with("bitcoupon@gmail.com");
						browser.fill("#password").with("bitadmin");
						browser.submit("#submit");
						browser.goTo("http://localhost:3333/userPanel");
						browser.fill("#qU").with("bitadmin");
						assertThat(browser.pageSource()).contains("John");
	
						
					}
					});
		
	}
	
	/**
	 * 
	 *  @Test for coupon search
	 */
	
		@Test
	    public void testSearch() {
	        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
	            public void invoke(TestBrowser browser) {
	                browser.goTo("http://localhost:3333");
	                browser.fill("#q").with("neum");
	                assertThat(browser.pageSource()).contains("Dvije noći za dvoje u Hotelu Sunce Neum");
	            }
	        });
	    }

						
}
