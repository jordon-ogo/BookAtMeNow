package ca.ualberta.cmput301f20t04.bookatmenow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SortTest {
    private ArrayList<Book> mockDatabase() {
        User testBorrower = new User("test_borrower", "345def", "test@testing.com");
        User testOwner = new User("test_owner", "asdlfkj", "mine@mine.com");

        ArrayList<Book> database = new ArrayList<>(Arrays.asList(
                new Book(
                        "A Tale of Two Cities",
                        "Charles Dickens",
                        "9781788280587",
                        "Available",
                        Arrays.asList(testOwner.getUserId(), testOwner.getUsername())
                ),
                new Book(
                        "Neuromancer",
                        "William Gibson",
                        "9780441569595",
                        "Borrowed",
                        Arrays.asList(testOwner.getUserId(), testOwner.getUsername()),
                        Arrays.asList(testBorrower.getUserId(), testBorrower.getUsername())),
                new Book(
                        "Dune",
                        "Frank Herbert",
                        "9780441172719",
                        "Requested",
                        Arrays.asList(testOwner.getUserId(), testOwner.getUsername())),
                new Book(
                        "C Programming: A Modern Approach",
                        "K.N. King",
                        "9780393979503",
                        "Accepted",
                        Arrays.asList(testOwner.getUserId(), testOwner.getUsername()),
                        Arrays.asList(testBorrower.getUserId(), testBorrower.getUsername())),
                new Book(
                        "The Rust Programming Language",
                        "Steve Klabnik",
                        "9781718500440",
                        "Available",
                        Arrays.asList(testOwner.getUserId(), testOwner.getUsername())),
                new Book(
                        "Introduction to Algorithms",
                        "CLRS",
                        "9780262033848",
                        "Invalid", // should default to Available status
                        Arrays.asList(testOwner.getUserId(), testOwner.getUsername()))
        ));

        return database;
    }

    @Test
    public void sortTest() {
        ArrayList<Book> database = mockDatabase();
        for (BookAdapter.CompareBookBy.SortOption option :
                BookAdapter.CompareBookBy.SortOption.values())
        {
            Collections.sort(database, new BookAdapter.CompareBookBy(option));

            String prev = "";
            String cur = "";
            for (int i = 0; i < database.size(); ++i) {
                switch (option) {
                    case TITLE:
                        cur = database.get(i).getTitle();
                        break;
                    case AUTHOR:
                        cur = database.get(i).getAuthor();
                        break;
                    case ISBN:
                        cur = database.get(i).getIsbn();
                        break;
                }
                if (!prev.isEmpty()) {
                    assertTrue(prev.compareTo(cur) <= 0);
                }
            }
        }
    }
}
