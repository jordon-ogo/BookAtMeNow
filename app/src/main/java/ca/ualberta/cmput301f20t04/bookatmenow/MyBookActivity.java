package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

//import static ca.ualberta.cmput301f20t04.bookatmenow.R.id.editTextAuthor;

/**
 * Add / edit book to personal library.
 */
public class MyBookActivity extends AppCompatActivity {

    Context context;

    private Button scanButton;
    private Button saveChangesButton;
    private Button removeButton;
    private Button pendingRequestButton;
    private Button takeImageButton;
    private Button removeImageButton;
    private Button locationButton;
    private Button receiveReturnButton;
    private Button setStatusButton;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText isbnEditText;
    private TextView currentBorrower;

    private ImageView bookImage;

    private String initIsbn;
    private String bookName;

    public static final int CHANGE_BOOK_FROM_MAIN = 2;
    public static final int CHANGE_BOOK_FROM_MYBOOKS = 3;
    public static final int ADD_BOOK = 1;

    private DBHandler db;

    final private static int CHECK_ISBN_SCAN = 2;
    final private static int REQUEST_IMAGE_CAPTURE = 1;
    final private static int REQUEST_ISBN_SCAN = 0;
    final private static int VIEW_PENDING_REQUESTS = 3;


    private Uri myUri;
    private Boolean pictureTaken;
    private static final int PERMISSIONS_REQUEST_ACCESS_CAMERA = 1;
    private File photoFile;
    private String currentBookImage;
    private boolean removeImage;

    private StorageReference storageReference;
    private StorageReference getImageRef;

