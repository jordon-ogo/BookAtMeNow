package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated
 */
public class MyRequests extends AppCompatActivity {

    //Views
    private Button backButton;
    private TextView noRequests;

    //listView and its properties
    private ListView requestBookList;
    private BookAdapter booksAdapter;
    private ArrayList<Book> myRequestedBooks;

    private DBHandler db;

    private String uuid;

    public final static int REQUEST_ACTIVITY = 20;

    public void back(View view){
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        backButton = findViewById(R.id.back_button_MyRequests);
        requestBookList = findViewById(R.id.myReqs_listView_MyRequests);
        noRequests = findViewById(R.id.noRequested_TextView_MyRequests);

        myRequestedBooks = new ArrayList<>();
        booksAdapter = new BookAdapter(MyRequests.this, myRequestedBooks);
        requestBookList.setAdapter(booksAdapter);
        noRequests.setVisibility(View.INVISIBLE);

        db = new DBHandler();

        final Intent intent = getIntent();
        uuid = intent.getStringExtra("uuid");

        if (uuid != null) {
            //get all books that current user has requested to borrow
            db.userRequests(uuid, new OnSuccessListener<List<Book>>() {
                        @Override
                        public void onSuccess(List<Book> books) {
                            myRequestedBooks.addAll(books);
                            booksAdapter.notifyDataSetChanged();
                            Log.d(ProgramTags.DB_ALL_FOUND, "Found all user requested books.");
                            if(myRequestedBooks.size() == 0){//there are no books that this user requested to borrow
                                requestBookList.setVisibility(View.GONE);
                                noRequests.setVisibility(View.VISIBLE);
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(ProgramTags.DB_ERROR, "Failed to load all user requested books. " + e.toString());
                        }
                    }
            );
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}