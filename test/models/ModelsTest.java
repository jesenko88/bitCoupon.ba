package models;

import java.util.List;

import org.junit.*;

import play.test.WithApplication;
import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class ModelsTest extends WithApplication {
	@Before
	public void setUp() {
		fakeApplication(inMemoryDatabase());
	}

	@Test
	public void testCreate() {
		User.create("test", "test.testovic@bitcamp.ba", "54321");
		User u = User.find(1);

		assertNotNull(u);
		assertEquals(u.username, "test");
		assertEquals(u.email, "test.testovic@bitcamp.ba");
		assertEquals(u.password, "54321");
	}

	@Test
	public void testFindNonExisting() {
		User u = User.find(13);

		assertNull(u);
	}

	@Test
	public void testDelete() {
		User.create("test", "test.testovic@bitcamp.ba","12345");
		User.delete(1);
		User b = User.find(1);
		assertNull(b);
	}
}
