package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * View A Book that's not owned by the logged in user.
 * View pickup location, optional book image, request / return / borrow actions.
 */
public class ABookActivity extends AppCompatActivity {

    private TextView aTitle;
    private TextView anAuthor;
    private TextView anIsbn;
    private TextView aStatus;
    private Button ownerButton;
    private Button requestButton;
    private Button borrowButton;
    private Button returnButton;
    private Button locationButton;
    private Context context;
    private Intent intent;

    private String isbn;
    private String bookName;
    private String owner_uuid;
    private String uuid;
    private String username;
    private List<String> location;

    final private static int CHECK_ISBN_SCAN = 0;

    private StorageReference storageReference;
    private StorageReference getImageRef;
    private final long FILE_SIZE = 5120*5120;

    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_book);
        context = this;
        intent = getIntent();

        isbn = intent.getStringExtra(ProgramTags.PASSED_ISBN);
        uuid = intent.getStringExtra(ProgramTags.PASSED_UUID);
        username = intent.getStringExtra(ProgramTags.PASSED_USERNAME);

        aTitle = findViewById(R.id.abook_title_textview);
        anAuthor = findViewById(R.id.abook_author_textview);
        anIsbn = findViewById(R.id.abook_isbn_textview);
        aStatus = findViewById(R.id.abook_status_textview);
        ownerButton = findViewById(R.id.abook_owner_button);
        requestButton = findViewById(R.id.abook_request_button);
        borrowButton = findViewById(R.id.abook_borrow_button);
        returnButton = findViewById(R.id.abook_return_button);
        locationButton = findViewById(R.id.abook_location_button);

        requestButton.setEnabled(false);
        borrowButton.setVisibility(View.INVISIBLE);
        returnButton.setVisibility(View.INVISIBLE);
        locationButton.setVisibility(View.INVISIBLE);

        db = new DBHandler();
        storageReference = FirebaseStorage.getInstance().getReference();

        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {

                String currentBookImage = String.valueOf("images/" + book.getIsbn() + ".jpg");
                bookName = book.getTitle();

                getImageRef = storageReference.child(currentBookImage);//try to get image

                getImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // file exists. Get image and paste it in imageview

                        getImageRef.getBytes(FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Log.i("AppInfo", "SUCCEED");
                                ImageView myImg = (ImageView) findViewById(R.id.aBook_imageView); //need to redefine it before changing it
                                Bitmap myBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                myImg.setImageBitmap(myBitmap);
                                myImg.setRotation(90);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {//if error = 404, then book does not have an image. Set currentBookImage to null
                                Log.i("AppInfo", "FAILED: " + e.toString());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // File not found. Do nothing
                    }
                });

                owner_uuid = book.getOwner().get(0);

                String title = "Title: ";
                SpannableString titleString = new SpannableString(title + book.getTitle());
                titleString.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                aTitle.setText(titleString);

                String author = "Author: ";
                SpannableString authorString = new SpannableString(author + book.getAuthor());
                authorString.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                anAuthor.setText(authorString);

                String isbn = "ISBN: ";
                SpannableString isbnString = new SpannableString(isbn + book.getIsbn());
                isbnString.setSpan(new StyleSpan(Typeface.BOLD), 0, isbn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                anIsbn.setText(isbnString);

                String status = "Status: ";
                String bookStatus = book.getStatus();
                SpannableString statusString = new SpannableString(status + bookStatus);
                statusString.setSpan(new StyleSpan(Typeface.BOLD), 0, status.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                aStatus.setText(statusString);

                //If user hasn't requested the book yet and the book is available or requested,
                //enable the request button.
                if(!book.checkForRequest(uuid) &&
                        (bookStatus.equals(ProgramTags.STATUS_AVAILABLE) ||
                        bookStatus.equals(ProgramTags.STATUS_REQUESTED))) requestButton.setEnabled(true);

                //If the current user is the borrower of the book being viewed, un-hide certain buttons.
                if (book.getBorrower() != null && book.getBorrower().size() == 2 && uuid.equals(book.getBorrower().get(0))) {
                    locationButton.setVisibility(View.VISIBLE);
                    location = book.getLocation();

                    if(book.getStatus().equals(ProgramTags.STATUS_ACCEPTED)) {
                        borrowButton.setVisibility(View.VISIBLE);
                    }

                    if(book.getStatus().equals(ProgramTags.STATUS_BORROWED)) {
                        borrowButton.setVisibility(View.VISIBLE);
                        borrowButton.setEnabled(false);
                        locationButton.setVisibility(View.VISIBLE);
                        returnButton.setVisibility(View.VISIBLE);

                        if(book.getReturning()) returnButton.setEnabled(false);
                    }
                }

                db.getUser(owner_uuid, new OnSuccessListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        ownerButton.setText(user.getUsername());
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        ownerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ABookActivity.this, AProfileActivity.class);
                i.putExtra(ProgramTags.PASSED_UUID, owner_uuid);
                startActivity(i);
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRequest();
            }
        });

        //On location button click check that the location exists and then view it in the
        //Geolocation activity.
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(location != null && location.size() == 2) {
                    Intent i = new Intent(ABookActivity.this, GeoLocation.class);
                    i.putExtra(ProgramTags.LOCATION_PURPOSE, "view");
                    i.putExtra(ProgramTags.LOCATION_MESSAGE, "ViewHandover");
                    i.putExtra(ProgramTags.PASSED_BOOKNAME, bookName);
                    i.putExtra("lat", location.get(0));
                    i.putExtra("lng", location.get(1));
                    startActivity(i);
                }
            }
        });

        borrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIsbn();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleReturn();
            }
        });

    }

    /**
     * If user requests a book, get the book from the db, add the request to the books requested list,
     * then re-add the book to the db. Also create a notification for the owner of the book and add
     * it to the db.
     */
    private void handleRequest() {
        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {
                book.addRequest(uuid);

                // Update the status textview to "requested"
                String status = "Status: ";
                String bookStatus = book.getStatus();
                SpannableString statusString = new SpannableString(status + bookStatus);
                statusString.setSpan(new StyleSpan(Typeface.BOLD), 0, status.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                aStatus.setText(statusString);


                try {
                    db.addBook(book, new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast toast = Toast.makeText(context, "You have requested this book.", Toast.LENGTH_SHORT);
                            toast.show();
                            requestButton.setEnabled(false);

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Requested book could not be re-added to database!");
                        }
                    });

                    Notification n = new Notification();
                    n.setType(ProgramTags.NOTIFICATION_REQUEST);
                    n.setReceiveUUID(book.getOwner().get(0));
                    List<String> requester = Arrays.asList(uuid, username);
                    n.setSender(requester);
                    List<String> bookInfo = Arrays.asList(book.getIsbn(), book.getTitle());
                    n.setBook(bookInfo);

                    db.addNotification(n, new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Log.d(ProgramTags.DB_MESSAGE, "Request notification was added to database!");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(ProgramTags.DB_MESSAGE, "Request notification could not be added to database!");
                        }
                    });

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
     * If user borrows a book, get the book from the db, set its status to borrowed, then re-add
     * the book to the db.
     */
    private void handleBorrow() {
        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {
                book.setStatus(ProgramTags.STATUS_BORROWED);

                String status = "Status: ";
                String bookStatus = book.getStatus();
                SpannableString statusString = new SpannableString(status + bookStatus);
                statusString.setSpan(new StyleSpan(Typeface.BOLD), 0, status.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                aStatus.setText(statusString);

                try {
                    db.addBook(book, new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast toast = Toast.makeText(context, "You have borrowed this book.", Toast.LENGTH_SHORT);
                            toast.show();
                            borrowButton.setEnabled(false);
                            returnButton.setVisibility(View.VISIBLE);

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Requested book could not be re-added to database!");
                        }
                    });
                    removeNotification(uuid, book.getIsbn());

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
     * Gets the list of notifications for the current user and then loops through them.   If there
     * are any notifications that the current users request for this book has been approved, delete
     * those as they are not necessary anymore.
     * @param uuid uuid of the current user.
     * @param isbn isbn of the book that the notification pertains to.
     */
    private void removeNotification(final String uuid, final String isbn) {
        final String type = ProgramTags.NOTIFICATION_APPROVE;
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

    /**
     * If borrower clicks to return a book, get the book from the db, set the "returning flag" to true,
     * then re-add the book to the db.  Also create a notification for the owner that the book is being
     * returned and add that notification to the db.
     */
    private void handleReturn() {
        db.getBook(isbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {
                book.setReturning(true);

                try {
                    db.addBook(book, new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast toast = Toast.makeText(context, "You are returning this book.", Toast.LENGTH_SHORT);
                            toast.show();
                            borrowButton.setEnabled(false);
                            returnButton.setEnabled(false);

                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Requested book could not be re-added to database!");
                        }
                    });

                    Notification n = new Notification();
                    n.setType(ProgramTags.NOTIFICATION_RETURN);
                    n.setReceiveUUID(book.getOwner().get(0));
                    List<String> borrower = Arrays.asList(uuid, username);
                    n.setSender(borrower);
                    List<String> bookInfo = Arrays.asList(book.getIsbn(), book.getTitle());
                    n.setBook(bookInfo);

                    db.addNotification(n, new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Log.d(ProgramTags.DB_MESSAGE, "Return notification was added to database!");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(ProgramTags.DB_MESSAGE, "Return notification could not be added to database!");
                        }
                    });

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
     * Scan the book being borrowed (ISBN of the scanned book must match the ISBN from the db for
     * that book.
     */
    public void checkIsbn() {
        Intent i = new Intent(ABookActivity.this, ScanBook.class);
        i.putExtra(ProgramTags.PASSED_ISBN, isbn);
        i.putExtra(ProgramTags.PASSED_BOOKNAME, bookName);
        i.putExtra(ProgramTags.SCAN_MESSAGE, "ScanExisting");
        startActivityForResult(i, CHECK_ISBN_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CHECK_ISBN_SCAN) {
            handleBorrow();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        this.finish();
    }
}