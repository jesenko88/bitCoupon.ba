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
	
	
	//  -------------------------------SANELA----------------------------------------------
	@Test
    public void testFilterPrice() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                browser.fill("#start_price").with("1");
                browser.fill("#end_price").with("21");
                browser.submit("#priceSubmit");
                assertThat(browser.pageSource()).doesNotContain("Dvije noći za dvoje u Hotelu Sunce Neum");
                assertThat(browser.pageSource()).contains("Only 20.00 KM");  //<<<
            }
        });
    }
	
//	@Test
//    public void testFilterPrice2() {
//        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
//            public void invoke(TestBrowser browser) {
//                browser.goTo("http://localhost:3333");
//                browser.fill("#start_price").with("1");
//                browser.fill("#end_price").with("21");
//                browser.submit("#priceSubmit");
//                assertThat(browser.pageSource()).contains("Only 20.00 KM");
//            }
//        });
//    }
	
	@Test
    public void testFilterDate() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                browser.fill("#datepicker").with("02052015");
                browser.submit("#datepicker");
                assertThat(browser.pageSource()).doesNotContain("Dvije noći za dvoje u Hotelu Sunce Neum");
            }
        });
    }
	
	@Test
    public void testFilterCategory() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                browser.click("#category");
                browser.click("#category1");
                browser.submit("#categorySubmit");
                assertThat(browser.pageSource()).doesNotContain("Dvije noći za dvoje u Hotelu Sunce Neum");
                assertThat(browser.pageSource()).doesNotContain("Only 20.00 KM"); //<<<<
            }
        });
    }
	
//	@Test
//    public void testFilterCategory2() {
//        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
//            public void invoke(TestBrowser browser) {
//                browser.goTo("http://localhost:3333");
//                browser.click("#category");
//                browser.click("#category1");
//                browser.submit("#categorySubmit");
//                assertThat(browser.pageSource()).doesNotContain("Only 20.00 KM");
//            }
//        });
//    }
	
	@Test
    public void testSortCategory1() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                browser.click("#orderby");
                browser.click("#categorySort");
                browser.submit("#sortSubmit");
                assertThat(browser.pageSource()).contains("Only 20.00 KM");
            }
        });
    }
	
	@Test
    public void testSortPrice() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                browser.click("#orderby");
                browser.click("#priceSort");
                browser.submit("#sortSubmit");
                assertThat(browser.pageSource()).contains("Only 20.00 KM");
            }
        });
    }
	

	@Test
    public void testSortDate() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                browser.click("#orderby");
                browser.click("#dateSort");
                browser.submit("#sortSubmit");
                assertThat(browser.pageSource()).contains("Only 20.00 KM");
            }
        });
    }
	
	
	@Test
	public void testExpiredCoupon() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333/coupon/2");
                assertThat(browser.pageSource()).contains("Coupon has expired");
                assertThat(browser.pageSource()).doesNotContain("Enter the amount of coupons");
            }
        });
		
	}
	
	
}
