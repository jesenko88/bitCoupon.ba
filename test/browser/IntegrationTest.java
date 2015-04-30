package browser;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import helpers.HashHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.Category;
import models.Company;
import models.Coupon;
import models.EmailVerification;
import models.Pin;
import models.User;

import org.junit.Test;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import play.Play;
import play.libs.F.Callback;
import play.test.TestBrowser;



public class IntegrationTest {

		
	/*
	 * Registration test //PROBLEM
	 */
/*	
	@Test
	public void testRegistration() {
		running( testServer(3333, fakeApplication(inMemoryDatabase())),
					new HtmlUnitDriver() , new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
					browser.goTo("http://localhost:3333/signup");
			//		 assertThat(browser.pageSource()).contains("REGISTRATION");
					assertThat(browser.pageSource()).contains("Login");
					 browser.fill("#email").with("jeshko2@hotmail.com");
					 browser.fill("#password").with("Testing88");
					 browser.fill("#confirmPassword").with("Testing88");
					 browser.fill("#username").with("Testing");
					 browser.fill("#surname").with("Testerovic");
					 browser.fill("#datepicker").with("05/10/1988");				 
					 browser.fill("#adress").with("Lozionicka");
					 browser.fill("#city").with("Sarajevo");
					 browser.submit("#submit-user");					
//				  	 assertThat(browser.pageSource()).contains("A verification mail has been sent to your email address!");
//					 assertThat(browser.pageSource()).contains("Login");
//					 assertThat(browser.pageSource()).contains("Forgot Password?");
			}
		});
	} 
*/	
	
	
	/**
	 * Test if the
	 * welcome page is being shown
	 */
	@Test
	public void test() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						browser.goTo("http://localhost:3333");
						assertThat(browser.pageSource()).contains("Registration");
						assertThat(browser.pageSource()).contains("Login");
					}
				});
	}

    


	/*
	 * Login test
	 */
	@Test
	public void testLogin() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						long id = User.createUser("steven", "hawking", new Date(),"male","adress", "city", 
										"hawking@mail.com",HashHelper.createPassword("123456"), false,
										Play.application().configuration().getString("defaultProfilePicture"));
						EmailVerification.makeNewRecord(id, true);
						browser.goTo("http://localhost:3333/loginpage");
						assertThat(browser.pageSource().contains("LOGIN"));
						assertThat(browser.pageSource()).contains("Forgot password?");
						browser.fill("#email").with("hawking@mail.com");
						browser.fill("#password").with("123456");
						browser.submit("#submit-login");
						assertThat(browser.pageSource()).contains("You are logged in as: hawking@mail.com");
						assertThat(browser.pageSource()).contains("steven");
						assertThat(browser.pageSource()).contains("Menu");
					}
				});
	}
	

	/**
	 * Test unauthorized admin panel access
	 */
	@Test
	public void adminPanelAcces() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						
						/* creating administrator */
						long adminId = User.createUser("nikola", "tesla", new Date(),"male","adress", "city", 
								"tesla@mail.com",HashHelper.createPassword("123456"), true,
								Play.application().configuration().getString("defaultProfilePicture"));
						EmailVerification.makeNewRecord(adminId, true);
						
						/* creating regular user without admin rights*/
						long userId = User.createUser("regUser", "hawking", new Date(),"male","adress", "city", 
								"regUser@mail.com",HashHelper.createPassword("123456"), false,
								Play.application().configuration().getString("defaultProfilePicture"));
						EmailVerification.makeNewRecord(userId, true);
						
						/* loging in as regular user */
						browser.goTo("http://localhost:3333/loginpage");
						assertThat(browser.pageSource().contains("LOGIN"));
						assertThat(browser.pageSource()).contains("Forgot password?");
						browser.fill("#email").with("regUser@mail.com");
						browser.fill("#password").with("123456");
						browser.submit("#submit-login");
						assertThat(browser.pageSource()).contains("You are logged in as: regUser@mail.com");			
						browser.goTo("http://localhost:9000/control-panel/user/1");
						assertThat(browser.pageSource().contains(":( Login to complete this action"));
					}
				});
	}
	
	
	/**
	 * Test added coupon preview on the index page
	 */
	@Test
	public void testShowAddedCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						Category category = new Category("TestCategory");
						category.save();
						Coupon c = new Coupon("TestCoupon", 55.4, new Date(), "picturePath", category, "description","remark");
						c.status = Coupon.Status.ACTIVE;
						c.save();
						browser.goTo("http://localhost:3333/");
						assertThat(browser.pageSource()).contains("TestCoupon");
					}
				});

	}

	/**
	 * Tests deleting coupon which is made in Global class
	 */
	@Test
	public void testDeleteCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) {
						Category category = new Category("TestCategory");
						category.save();
						Coupon c = new Coupon("TestCoupon", 55.4, new Date(), "picturePath", category, "description","remark");
						c.status = Coupon.Status.ACTIVE;
						c.save();
						Coupon.delete(c.id);
						browser.goTo("http://localhost:3333/");
						assertThat(browser.pageSource()).doesNotContain("TestCoupon");

					}
				});

	}

	/**
	 * Testing coupon search
	 */
	@Test
    public void testSearch() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
				Category category = new Category("TestCategory");
				category.save();
            	Coupon mars = new Coupon("Mars", 55.4, new Date(), "picturePath", category, "description","remark");
				mars.status = Coupon.Status.ACTIVE;
				mars.save();
				Coupon jupiter = new Coupon("Jupiter", 55.4, new Date(), "picturePath", category, "description","remark");
				jupiter.status = Coupon.Status.ACTIVE;
				jupiter.save();
                browser.goTo("http://localhost:3333/search?q=mars");
                assertThat(browser.pageSource()).contains("Mars");
                assertThat(browser.pageSource()).doesNotContain("Jupiter");
            }
        });
    }


	
	/**
	 * Test if a expired coupon can be bought
	 */
	@Test
	public void testExpiredCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) throws ParseException {
                Category category = new Category("TestCategory");
				category.save();		
			    Company company = new Company("Company", "email", "password", new Date(), "logo", "adress", "city", "contact");
			    company.save();
				DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
				Date expiredDate = df.parse("08/10/1988");		
				Coupon coupon = 	new Coupon("TestCoupon", 55.4, expiredDate, "picturePath",category, "Test description", "remark",2, 5, new Date(), company);	
				coupon.status = Coupon.Status.EXPIRED;
				coupon.save();
				
				browser.goTo("http://localhost:3333/coupon/" + coupon.id);              
                assertThat(browser.pageSource()).doesNotContain("TestCoupon");
                assertThat(browser.pageSource()).doesNotContain("Test description");
            }
        });
		
	}
	
	/**
	 * Test maximal order quantity
	 */
	@Test
	public void testMaxOrder() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) throws ParseException {
						int maxOrder = 5;
						String invaliQuantity = "15";
						Category category = new Category("TestCategory");
						category.save();
						DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						Date expiration = df.parse("08/10/2050");
						Company company = new Company("Company", "email", "password", new Date(), "logo", "adress", "city", "contact");
						company.save();
		            	long id = Coupon.createCoupon("Hawai", 45, expiration, "pic", category, "desc", "rem", 2, maxOrder, null, company, Coupon.Status.ACTIVE);
						browser.goTo("http://localhost:3333/coupon/" + id);
						browser.fill("#quantity").with(invaliQuantity);
						browser.submit("#submit-buy");
						
						assertThat(browser.pageSource()).doesNotContain("Choose a way to pay");
						assertThat(browser.pageSource()).doesNotContain("Your order summary");
						assertThat(browser.pageSource()).doesNotContain("Pay with my PayPal account");

						assertThat(browser.pageSource()).contains("Hawai");
						assertThat(browser.pageSource()).contains("Enter the amount of coupons");
											
					}
				});
	}
	
	/**
	 * Testing the 'buy for user' feature
	 */
	@Test
	public void buyForUser() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())),
				new HtmlUnitDriver(), new Callback<TestBrowser>() {
					public void invoke(TestBrowser browser) throws ParseException {
						
						//creating coupon
						Category category = new Category("TestCategory");
						category.save();
						DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
						Date expiration = df.parse("08/10/2050");
						Company company = new Company("Company", "email", "password", new Date(), "logo", "adress", "city", "contact");
						company.save();
		            	long couponId = Coupon.createCoupon("Hawai", 45, expiration, "pic", category, "desc", "rem", 2, 5, new Date(), company, Coupon.Status.ACTIVE);
					
		            	//creating customer
						User lucky = new User("buyer", "lucky", new Date(), "female", "adress", "city", 
								"lucky@mail.com", HashHelper.createPassword("123456"), false,
								Play.application().configuration().getString("defaultProfilePicture"));
						lucky.save();
						EmailVerification.makeNewRecord(lucky.id, true);
						Pin pin = Pin.generatePin(lucky);
						
						//creating administrator
						User admin = new User("AdminNN", "worker", new Date(), "female", "adress", "city", 
								"adminNN@mail.com", HashHelper.createPassword("123456"), true,
								Play.application().configuration().getString("defaultProfilePicture"));
						admin.save();
						EmailVerification.makeNewRecord(admin.id, true);
						
						// Login as admin
						browser.goTo("http://localhost:3333/loginpage");
						browser.fill("#email").with("adminNN@mail.com");
						browser.fill("#password").with("123456");
						browser.submit("#submit-login");		
						
						//Go to coupon page and enter pin for user
						browser.goTo("http://localhost:3333/coupon/" + couponId);
						browser.fill("#pin").with(pin.code);
						browser.submit("#buy-for");		
						
						//Proceed page
						assertThat(browser.pageSource()).contains("lucky@mail.com");
						assertThat(browser.pageSource()).contains("Hawai");
						browser.fill("#quantity").with("1");
						browser.submit("#submit-buy-for");	
						
						assertThat(browser.pageSource()).contains("Transaction complete");
						assertThat(lucky.bought_coupons.size() > 0);

					}
				});
	}
	
	
	
	
}
