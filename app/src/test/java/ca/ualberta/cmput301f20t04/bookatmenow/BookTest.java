package ca.ualberta.cmput301f20t04.bookatmenow;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class BookTest {
    @Test
    public void testBookBasic() {
        ArrayList<String> owner = new ArrayList<String>();
        owner.add("Michelle");
        owner.add("18f298ca-h345-9i3r-1234-e9855cb320c8");
        Book book = new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "9780439554930", "Available", owner);
        assertEquals("Harry Potter and the Sorcerer's Stone", book.getTitle());
        assertEquals("J.K. Rowling", book.getAuthor());
        assertEquals("9780439554930", book.getIsbn());
        assertEquals("Available", book.getStatus());
        assertEquals(owner, book.getOwner());
    }

    @Test
    public void testBookComplete() {
        ArrayList<String> owner = new ArrayList<String>();
        owner.add("Michelle");
        owner.add("18f298ca-h345-9i3r-1234-e9855cb320c8");
        ArrayList<String> borrower = new ArrayList<String>();
        borrower.add("Andy");
        borrower.add("18f298ca-y230-0d2d-9842-e9855cb320c8");
        Book book = new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "9780439554930", "Accepted", owner, borrower);
        assertEquals("Harry Potter and the Sorcerer's Stone", book.getTitle());
        assertEquals("J.K. Rowling", book.getAuthor());
        assertEquals("9780439554930", book.getIsbn());
        assertEquals("Accepted", book.getStatus());
        assertEquals(owner, book.getOwner());
        assertEquals(borrower, book.getBorrower());
    }

    @Test
    public void testInvalidStatus() {
        ArrayList<String> owner = new ArrayList<String>();
        owner.add("Michelle");
        owner.add("18f298ca-h345-9i3r-1234-e9855cb320c8");
        ArrayList<String> borrower = new ArrayList<String>();
        borrower.add("Andy");
        borrower.add("18f298ca-y230-0d2d-9842-e9855cb320c8");
        Book book = new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "9780439554930", "Invalid", owner, borrower);
        assertEquals("Harry Potter and the Sorcerer's Stone", book.getTitle());
        assertEquals("J.K. Rowling", book.getAuthor());
        assertEquals("9780439554930", book.getIsbn());
        assertEquals("Accepted", book.getStatus());
        assertEquals(owner, book.getOwner());
        assertEquals(borrower, book.getBorrower());
    }

    @Test
    public void testBookChange() {
        ArrayList<String> owner = new ArrayList<String>();
        owner.add("Jack");
        owner.add("18f298ca-p235-0d4q-4562-e9855cb320c8");
        ArrayList<String> borrower = new ArrayList<String>();
        borrower.add("Mary");
        borrower.add("18f298ca-k293-h3j4-0123-e9855cb320c8");

        Book book = new Book("Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "9780439554930", "Accepted", owner, borrower);
        assertEquals("Harry Potter and the Sorcerer's Stone", book.getTitle());
        assertEquals("J.K. Rowling", book.getAuthor());
        assertEquals("9780439554930", book.getIsbn());
        assertEquals("Accepted", book.getStatus());

        book.setTitle("Harry Potter and the Chamber of Secrets");
        book.setAuthor("J.K. Rowling");
        book.setIsbn("9780439064873");
        book.setStatus("Accepted");
        book.setOwner(owner);
        book.setBorrower(borrower);

        assertEquals("Harry Potter and the Chamber of Secrets", book.getTitle());
        assertEquals("J.K. Rowling", book.getAuthor());
        assertEquals("9780439064873", book.getIsbn());
        assertEquals("Accepted", book.getStatus());
        assertEquals(owner, book.getOwner());
        assertEquals(borrower, book.getBorrower());
    }

}