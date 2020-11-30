package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * List all books, list logged in user's books, list all books borrowed/requested by user, search/filter books.
 */
public class MainActivity extends AppCompatActivity {

    final private static int VIEW_NOTIFICATIONS = 10;
    final private static int VIEW_ABOOK = 11;

    private FloatingActionButton addBookButton;
    private FloatingActionButton editProfileButton;
    private FloatingActionButton viewInboxButton;

    private ImageButton sortButton;
    private Button searchButton;
    private FloatingActionButton filterButton;

    private Animation slideOnLeft;
    private Animation slideOffRight;
    private Animation slideOffLeft;
    private Animation slideOnRight;

    private EditText searchEditText;

    private TabLayout filterTabs;

    private String uuid;
    private String username;

    BookAdapter.CompareBookBy.SortOption sortOption;

    List<String> filterTerms;

    private enum MainActivityViews{
        ALL_BOOKS,
        MY_BOOKS,
        BORROWED,
        REQUESTED;

        /**
         * Allow this enum to be used like a C enum.
         *
         * @param i
         *      The equivalent integer to the MainActivityView
         * @return
         *      The corresponding enum value
         */
        public static MainActivityViews fromInt(int i) {
            switch (i) {
                default:
                case 0:
                    return ALL_BOOKS;
                case 1:
                    return MY_BOOKS;
                case 2:
                    return BORROWED;
                case 3:
                    return REQUESTED;
            }
        }

        public int toInt() {
            switch (this) {
                default:
                case ALL_BOOKS:
                    return 0;
                case MY_BOOKS:
                    return 1;
                case BORROWED:
                    return 2;
                case REQUESTED:
                    return 3;
            }
        }
    }
    private MainActivityViews currentView;

    ListView bookList;
    BookAdapter allBooksAdapter;
    ArrayList<Book> filteredBooks;
    DBHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // keyboard hiding (adapted from https://stackoverflow.com/a/17789187)
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        Log.d(ProgramTags.TEST_TAG, String.valueOf(MainActivityViews.MY_BOOKS));

        db = new DBHandler();

        bookList = findViewById(R.id.book_list);
        filteredBooks = new ArrayList<>();
        allBooksAdapter = new BookAdapter(MainActivity.this, filteredBooks);
        bookList.setAdapter(allBooksAdapter);

        uuid = getIntent().getStringExtra(FireStoreMapping.USER_FIELDS_ID);
        username = getIntent().getStringExtra(FireStoreMapping.USER_FIELDS_USERNAME);

        sortOption = BookAdapter.CompareBookBy.SortOption.TITLE;

        addBookButton = findViewById(R.id.floating_add);
        addBookButton.setVisibility(View.INVISIBLE);

        filterButton = findViewById(R.id.floating_filter);
        filterButton.setEnabled(false);
        filterButton.setVisibility(View.INVISIBLE);
        filterTerms = new ArrayList<>();

        // animation adapted from https://stackoverflow.com/a/44145485
        slideOnLeft = AnimationUtils.loadAnimation(this, R.anim.slide_on_left);
        slideOffRight = AnimationUtils.loadAnimation(this, R.anim.slide_off_right);
        slideOffLeft = AnimationUtils.loadAnimation(this, R.anim.slide_off_left);
        slideOnRight= AnimationUtils.loadAnimation(this, R.anim.slide_on_right);

