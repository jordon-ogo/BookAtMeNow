package ca.ualberta.cmput301f20t04.bookatmenow;

public class FireStoreMapping {
    static final String COLLECTIONS_USER = "Users";
    static final String COLLECTIONS_BOOK = "Books";
    static final String COLLECTIONS_NOTIFICATION = "Notifications";

    static final String USER_FIELDS_ID = "User ID";
    static final String USER_FIELDS_USERNAME = "Username";
    static final String USER_FIELDS_PASSWORD = "Password";
    static final String USER_FIELDS_PHONE = "Phone";
    static final String USER_FIELDS_EMAIL = "EMail";
    static final String USER_FIELDS_ADDRESS = "Address";

    static final String BOOK_FIELDS_TITLE = "Title";
    static final String BOOK_FIELDS_AUTHOR = "Author";
    static final String BOOK_FIELDS_ISBN = "ISBN";
    static final String BOOK_FIELDS_STATUS = "Status";
    static final String BOOK_FIELDS_BORROWER = "Borrower";
    static final String BOOK_FIELDS_OWNER = "Owner";
    static final String BOOK_FIELDS_REQUESTS = "Requests";
    static final String BOOK_FIELDS_LOCATION = "HandoverLocation";
    static final String BOOK_FIELDS_IMAGE = "Image";
    static final String BOOK_FIELDS_DESCRIPTION = "Description";
    static final String BOOK_FIELDS_RETURNING = "Returning";

    static final String NOTIFICATION_FIELDS_RECEIVER = "Receiver";
    static final String NOTIFICATION_FIELDS_SENDER = "Sender";
    static final String NOTIFICATION_FIELDS_TYPE = "Type";
    static final String NOTIFICATION_FIELDS_BOOK = "Book";
    static final String NOTIFICATION_FIELDS_TIMESTAMP = "Timestamp";

    static final String BOOK_STATUS_AVAILABLE = "Available";
    static final String BOOK_STATUS_REQUESTED = "Requested";
    static final String BOOK_STATUS_ACCEPTED = "Accepted";
    static final String BOOK_STATUS_BORROWED = "Borrowed";
    static final String BOOK_STATUS_UNAVAILABLE = "Unavailable";


    /**
     * Private class so this is not initialised by accident
     */
    private FireStoreMapping() {}
}
