package ca.ualberta.cmput301f20t04.bookatmenow;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    @Test
    public void testUserBasic() {
        User user = new User("name", "password", "email@email.com");
        assertEquals("name", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("email@email.com", user.getEmail());
    }

    @Test
    public void testUserWithPhone() {
        User user = new User("name", "password", "1234567", "email@email.com");
        assertEquals("name", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("1234567", user.getPhone());
        assertEquals("email@email.com", user.getEmail());
    }

    @Test
    public void testUserWithAddress() {
        User user = new User("name", "password", "1234 House", "email@email.com");
        assertEquals("name", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("1234 House", user.getAddress());
        assertEquals("email@email.com", user.getEmail());
    }

    @Test
    public void testUserComplete() {
        User user = new User("name", "password", "1234567","email@email.com", "1234 House");
        assertEquals("name", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("1234567", user.getPhone());
        assertEquals("1234 House", user.getAddress());
        assertEquals("email@email.com", user.getEmail());
    }

    @Test
    public void testUserChange() {
        User user = new User("name", "password", "1234567", "email@email.com", "1234 House");
        assertEquals("name", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("1234567", user.getPhone());
        assertEquals("1234 House", user.getAddress());
        assertEquals("email@email.com", user.getEmail());

        user.setUsername("new name");
        user.setPassword("newpw");
        user.setPhone("5678910");
        user.setAddress("5678 Apartment");
        user.setEmail("newemail@email.com");

        assertEquals("new name", user.getUsername());
        assertEquals("newpw", user.getPassword());
        assertEquals("5678910", user.getPhone());
        assertEquals("5678 Apartment", user.getAddress());
        assertEquals("newemail@email.com", user.getEmail());
    }
}