        db.getAllBooks(new OnSuccessListener<List<Book>>() {
                           @Override
                           public void onSuccess(List<Book> books) {
//                               filteredBooks.clear();
//                               filteredBooks.addAll(books);
//                               allBooksAdapter.sort(sortOption);
                               setViewMode(BookAdapter.ViewMode.ALL, books);
                               Log.d(ProgramTags.DB_ALL_FOUND, "All books in database successfully found");
                               setUi(filteredBooks);
                           }
                       },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Set the user interface according to which tab is selected.
     * @param filteredBooks
     */
    private void setUi(final ArrayList<Book> filteredBooks) {
        // menu buttons
        editProfileButton = findViewById(R.id.floating_edit_profile);
        viewInboxButton = findViewById(R.id.floating_view_inbox);
        searchButton = findViewById(R.id.search_btn);
        searchEditText = findViewById(R.id.search_bar);

        currentView = MainActivityViews.ALL_BOOKS;

        filterTabs = findViewById(R.id.filterTabs);

        sortButton = findViewById(R.id.sort);

        viewInboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, UserNotifications.class);
                i.putExtra(ProgramTags.PASSED_UUID, uuid);
                i.putExtra(ProgramTags.PASSED_USERNAME, username);
                startActivityForResult(i, VIEW_NOTIFICATIONS);
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                i.putExtra("uuid", uuid);
                startActivity(i);
            }
        });

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SortDialog(MainActivity.this).show(getSupportFragmentManager(), "Sort Books");
            }
        });

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FilterDialog(MainActivity.this).show(getSupportFragmentManager(), "Filter Books");
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String search = searchEditText.getText().toString().toLowerCase().trim();
                if (search.length() > 0) {
                    List<String> searchTerms = Arrays.asList(search.split(" "));
                    db.searchBooks(searchTerms,
                            new OnSuccessListener<List<Book>>() {
                                @Override
                                public void onSuccess(List<Book> books) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    switch (currentView) {
                                        case REQUESTED:
                                            setViewMode(BookAdapter.ViewMode.REQUESTED, books);
                                            break;
                                        case BORROWED:
                                            setViewMode(BookAdapter.ViewMode.BORROWED, books);
                                            break;
                                        case MY_BOOKS:
                                            if (filterTerms.size() > 0) {
                                                setViewMode(BookAdapter.ViewMode.OWNED_FILTERED, books);
                                            } else {
                                                setViewMode(BookAdapter.ViewMode.OWNED, books);
                                            }
                                            break;
                                        default:
                                            setViewMode(BookAdapter.ViewMode.ALL, books);
                                            break;
                                    }
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    db.getAllBooks(new OnSuccessListener<List<Book>>() {
                                @Override
                                public void onSuccess(List<Book> books) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    switch (currentView) {
                                        case REQUESTED:
                                            setViewMode(BookAdapter.ViewMode.REQUESTED, books);
                                            break;
                                        case BORROWED:
                                            setViewMode(BookAdapter.ViewMode.BORROWED, books);
                                            break;
                                        case MY_BOOKS:
                                            if (filterTerms.size() > 0) {
                                                setViewMode(BookAdapter.ViewMode.OWNED_FILTERED, books);
                                            } else {
                                                setViewMode(BookAdapter.ViewMode.OWNED, books);
                                            }
                                            break;
                                        default:
                                            setViewMode(BookAdapter.ViewMode.ALL, books);
                                            break;
                                    }
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
        });

        filterTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentView = MainActivityViews.fromInt(tab.getPosition());
                Log.i("INDEX->", "Selected TAB Index - "+ tab.getPosition());

                switch (currentView) {
                    default:
                    case ALL_BOOKS:
                        db.getAllBooks(
                                new OnSuccessListener<List<Book>>() {
                                    @Override
                                    public void onSuccess(List<Book> books) {
                                        filterTerms.clear();
                                        setViewMode(BookAdapter.ViewMode.ALL, books);
                                    }
                                },
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                        break;

                    case MY_BOOKS:
                        db.getAllBooks(
                                new OnSuccessListener<List<Book>>() {
                                    @Override
                                    public void onSuccess(List<Book> books) {
                                        filterTerms.clear();
                                        addBookButton.setVisibility(View.VISIBLE);
                                        filterButton.setVisibility(View.VISIBLE);
                                        addBookButton.startAnimation(slideOnLeft);
                                        filterButton.startAnimation(slideOnRight);
                                        addBookButton.setEnabled(true);
                                        filterButton.setEnabled(true);
                                        setViewMode(BookAdapter.ViewMode.OWNED, books);
                                    }
                                },
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                       break;

                    case BORROWED:
                        db.getAllBooks(
                                new OnSuccessListener<List<Book>>() {
                                    @Override
                                    public void onSuccess(List<Book> books) {
                                        filterTerms.clear();
                                        setViewMode(BookAdapter.ViewMode.BORROWED, books);
                                    }
                                },
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                       break;

                    case REQUESTED:
                        if (uuid != null) {
                            db.userRequests(
                                    uuid,
                                    new OnSuccessListener<List<Book>>() {
                                        @Override
                                        public void onSuccess(List<Book> books) {
                                            filterTerms.clear();
                                            setViewMode(BookAdapter.ViewMode.REQUESTED, books);
                                        }
                                    },
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                            );
                        }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (MainActivityViews.fromInt(tab.getPosition()) == MainActivityViews.MY_BOOKS) {
                    disableButtons();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                Log.d(ProgramTags.TEST_TAG, "List Adapter");
                if (filteredBooks.get(pos).getOwner().get(0).equals(uuid)) {
                    Intent i = new Intent(MainActivity.this, MyBookActivity.class);
                    i.putExtra(ProgramTags.PASSED_ISBN, filteredBooks.get(pos).getIsbn());
                    i.putExtra(ProgramTags.PASSED_UUID, uuid);
                    i.putExtra(ProgramTags.PASSED_USERNAME, username);
                    if (currentView == MainActivityViews.MY_BOOKS) {
                        startActivityForResult(i, MyBookActivity.CHANGE_BOOK_FROM_MYBOOKS);
                    } else {
                        startActivityForResult(i, MyBookActivity.CHANGE_BOOK_FROM_MAIN);
                    }
                } else {
                    Intent i = new Intent(MainActivity.this, ABookActivity.class);
                    i.putExtra(ProgramTags.PASSED_ISBN, filteredBooks.get(pos).getIsbn());
                    i.putExtra(ProgramTags.PASSED_UUID, uuid);
                    i.putExtra(ProgramTags.PASSED_USERNAME, username);
                    startActivityForResult(i, VIEW_ABOOK);
                }
            }
        });

        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MyBookActivity.class);
                i.putExtra(ProgramTags.PASSED_UUID, uuid);
                i.putExtra(ProgramTags.PASSED_USERNAME, username);
                startActivityForResult(i, MyBookActivity.ADD_BOOK);
            }
        });
    }

    private void disableButtons() {
        addBookButton.setEnabled(false);
        filterButton.setEnabled(false);
        addBookButton.startAnimation(slideOffRight);
        filterButton.startAnimation(slideOffLeft);
        addBookButton.setVisibility(View.INVISIBLE);
        filterButton.setVisibility(View.INVISIBLE);
    }

    /**
     *
     * @param viewMode
     * @param allBooks
     */
    private void setViewMode(BookAdapter.ViewMode viewMode, List<Book> allBooks) {
        filteredBooks.clear();

        for (Book book : allBooks) {
            if (BookAdapter.checkUser(book, uuid, viewMode, filterTerms)) {
                filteredBooks.add(book);
            }
        }
        allBooksAdapter.sort(sortOption);
    }

    public void filterUpdate() {
        final BookAdapter.ViewMode mode;
        if (currentView == MainActivityViews.ALL_BOOKS) {
            mode = BookAdapter.ViewMode.ALL;
        } else {
            if (filterTerms.size() > 0) {
                mode = BookAdapter.ViewMode.OWNED_FILTERED;
            } else {
                mode = BookAdapter.ViewMode.OWNED;
            }
        }

        db.getAllBooks(
                new OnSuccessListener<List<Book>>() {
                    @Override
                    public void onSuccess(List<Book> books) {
                        setViewMode(mode, books);
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);
        db.getAllBooks(
                new OnSuccessListener<List<Book>>() {
                    @Override
                    public void onSuccess(List<Book> books) {
                        try {
                            if (requestCode == MyBookActivity.ADD_BOOK || requestCode == MyBookActivity.CHANGE_BOOK_FROM_MYBOOKS) {
                                if (filterTerms.size() > 0) {
                                    setViewMode(BookAdapter.ViewMode.OWNED_FILTERED, books);
                                } else {
                                    setViewMode(BookAdapter.ViewMode.OWNED, books);
                                }
                            } else if (currentView.equals(MainActivityViews.BORROWED)) {
                                setViewMode(BookAdapter.ViewMode.BORROWED, books);
                            } else if (currentView.equals(MainActivityViews.REQUESTED)) {
                                setViewMode(BookAdapter.ViewMode.REQUESTED, books);
                            } else {
                                setViewMode(BookAdapter.ViewMode.ALL, books);
                            }
                            Log.d(ProgramTags.GENERAL_SUCCESS, "Book list updated.");
                        } catch (Exception e) {
                            Log.d(ProgramTags.GENERAL_ERROR, String.format("Failed to update book list with error %s", e));
                            Toast.makeText(getApplicationContext(), "Failed to update book list.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}