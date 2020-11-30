package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * View requests on a book owned by logged in user.
 */
public class BookRequests extends AppCompatActivity {

    private ListView requesterList;
    private TextView noRequests;
    private TextView bookRequestsTitle;
    private Context context;

    private RequestAdapter requestAdapter;
    private LinkedList<User> bookRequests;

    private DBHandler db;

    Intent intent;

    private String isbn;
    private String bookName;

    final private static int CHECK_ISBN_SCAN = 0;
    final private static int REQUEST_LOCATION = 1;

    List<String> location;
    int acceptPosition;


    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, intent);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_requests);
        context = this;

        requesterList = findViewById(R.id.myBookReqs_listView_BookRequests);
        noRequests = findViewById(R.id.noRequested_TextView_BookRequests);
        bookRequestsTitle = findViewById(R.id.myBookReqs_BookTitle);

        intent = getIntent();
        isbn = intent.getStringExtra(ProgramTags.PASSED_ISBN);

        bookRequests = new LinkedList<>();
        requestAdapter = new RequestAdapter(BookRequests.this, bookRequests);
        requesterList.setAdapter(requestAdapter);
        noRequests.setVisibility(View.INVISIBLE);
        bookRequestsTitle.setVisibility(View.INVISIBLE);

        db = new DBHandler();

        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {//got bool with isbn. Now get users requesting book

                bookName = book.getTitle();

                String requestsFor = "Requests for: ";
                SpannableString requestTitleString = new SpannableString(requestsFor + book.getTitle());
                requestTitleString.setSpan(new StyleSpan(Typeface.ITALIC), requestsFor.length() - 1, requestsFor.length() + book.getTitle().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                bookRequestsTitle.setText(requestTitleString);

                //There are no users requesting this book.  Make the "no requests" text visible.
                if (book.noRequests()) {
                    requesterList.setVisibility(View.GONE);
                    noRequests.setVisibility(View.VISIBLE);
                } else {
                    // Users are requesting the book. Get users who requested book
                    // and add them to the bookRequests linked list. Make title of book visible.
                    bookRequestsTitle.setVisibility(View.VISIBLE);
                    db.bookRequests(book.getRequests(), new OnSuccessListener<List<User>>() {
                        @Override
                        public void onSuccess(List<User> users) {
                            Log.e("AppInfo", "all users are: " + String.valueOf(users));
                            bookRequests.addAll(users);
                            requestAdapter.notifyDataSetChanged();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("AppInfo", "Failed to get book requesters");
                        }
                    });
                }

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("AppInfo", "Failed to get book with given isbn");
            }
        });

    }

    /**
     * If the accept button is pressed on a request, call functions to get handover location and
     * check books ISBN.
     * @param position of button press in list of requests.
     */
    public void clickedAccept(int position) {
        acceptPosition = position;
        getLocation();
    }

    /**
     * Removes a request in the local book object and then re-adds that book to the db with the
     * modified requests list. Also creates a notification for the rejected user and adds it to the
     * db.
     * @param position position in the user linked list of the request being removed.
     */
    public void removeRequest(int position) {
        final String requestUuid = bookRequests.get(position).getUserId();
        final String requestUsername = bookRequests.get(position).getUsername();
        Toast.makeText(context, "Removed request by " + requestUsername, Toast.LENGTH_SHORT).show();
        bookRequests.remove(position);
        requestAdapter.notifyDataSetChanged();

        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {
                book.deleteRequest(requestUuid);

                if (book.noRequests()) {
                    requesterList.setVisibility(View.GONE);
                    bookRequestsTitle.setVisibility(View.GONE);
                    noRequests.setVisibility(View.VISIBLE);
                    book.setStatus(ProgramTags.STATUS_AVAILABLE);
                }

                final String ownerUuid = book.getOwner().get(0);

                try {
                    db.addBook(book, new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Requested book could not be re-added to database!");
                        }
                    });

                    Notification n = new Notification();
                    n.setType(ProgramTags.NOTIFICATION_REJECT);
                    n.setReceiveUUID(requestUuid);
                    n.setSender(book.getOwner());
                    List<String> bookInfo = Arrays.asList(book.getIsbn(), book.getTitle());
                    n.setBook(bookInfo);

                    db.addNotification(n, new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Log.d(ProgramTags.DB_MESSAGE, "Reject notification was added to database!");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(ProgramTags.DB_MESSAGE, "Reject notification could not be added to database!");
                        }
                    });

                    removeNotification(ownerUuid, isbn);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Accepts a book request by a user.  Clears all other book requests and makes the accepted
     * user the borrower of the book.   Updates the books status to "accepted" and adds the
     * handover location.   Then re-adds the book to the db to update it.  Also creates a notification
     * for the accepted user and adds it to the db.
     * @param position position in the user linked list of the user whose request is accepted.
     */
    public void acceptRequest(int position) {
        final String borrowerUuid = bookRequests.get(position).getUserId();
        final String borrowerUsername = bookRequests.get(position).getUsername();
        Toast.makeText(context, "Accepted request by " + borrowerUsername, Toast.LENGTH_SHORT).show();
        final List<String> borrower = Arrays.asList(borrowerUuid, borrowerUsername);
        bookRequests.clear();
        requestAdapter.notifyDataSetChanged();
        requesterList.setVisibility(View.GONE);
        bookRequestsTitle.setVisibility(View.GONE);
        noRequests.setVisibility(View.VISIBLE);

        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {
                intent.putExtra(ProgramTags.PASSED_BORROWER, borrowerUsername);
                book.setBorrower(borrower);
                book.setStatus(ProgramTags.STATUS_ACCEPTED);
                book.setLocation(location);
                book.clearRequests();

                final String ownerUuid = book.getOwner().get(0);

                try {
                    db.addBook(book, new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Log.d(ProgramTags.DB_MESSAGE, "Requested book was be re-added to database!");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Requested book could not be re-added to database!");
                        }
                    });

                    Notification n = new Notification();
                    n.setType(ProgramTags.NOTIFICATION_APPROVE);
                    n.setReceiveUUID(borrowerUuid);
                    n.setSender(book.getOwner());
                    List<String> bookInfo = Arrays.asList(book.getIsbn(), book.getTitle());
                    n.setBook(bookInfo);

                    db.addNotification(n, new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Log.d(ProgramTags.DB_MESSAGE, "Accept notification was added to database!");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(ProgramTags.DB_MESSAGE, "Accept notification could not be added to database!");
                        }
                    });

                    removeNotification(ownerUuid, isbn);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Launches the ScanBook activity and passes in the name and ISBN of the book to check that
     * the book being accepted is in the owners possession.
     */
    public void checkIsbn() {
        Intent i = new Intent(BookRequests.this, ScanBook.class);
        i.putExtra(ProgramTags.PASSED_ISBN, isbn);
        i.putExtra(ProgramTags.PASSED_BOOKNAME, bookName);
        i.putExtra(ProgramTags.SCAN_MESSAGE, "ScanExisting");
        startActivityForResult(i, CHECK_ISBN_SCAN);
    }

    /**
     * Launches the GeoLocation activity so that the owner can select and handover location for an
     * accepted book request.
     */
    public void getLocation() {
        Intent i = new Intent(BookRequests.this, GeoLocation.class);
        i.putExtra(ProgramTags.LOCATION_PURPOSE, "getLocation");
        i.putExtra(ProgramTags.LOCATION_MESSAGE, "SelectHandover");
        i.putExtra(ProgramTags.PASSED_BOOKNAME, bookName);
        startActivityForResult(i, REQUEST_LOCATION);
    }

    /**
     * Gets the list of notifications for the current user and then loops through them.   If there
     * are any request notifications that the current book is being requested, delete those as they are
     * not necessary anymore.
     * @param uuid uuid of the current user.
     * @param isbn isbn of the book that the notification pertains to.
     */
    private void removeNotification(final String uuid, final String isbn) {
        final String type = ProgramTags.NOTIFICATION_REQUEST;
        db.getNotifications(uuid, new OnSuccessListener<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notificationList) {
                for(final Notification n : notificationList) {
                    if(n.getType().equals(type) && n.getBook().get(0).equals(isbn)) {
                        db.removeNotification(n.getSelfUUID(), new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                Log.d(ProgramTags.DB_MESSAGE, String.format("Notification %s has been removed.", n.getReceiveUUID()));
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(ProgramTags.DB_ERROR, String.format("Notification %s could not be removed.", n.getReceiveUUID()));
                            }
                        });
                    }
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(ProgramTags.DB_ERROR, String.format("Could not retrieve notifications for %s", uuid));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                // If the GeoLocation activity completes successfully, get the location that was
                // selected.
                case REQUEST_LOCATION:
                    String lat = data.getStringExtra("lat");
                    String lng = data.getStringExtra("lng");
                    location = Arrays.asList(lat, lng);
                    checkIsbn();
                    break;

                // If the ScanBook activity completes successfully, call the acceptRequest function.
                case CHECK_ISBN_SCAN:
                    acceptRequest(acceptPosition);
                    break;
            }
        }
    }
}