package ca.ualberta.cmput301f20t04.bookatmenow;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A BaseAdapter class specialized for a database of {@link Book}s that are currently being
 * requested from a given owner. With this adapter, the owner will be able to accept or reject
 * requests.
 *
 * @version 0.7
 * @deprecated
 */
public class RequestList extends BookList {
    private ArrayList<String> requesters;
    private ArrayList<String> displayNames;

    /**
     * Construct a view of all a given owner's pending requests.
     *
     * @param context
     *      The context of the calling activity, used to display objects on the screen
     * @param ownerName
     *      The UUID of the {@link User} whose requests are being managed
     */
    public RequestList(Context context, final String ownerName) {
        super(context, new LinkedList<Book>());

        // In the case of the request list, filteredBooks is represented as a LinkedList because it
        // is frequently added to and deleted from, and is never sorted, therefore not needing
        // random access.
        filteredBooks = new LinkedList<>();
        requesters = new ArrayList<>();
        displayNames = new ArrayList<>();

        final HashMap<String, String> ownerMap = new HashMap<>();
        db.getUser(ownerName, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                ownerMap.put(ProgramTags.DB_USER_FOUND, user.getUserId());

                Log.d(ProgramTags.DB_USER_FOUND, "User successfully found");
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(ProgramTags.DB_ERROR, "User could not be found!" + e.toString());
            }
        });

        if (ownerMap.isEmpty()) { return; }

        db.getAllBooks(new OnSuccessListener<List<Book>>() {
                    @Override
                    public void onSuccess(List<Book> books) {
                        for (Book book : books) {
                            if (book.getOwner().equals(ownerMap.get(ProgramTags.DB_USER_FOUND)) &&
                                    Book.StatusEnum.valueOf(book.getStatus()) ==
                                            Book.StatusEnum.Requested)
                            {
                                for (String requester : book.getRequests()) {
                                    filteredBooks.add(book);
                                    requesters.add(requester);
                                    db.getUser(requester, new OnSuccessListener<User>() {
                                        @Override
                                        public void onSuccess(User user) {
                                            String username = user.getUsername();
                                            if (username != null) {
                                                displayNames.add(username);
                                            } else {
                                                displayNames.add(user.getEmail());
                                            }
                                            Log.d(ProgramTags.DB_USER_FOUND,
                                                    "User successfully found");
                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(ProgramTags.DB_ERROR,
                                                    "User could not be found!" + e.toString());
                                        }
                                    });
                                }
                            }
                        }
                       Log.d(ProgramTags.DB_ALL_FOUND, "All books in database successfully found");
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(ProgramTags.DB_ERROR, "Not all books could be found!" + e.toString());
                    }
                });
    }

    /**
     * A required method from {@link android.widget.BaseAdapter} for displaying an element of the
     * internal list at a given position.
     *
     * @param position
     *      The position of the element to display from the internal list
     * @param convertView
     *      The external {@link View} in which to display the element's data
     * @param parent
     *      The {@link ViewGroup} containing the elements of the {@link android.widget.ListView}
     * @return
     *      The original given {@link View}, converted into a row of the internal list
     */
    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflate_helper(convertView, parent, R.layout.request_row);

        final Book book = filteredBooks.get(position);
        final String borrower = displayNames.get(position);

        final TextView title = convertView.findViewById(R.id.display_name_text);
        final TextView username = convertView.findViewById(R.id.display_name_text);

        title.setText(book.getTitle());
        username.setText(borrower);

        convertView.findViewById(R.id.confirm_button)
                .setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                book.setStatus(Book.StatusEnum.Borrowed.toString());
                db.usernameExists(borrower, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        db.getUser(s, new OnSuccessListener<User>() {
                            @Override
                            public void onSuccess(User user) {
                                List borrower = Arrays.asList(user.getUserId(), user.getUsername());
                                book.setBorrower(borrower);
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
//                db.addBook(book);

                refreshRequests();
            }
        });

        convertView.findViewById(R.id.reject_button)
                .setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                book.setStatus(Book.StatusEnum.Available.toString());
//                db.addBook(book);

                refreshRequests();
            }
        });

        return convertView;
    }

    /**
     * A helper method to remove all books whose current status is not Pending from the request
     * list.
     */
    private void refreshRequests() {
        Iterator<Book> bookIterator = filteredBooks.iterator();
        while (bookIterator.hasNext()) {
            Book book = bookIterator.next();
            if (Book.StatusEnum.valueOf(book.getStatus()) != Book.StatusEnum.Requested) {
                bookIterator.remove();
            }
        }
    }

    /**
     * A required method from {@link android.widget.BaseAdapter} for getting a unique identifying
     * long value from an element of the internal list at a given position. In this case, because
     * the same book can be in this list multiple times, and the order of the filteredList is never
     * changed, the book's position itself is its unique identifier.
     *
     * @param position
     *      The position in the internal filtered list from which to get a unique feature of the
     *      element
     * @return
     *      The book's position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }
}