    private final long FILE_SIZE = 7000000;

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        this.finish();
    }

    public void takePicture(View view){
        if(getCameraPermissions() == true){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                photoFile = null;
                try {
                    photoFile = createImageFile();
                    removeImage = false;
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    myUri = FileProvider.getUriForFile(this,
                            "ca.ualberta.cmput301f20t04.bookatmenow",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, myUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        } else {

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void removeImage(View view){
        Toast.makeText(context, "Image removed. Save to make change permanent.", Toast.LENGTH_LONG).show();
        removeImage = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE: // if user took photo, set it in imageview
                    ImageView myImg = (ImageView) findViewById(R.id.myBook_imageview); //need to redefine it before changing it
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 3;
                    Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
                    myImg.setImageBitmap(myBitmap);
                    myImg.setRotation(90);
                    pictureTaken = true;
                    break;

                case REQUEST_ISBN_SCAN:
                    String newIsbn = data.getStringExtra("isbn");

                    if (!newIsbn.equals(initIsbn)) {
                        isbnEditText.setText(newIsbn);
                    }
                    break;

                case CHECK_ISBN_SCAN:
                    receiveBook();
                    break;

                case VIEW_PENDING_REQUESTS:
                    db.getBook(initIsbn, new OnSuccessListener<Book>() {
                        @Override
                        public void onSuccess(Book book) {
                            statusButtonSetup(book.getStatus());
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });

            }
        }
    }

    /**
     * Function for the owner to receive back a book they have lent out.   Resets book to default
     * available state and re-adds it to the db.
     */
    private void receiveBook() {
        db.getBook(initIsbn, new OnSuccessListener<Book>() {
            @Override
            public void onSuccess(Book book) {
                List<String> emptyList = Collections.singletonList("");
                final String ownerUuid = book.getOwner().get(0);
                book.setBorrower(emptyList);
                book.setReturning(false);
                book.setLocation(emptyList);
                book.setStatus(ProgramTags.STATUS_AVAILABLE);

                try {
                    db.addBook(book, new OnSuccessListener<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast toast = Toast.makeText(context, "You have received this book.", Toast.LENGTH_SHORT);
                            toast.show();
                            pendingRequestButton.setEnabled(true);
                            locationButton.setVisibility(View.INVISIBLE);
                            receiveReturnButton.setVisibility(View.INVISIBLE);
                            currentBorrower.setText("Not Borrowed");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Received book could not be re-added to database!");
                        }
                    });

                    removeNotification(ownerUuid, book.getIsbn());
                    statusButtonSetup(book.getStatus());

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
     * are any notifications that the current book is being returned, delete those as they are
     * not necessary anymore.
     * @param uuid uuid of the current user.
     * @param isbn isbn of the book that the notification pertains to.
     */
    private void removeNotification(final String uuid, final String isbn) {
        final String type = ProgramTags.NOTIFICATION_RETURN;
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

    private boolean getCameraPermissions() {
        /*
         * Request camera permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);//-1 means deny. 0 means allow
        if(grantResults[0] == -1){//permissions were denied
            Toast toast = Toast.makeText(this, "Camera permissions are needed to use the camera", Toast.LENGTH_LONG);
            toast.show();
        } else if(grantResults[0] == 0){//permissions were granted
            takePicture(null);
        } else{
            new Exception("grantResult returned incorrect value");
        }
    }

    /**
     * Set the text of the status button and its background tint based on the books current status.
     * @param status status of the book being viewed.
     */
    private void statusButtonSetup(String status) {
        //Setting padding around the button stops text from being cut off.
        setStatusButton.setPadding(1,1,1,1);
        switch (status) {
            case FireStoreMapping.BOOK_STATUS_AVAILABLE:
                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_AVAILABLE);
                if (Build.VERSION.SDK_INT >= 21) {
                    setStatusButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.confirm)));
                }
                break;
            case FireStoreMapping.BOOK_STATUS_UNAVAILABLE:
                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_UNAVAILABLE);
                if (Build.VERSION.SDK_INT >= 21) {
                    setStatusButton.setBackgroundTintList(null);
                }
                break;
            case FireStoreMapping.BOOK_STATUS_REQUESTED:
                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_REQUESTED);
                if (Build.VERSION.SDK_INT >= 21) {
                    setStatusButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.mid_way)));
                }
                break;
            case FireStoreMapping.BOOK_STATUS_ACCEPTED:
                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_ACCEPTED);
                if (Build.VERSION.SDK_INT >= 21) {
                    setStatusButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.deny)));
                }
                break;
            case FireStoreMapping.BOOK_STATUS_BORROWED:
                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_BORROWED);
                if (Build.VERSION.SDK_INT >= 21) {
                    setStatusButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.deny)));
                }
                break;

            default:
                setStatusButton.setVisibility(View.INVISIBLE);
                setStatusButton.setEnabled(false);
        }
    }

    private boolean checkFields() {
        return (!(titleEditText.getText().length() < 1) &&
                !(authorEditText.getText().length() < 1) &&
                (isbnEditText.getText().length() == 13));
    }

    private void toggleAllFields(int mode) {
        if (saveChangesButton.isEnabled()) {
            if (mode == 0) {
                scanButton.setEnabled(false);
                saveChangesButton.setEnabled(false);
                setStatusButton.setEnabled(false);
                takeImageButton.setEnabled(false);
                removeImageButton.setEnabled(false);
                titleEditText.setEnabled(false);
                authorEditText.setEnabled(false);
                isbnEditText.setEnabled(false);
            } else if (mode == 1) {
                saveChangesButton.setEnabled(false);
                setStatusButton.setEnabled(false);
                removeButton.setEnabled(false);
                pendingRequestButton.setEnabled(false);
                takeImageButton.setEnabled(false);
                removeImageButton.setEnabled(false);
                titleEditText.setEnabled(false);
                authorEditText.setEnabled(false);
            }
        } else if (!saveChangesButton.isEnabled()) {
            if (mode == 0) {
                scanButton.setEnabled(true);
                saveChangesButton.setEnabled(true);
                setStatusButton.setEnabled(true);
                takeImageButton.setEnabled(true);
                removeImageButton.setEnabled(true);
                titleEditText.setEnabled(true);
                authorEditText.setEnabled(true);
                isbnEditText.setEnabled(true);
            } else if (mode == 1) {
                saveChangesButton.setEnabled(true);
                setStatusButton.setEnabled(true);
                removeButton.setEnabled(true);
                pendingRequestButton.setEnabled(true);
                takeImageButton.setEnabled(true);
                removeImageButton.setEnabled(true);
                titleEditText.setEnabled(true);
                authorEditText.setEnabled(true);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_book);

        final Intent main = getIntent();
        db = new DBHandler();

        context = this;

        pictureTaken = false;
        removeImage = false;
        storageReference = FirebaseStorage.getInstance().getReference();

        scanButton = findViewById(R.id.myBook_scan_button);
        saveChangesButton = findViewById(R.id.myBook_save_change_button);
        setStatusButton = findViewById(R.id.myBook_set_status);
        removeButton = findViewById(R.id.myBook_remove_button);
        pendingRequestButton = findViewById(R.id.myBook_pending_request_button);
        takeImageButton = findViewById(R.id.myBook_take_picture_button);
        removeImageButton = findViewById(R.id.myBook_remove_picture_button);
        locationButton= findViewById(R.id.myBook_location_button);
        receiveReturnButton= findViewById(R.id.myBook_receive_button);

        titleEditText = findViewById(R.id.myBook_title_edittext);
        authorEditText = findViewById(R.id.myBook_author_edittext);
        isbnEditText = findViewById(R.id.myBook_isbn_edittext);
        currentBorrower = findViewById(R.id.myBook_current_borrower_textview);

        bookImage = findViewById(R.id.myBook_imageview);

        locationButton.setVisibility(View.INVISIBLE);
        receiveReturnButton.setVisibility(View.INVISIBLE);


        if (main.hasExtra(ProgramTags.PASSED_ISBN)) {
            initIsbn = main.getStringExtra(ProgramTags.PASSED_ISBN);
            isbnEditText.setVisibility(View.INVISIBLE);
            scanButton.setVisibility(View.INVISIBLE);

            db.getBook(initIsbn, new OnSuccessListener<Book>() {//not adding a book. editing a pre-existing book
                @Override
                public void onSuccess(final Book book) {
                    titleEditText.setText(book.getTitle());
                    authorEditText.setText(book.getAuthor());
                    isbnEditText.setText(book.getIsbn());

                    bookName = book.getTitle();

                    //If the book is accepted or borrowed, disable the pending requests button and
                    //show the button for the handover location.
                    if(book.getStatus().equals(ProgramTags.STATUS_ACCEPTED) ||
                            book.getStatus().equals(ProgramTags.STATUS_BORROWED)) {
                        pendingRequestButton.setEnabled(false);
                        locationButton.setVisibility(View.VISIBLE);
                    }

                    // If the book is borrowed and the borrower has clicked the return button,
                    // enable the receive return button so that the owner can scan there book to
                    // receive it back.
                    if(book.getReturning() && book.getStatus().equals(ProgramTags.STATUS_BORROWED)) {
                        receiveReturnButton.setVisibility(View.VISIBLE);
                    }

                    //If the book has been borrowed, display who borrowed it.
                    if(book.getBorrower() != null && book.getBorrower().size() == 2 && book.getStatus().equals(ProgramTags.STATUS_BORROWED)) {
                        String borrowedBy = "Borrowed by: ";
                        SpannableString borrowerString = new SpannableString(borrowedBy + book.getBorrower().get(1));
                        borrowerString.setSpan(new StyleSpan(Typeface.BOLD), 0, borrowedBy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        currentBorrower.setText(borrowerString);
                    }

                    currentBookImage = String.valueOf("images/" + book.getIsbn() + ".jpg");

                    getImageRef = storageReference.child(currentBookImage);//try to get image

                    getImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // file exists. Get image and paste it in imageview

                            getImageRef.getBytes(FILE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Log.i("AppInfo", "SUCCEED");
                                    ImageView myImg = (ImageView) findViewById(R.id.myBook_imageview); //need to redefine it before changing it
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inSampleSize = 3;
                                    Bitmap myBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
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
                            // File not found
                            currentBookImage = null;
                        }
                    });

                    statusButtonSetup(book.getStatus());

                    // If the remove button is pressed, check that the user wants to delete the book,
                    // and then remove the book and any images associated with it.
                    removeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog
                                    .Builder(MyBookActivity.this)
                                    .setTitle("Remove Book")
                                    .setMessage("Are you sure you want to permanently delete this book?")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if(currentBookImage != null){//there is a book image to delete
                                                getImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {//bok image has deleted. Now remove book from db

                                                        db.removeBook(initIsbn,
                                                            new OnSuccessListener<Boolean>() {
                                                                @Override
                                                                public void onSuccess(Boolean aBoolean) {
                                                                    setResult(RESULT_OK, main);
                                                                    finish();
                                                                }
                                                            },
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            });

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Error", e.toString());
                                                    }
                                                });
                                            } else {//no book image to delete. Just remove from db
                                                db.removeBook(initIsbn,
                                                    new OnSuccessListener<Boolean>() {
                                                        @Override
                                                        public void onSuccess(Boolean aBoolean) {
                                                            setResult(RESULT_OK, main);
                                                            finish();
                                                        }
                                                    },
                                                    new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    });
                                            }
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, null)
                                    .show();
                        }
                    });

                    setStatusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (book.getStatus().equals(FireStoreMapping.BOOK_STATUS_AVAILABLE)) {
                                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_UNAVAILABLE);
                                book.setStatus(FireStoreMapping.BOOK_STATUS_UNAVAILABLE);

                                if (Build.VERSION.SDK_INT >= 21) {
                                    setStatusButton.setBackgroundTintList(null);
                                }

                                Toast.makeText(context, "Book is now unavailable.", Toast.LENGTH_SHORT).show();
                            } else if (book.getStatus().equals(FireStoreMapping.BOOK_STATUS_UNAVAILABLE)) {

                                if (Build.VERSION.SDK_INT >= 21) {
                                    setStatusButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.confirm)));
                                }

                                setStatusButton.setText(FireStoreMapping.BOOK_STATUS_AVAILABLE);
                                book.setStatus(FireStoreMapping.BOOK_STATUS_AVAILABLE);
                                Toast.makeText(context, "Book is now available.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                     // If the location button is pressed launch the GeoLocation activity in view
                     // mode to display the books pickup location.
                    locationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(book.getLocation() != null && book.getLocation().size() == 2) {
                                Intent i = new Intent(MyBookActivity.this, GeoLocation.class);
                                i.putExtra(ProgramTags.LOCATION_PURPOSE, "view");
                                i.putExtra(ProgramTags.LOCATION_MESSAGE, "ViewHandover");
                                i.putExtra(ProgramTags.PASSED_BOOKNAME, bookName);
                                i.putExtra("lat", book.getLocation().get(0));
                                i.putExtra("lng", book.getLocation().get(1));
                                startActivity(i);
                            }
                        }
                    });

                    // If the receive return button is pressed, launch the ScanBook activity and
                    // pass in the books ISBN to check that the book being returned matches the book
                    // in the BD.
                    receiveReturnButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(MyBookActivity.this, ScanBook.class);
                            i.putExtra(ProgramTags.PASSED_ISBN, book.getIsbn());
                            i.putExtra(ProgramTags.PASSED_BOOKNAME, book.getTitle());
                            i.putExtra(ProgramTags.SCAN_MESSAGE, "ScanExisting");
                            startActivityForResult(i, CHECK_ISBN_SCAN);
                        }
                    });

                    // Ensure that all fields are correct and then save the changes to the book by
                    // updating the modified fields and re-adding the book to the db.
                    saveChangesButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(pictureTaken == true && removeImage == false){//picture was taken and user does not want it removed. Save it
                                Uri file = myUri;
                                currentBookImage = String.valueOf("images/" + book.getIsbn() + ".jpg");
                                final StorageReference riversRef = storageReference.child(currentBookImage);

                                // Disable buttons
                                toggleAllFields(1);

                                UploadTask uploadTask = riversRef.putBytes(compressImage(file));//uploading a file
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {//image uploaded. Now update db

                                        if (checkFields()) {
                                            book.setTitle(titleEditText.getText().toString().trim());
                                            book.setAuthor(authorEditText.getText().toString().trim());
                                            try {
                                                db.addBook(book, new OnSuccessListener<Boolean>() {
                                                    @Override
                                                    public void onSuccess(Boolean aBoolean) {
                                                        // send data back to main

                                                        setResult(RESULT_OK, main);
                                                        finish();
                                                    }
                                                }, new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(ProgramTags.DB_ERROR, "Book could not be added to database!");
                                                        setResult(RESULT_CANCELED, main);
                                                        finish();
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }

                                        } else {
                                            new AlertDialog.Builder(MyBookActivity.this)
                                                    .setTitle("Error!")
                                                    .setMessage("Please fill in all the required fields!")
                                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            toggleAllFields(1);
                                                        }
                                                    }).show();
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        // ...
                                        Log.i("AppInfo", "FAIL: " + exception.toString());
                                    }
                                });

                            }else {//no picture was taken and user does not want to remove image. Update book data normally

                                if(removeImage == true && currentBookImage != null){//regardless of if an image was taken, remove it if it exists
                                    getImageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {//bok image has deleted. Now remove book from db
                                            Log.i("AppInfo", "Image removed");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("Error", e.toString());
                                        }
                                    });
                                }

                                toggleAllFields(1);

                                if (checkFields()) {
                                    book.setTitle(titleEditText.getText().toString().trim());
                                    book.setAuthor(authorEditText.getText().toString().trim());
                                    try {
                                        db.addBook(book, new OnSuccessListener<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean aBoolean) {
                                                // send data back to main

                                                setResult(RESULT_OK, main);
                                                finish();
                                            }
                                        }, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(ProgramTags.DB_ERROR, "Book could not be added to database!");
                                                setResult(RESULT_CANCELED, main);
                                                finish();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    new AlertDialog.Builder(MyBookActivity.this)
                                            .setTitle("Error!")
                                            .setMessage("Please fill in all the required fields!")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    toggleAllFields(1);
                                                }
                                            }).show();
                                }
                            }//end of entire if else

                        }
                    });

                    // Takes user to the BookRequests activity to view any pending requests that book
                    // may have.
                    pendingRequestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(MyBookActivity.this, BookRequests.class);
                            i.putExtra(ProgramTags.PASSED_ISBN, initIsbn);
                            startActivityForResult(i,VIEW_PENDING_REQUESTS);
                        }
                    });



                    titleEditText.setText(book.getTitle());

                    authorEditText.setText(book.getAuthor());

                    pictureTaken = false;
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(ProgramTags.DB_ERROR, "Book could not be found!" + e.toString());
                }
            });
        } else {//adding a new book
            final String uuid = main.getStringExtra(ProgramTags.PASSED_UUID);
            final String username = main.getStringExtra(ProgramTags.PASSED_USERNAME);
            final Book newBook = new Book();
            newBook.setStatus(FireStoreMapping.BOOK_STATUS_AVAILABLE);

            pendingRequestButton.setVisibility(View.INVISIBLE);
            removeButton.setVisibility(View.INVISIBLE);
            currentBorrower.setVisibility(View.INVISIBLE);
            setStatusButton.setText(FireStoreMapping.BOOK_STATUS_AVAILABLE);

            setStatusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (newBook.getStatus().equals(FireStoreMapping.BOOK_STATUS_AVAILABLE)) {
                        setStatusButton.setText(FireStoreMapping.BOOK_STATUS_UNAVAILABLE);
                        newBook.setStatus(FireStoreMapping.BOOK_STATUS_UNAVAILABLE);

                        if(Build.VERSION.SDK_INT >= 21) {
                            setStatusButton.setBackgroundTintList(null);
                        }

                        Toast.makeText(context, "Book is now unavailable.", Toast.LENGTH_SHORT).show();
                    } else {
                        setStatusButton.setText(FireStoreMapping.BOOK_STATUS_AVAILABLE);
                        newBook.setStatus(FireStoreMapping.BOOK_STATUS_AVAILABLE);

                        if(Build.VERSION.SDK_INT >= 21) {
                            setStatusButton.setBackgroundTintList(null);
                        }

                        Toast.makeText(context, "Book is now available.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Launches the ScanBook activity to get a ISBN from the books barcode.
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MyBookActivity.this, ScanBook.class);
                    i.putExtra(ProgramTags.SCAN_MESSAGE, "ScanNew");
                    startActivityForResult(i, REQUEST_ISBN_SCAN);
                }
            });

            // Checks that all the information is good and then adds the book to the db.
            saveChangesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(pictureTaken == true && checkFields()){//picture was taken. Save it
                        Uri file = myUri;
                        currentBookImage = String.valueOf("images/" + isbnEditText.getText().toString().trim() + ".jpg");
                        final StorageReference riversRef = storageReference.child(currentBookImage);

                        // Disable buttons
                        toggleAllFields(0);

                        UploadTask uploadTask = riversRef.putBytes(compressImage(file));//uploading a file
                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                if (checkFields()) {

                                    newBook.setTitle(titleEditText.getText().toString().trim());
                                    Log.d(ProgramTags.BOOK_DATA, String.format("Book title set to %s", newBook.getTitle()));

                                    newBook.setAuthor(authorEditText.getText().toString().trim());
                                    Log.d(ProgramTags.BOOK_DATA, String.format("Book author set to %s", newBook.getAuthor()));

                                    newBook.setIsbn(isbnEditText.getText().toString().trim());
                                    Log.d(ProgramTags.BOOK_DATA, String.format("Book isbn set to %s", newBook.getIsbn()));

                                    List<String> owner = Arrays.asList(uuid, username);
                                    newBook.setOwner(owner);

                                    Log.d(ProgramTags.BOOK_DATA, String.format("Book owner set to %s", newBook.getOwner()));

                                    try {
                                        db.addBook(newBook,
                                                new OnSuccessListener<Boolean>() {
                                                    @Override
                                                    public void onSuccess(Boolean aBoolean) {
                                                        Toast.makeText(getApplicationContext(), "Book added.", Toast.LENGTH_LONG).show();
                                                        setResult(RESULT_OK, main);
                                                        finish();
                                                    }
                                                },
                                                new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(), "Failed to save data, please try again.", Toast.LENGTH_LONG).show();
                                                        Log.d(ProgramTags.DB_ERROR, "Failed to add new book from MyBookActivity.");
                                                    }
                                                });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    new AlertDialog.Builder(MyBookActivity.this)
                                            .setTitle("Error!")
                                            .setMessage("Please fill in all the required fields!")
                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    toggleAllFields(0);
                                                }
                                            }).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Log.i("AppInfo", "FAIL: " + exception.toString());
                            }
                        });

                    } else {//no picture taken. Update db normally
                        toggleAllFields(0);
                        if (checkFields()) {

                            newBook.setTitle(titleEditText.getText().toString().trim());
                            Log.d(ProgramTags.BOOK_DATA, String.format("Book title set to %s", newBook.getTitle()));

                            newBook.setAuthor(authorEditText.getText().toString().trim());
                            Log.d(ProgramTags.BOOK_DATA, String.format("Book author set to %s", newBook.getAuthor()));

                            newBook.setIsbn(isbnEditText.getText().toString().trim());
                            Log.d(ProgramTags.BOOK_DATA, String.format("Book isbn set to %s", newBook.getIsbn()));

                            List<String> owner = Arrays.asList(uuid, username);
                            newBook.setOwner(owner);

                            Log.d(ProgramTags.BOOK_DATA, String.format("Book owner set to %s", newBook.getOwner()));


                            try {
                                db.addBook(newBook,
                                        new OnSuccessListener<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean aBoolean) {
                                                Toast.makeText(getApplicationContext(), "Book added.", Toast.LENGTH_LONG).show();
                                                setResult(RESULT_OK, main);
                                                finish();
                                            }
                                        },
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getApplicationContext(), "Failed to save data, please try again.", Toast.LENGTH_LONG).show();
                                                Log.d(ProgramTags.DB_ERROR, "Failed to add new book from MyBookActivity.");
                                            }
                                        });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            new AlertDialog.Builder(MyBookActivity.this)
                                    .setTitle("Error!")
                                    .setMessage("Please fill in all the required fields!")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            toggleAllFields(0);
                                        }
                                    }).show();
                        }
                    }
                }
            });

        }
    }


    /**
     * Compresses an image for easier and quicker upload to firebase.
     * @param file uniform resource identifier for the image file being compressed.
     * @return byte array of the compressed image.
     */
    public byte[] compressImage(Uri file) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream oArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, oArray);

        return oArray.toByteArray();
    }
}