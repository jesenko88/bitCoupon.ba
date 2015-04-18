import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import models.Category;
import models.Company;
import models.Coupon;

import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import play.test.*;
import play.libs.F.*;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;



public class IntegrationTest {

		
	/*
	 * Registration test //PROBLEM
	 */
	
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
						browser.goTo("http://localhost:3333/loginpage");
						assertThat(browser.pageSource().contains("LOGIN"));
						assertThat(browser.pageSource()).contains("Forgot password?");
						browser.fill("#email").with("jesenko.gavric@bitcamp.ba");
						browser.fill("#password").with("johndoe");
						browser.submit("#submit-login");
						assertThat(browser.pageSource()).contains("You are logged in as: jesenko.gavric@bitcamp.ba");
						assertThat(browser.pageSource()).contains("John");
						assertThat(browser.pageSource()).contains("Menu");
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
						c.status = true;
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
						c.status = true;
						c.save();
						Coupon.delete(c.id);
						browser.goTo("http://localhost:3333/");
						assertThat(browser.pageSource()).doesNotContain("TestCoupon");

					}
				});

	}

	
	@Test
    public void testSearch() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), new HtmlUnitDriver(), new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
				Category category = new Category("TestCategory");
				category.save();
            	Coupon mars = new Coupon("Mars", 55.4, new Date(), "picturePath", category, "description","remark");
				mars.status = true;
				mars.save();
				Coupon jupiter = new Coupon("Jupiter", 55.4, new Date(), "picturePath", category, "description","remark");
				jupiter.status = true;
				jupiter.save();
                browser.goTo("http://localhost:3333/search?q=mars");
                assertThat(browser.pageSource()).contains("Mars");
                assertThat(browser.pageSource()).doesNotContain("Jupiter");
            }
        });
    }


	
	
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
				Coupon coupon = 	new Coupon("TestCoupon", 55.4, expiredDate, "picturePath",category, "description", "remark",2, 5, new Date(), company);	
				coupon.status = true;
				coupon.save();
				
				browser.goTo("http://localhost:3333/coupon/" + coupon.id);              
                assertThat(browser.pageSource()).contains("Coupon has expired ");
                assertThat(browser.pageSource()).doesNotContain("Enter the amount of coupons");
            }
        });
		
	}
	
	
}
