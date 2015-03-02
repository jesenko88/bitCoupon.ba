import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
import static org.fluentlenium.core.filter.FilterConstructor.*;

public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
                assertThat(browser.pageSource()).contains("Welcome ");
                assertThat(browser.pageSource()).contains("Welcome to bitCoupon");
                assertThat(browser.pageSource()).contains("Home: ");
                assertThat(browser.pageSource()).contains("Registration");
                assertThat(browser.pageSource()).contains("Login");
                 assertThat(browser.pageSource()).contains("Logout");
            }
        });
    }
  /*
   @Test
    public void testRegistration() {
        running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
  
                browser.goTo("http://localhost:3333/");
                browser.fill("#username").with("tester");
                browser.fill("#email").with("test.testoviic@bitcamp.ba");
                browser.fill("#password").with("123456");
                browser.submit("#register");
                assertThat(browser.pageSource()).contains("Welcome tester");
                assertThat(browser.pageSource()).contains("Home: ");
                assertThat(browser.pageSource()).contains("Logout");

            }
        });
    }*/
   
   @Test
   public void testLogin() {
       running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
           public void invoke(TestBrowser browser) {
 
               browser.goTo("http://localhost:3333/");
               browser.fill("#username").with("tester");
               browser.fill("#password").with("123456");
               browser.submit("#login");
               assertThat(browser.pageSource()).contains("Welcome tester");
               assertThat(browser.pageSource()).contains("Home: ");
               assertThat(browser.pageSource()).contains("Logout");

           }
       });
   }
}
