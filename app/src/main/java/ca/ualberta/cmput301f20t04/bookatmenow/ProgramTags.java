package ca.ualberta.cmput301f20t04.bookatmenow;

public class ProgramTags {
    static final String TEST_TAG = "Test";

    static final String DB_ERROR = "DatabaseError";
    static final String DB_MESSAGE = "Database";
    static final String DB_TEST = "DatabaseTest";
    static final String DB_ALL_FOUND = "AllFound";
    static final String DB_USER_FOUND = "UserFound";

    static final String LOCATION_PURPOSE = "LocationPurpose";

    static final String BOOK_ERROR = "BookError";
    static final String NOTIFICATION_ERROR = "NotificationError";

    static final String BOOK_CHANGED = "BookChanged";
    static final String BOOK_DATA = "BookData";
    static final String BOOK_POS = "BookPosition";
    static final String PASSED_ISBN = "PassedISBN";
    static final String PASSED_BOOKNAME = "PassedBookname";
    static final String PASSED_UUID = "PassedUUID";
    static final String PASSED_USERNAME = "PassedUsername";
    static final String PASSED_BORROWER = "PassedBorrower";

    static final String SCAN_MESSAGE = "ScanMessage";
    static final String LOCATION_MESSAGE = "LocationMessage";

    static final String STATUS_AVAILABLE = "Available";
    static final String STATUS_REQUESTED = "Requested";
    static final String STATUS_ACCEPTED = "Accepted";
    static final String STATUS_BORROWED = "Borrowed";
    static final String STATUS_UNAVAILABLE = "Unavailable";

    static final String GENERAL_ERROR = "ERROR";
    static final String GENERAL_SUCCESS = "SUCCESS";

    static final String TYPE_OWNER = "Owner";
    static final String TYPE_BORROWER = "Borrower";

    static final String NOTIFICATION_REQUEST = "Request";
    static final String NOTIFICATION_APPROVE = "Approve";
    static final String NOTIFICATION_REJECT = "Reject";
    static final String NOTIFICATION_RETURN = "Return";

    private ProgramTags() {}
}
