package ca.ualberta.cmput301f20t04.bookatmenow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * An {@link ArrayAdapter} class specialized for a database of {@link Book}s to be owned, borrowed,
 * requested, or simply displayed.
 * <p>
 * Much of this class had to be deprecated due to idiosyncrasies with the way async databases work.
 *
 * @author Warren Stix
 * @see ArrayAdapter
 * @see android.widget.BaseAdapter
 * @version 1.6
 */
public class BookAdapter extends ArrayAdapter<Book> {
    /**
     * Identify the "View Mode" this adapter is launched in.
     *
     * @author Warren Stix
     * @see Book.StatusEnum
     * @version 0.1
     */
    enum ViewMode {
        /**
         * Display all {@link Book}s in the system
         */
        ALL,
        /**
         * Display the {@link Book}s owned by the {@link User} with a given UUID
         */
        OWNED,
        /**
         * Display the {@link Book}s being owned by the {@link User} with a given UUID and Status Filter
         */
        OWNED_FILTERED,
        /**
         * Display the {@link Book}s being borrowed by the {@link User} with a given UUID
         */
        BORROWED,
        /**
         * Display the {@link Book}s being requested by the {@link User} with a given UUID
         */
        REQUESTED;
    }

    /**
     * @deprecated
     */
    @NonNull private ViewMode viewMode;

    /**
     * @deprecated
     */
    @Nullable private String uuid;

    private final Context context;
    private ArrayList<Book> filteredBooks;

    /**
     * Construct a view of all books in the system.
     *
     * @param context
     *      The context of the calling activity, used to display objects on the screen
     */
    public BookAdapter(Context context, ArrayList<Book> filteredBooks) {
        super(context, 0, filteredBooks);

        this.context = context;
        this.filteredBooks = filteredBooks;
//        viewMode = ViewMode.ALL;
//        uuid = null;
    }

    /**
     *  Construct a view of books that a user owns, has borrowed, or has requested to borrow.
     *
     * @param context
     *      The context of the calling activity, used to display objects on the screen
     * @param viewMode
     *      The "view mode" of this list of books - indicates how many books in the system need to
     *      be displayed and re-displayed after filtering
     * @param uuid
     *      The UUID of the user whose books are being displayed
     */
    public BookAdapter(Context context, ArrayList<Book> filteredBooks, @NonNull ViewMode viewMode,
                       @Nullable String uuid)
    {
        super(context, 0, filteredBooks);

        this.context = context;
//        this.viewMode = viewMode;
//        this.uuid = uuid;

        // the following commented code is deprecated; it was broken by the implementation of the
        // database
//        db.getAllBooks(new OnSuccessListener<List<Book>>() {
//                    @Override
//                    public void onSuccess(List<Book> books) {
//
//                        for (Book book : books) {
//                            if (checkUser(book)) {
//                                filteredBooks.add(book);
//                            }
//                        }
//
//                       Log.d(ProgramTags.DB_ALL_FOUND, "All books in database successfully found");
//                    }
//                },
//                new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(ProgramTags.DB_ERROR, "Not all books could be found!" + e.toString());
//                    }
//                });
    }

    /**
     * A helper method checking the given book to ensure that it should be displayed in the
     * adapter's current view mode.
     *
     * @param book
     *      The current book to check
     * @return
     *      A boolean representing whether or not the current book should be displayed
     */
    public static boolean checkUser(Book book, String uuid, ViewMode viewMode, List<String> filter) {
        if (uuid == null) {
            return true;
        }

        if ((Book.StatusEnum.valueOf(book.getStatus()) == Book.StatusEnum.Unavailable) &&
            !uuid.equals(book.getOwner().get(0)))
        {
            return false;
        }

        switch (viewMode) {
            case OWNED:
                return uuid.equals(book.getOwner().get(0));
            case OWNED_FILTERED:
                return uuid.equals(book.getOwner().get(0)) && filter.contains(book.getStatus());
            case BORROWED:
                if (book.getBorrower().size() == 2) {
                    return uuid.equals(book.getBorrower().get(0));
                } else {
                    return false;
                }
            case REQUESTED:
                for (String requester : book.getRequests()) {
                    if (uuid.equals(requester)) {
                        return true;
                    }
                }
                return false;
            default:
                return (book.getStatus().equals(FireStoreMapping.BOOK_STATUS_AVAILABLE)
                        || book.getStatus().equals(FireStoreMapping.BOOK_STATUS_REQUESTED)
                || book.getOwner().get(0).equals(uuid));
        }
    }

    /**
     * A required method from {@link ArrayAdapter} for displaying an element of the
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
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.book_row, parent, false);
        }

        Book book = filteredBooks.get(position);
        setFields(convertView, book);

        return convertView;
    }

    private void setFields(View convertedView, Book book) {
        TextView title = convertedView.findViewById(R.id.title_text);
        TextView author = convertedView.findViewById(R.id.author_text);
        TextView isbn = convertedView.findViewById(R.id.isbn_text);
        TextView status = convertedView.findViewById(R.id.status_text);
        TextView owner = convertedView.findViewById(R.id.owner_text);
        TextView borrower = convertedView.findViewById(R.id.borrower_text);

        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        isbn.setText(book.getIsbn());
        setStatus(status, book);
        owner.setText(book.getOwner().get(1));
        if (book.getBorrower().size() == 2) {
            borrower.setText(book.getBorrower().get(1));
        }
    }

    private void setStatus(TextView statusView, Book book) {
        int colour;
        String bookStatus = book.getStatus();

        switch (Book.StatusEnum.valueOf(bookStatus)) {
            case Available:
                colour = ContextCompat.getColor(context, R.color.confirm);
                break;
            case Requested:
                colour = ContextCompat.getColor(context, R.color.mid_way);
                break;
            case Accepted:
            case Borrowed:
                colour = ContextCompat.getColor(context, R.color.deny);
                break;
            default:
                colour = 0x000000;
        }

        statusView.setText(bookStatus);
        statusView.setTextColor(colour);
    }

    /**
     * A required method from {@link android.widget.BaseAdapter} for getting a unique identifying
     * long value from an element of the internal list at a given position. In this case, the ISBN
     * of a book can be used, as a 64-bit long can represent up to 19 digits, while an ISBN has at
     * most 13 digits.
     * <p>
     * Deprecated due to due to idiosyncrasies with the way async databases work.
     *
     * @param position
     *      The position in the internal filtered list from which to get a unique feature of the
     *      element
     * @return
     *      The book's unique identifying ISBN at this position
     *
     * @deprecated
     */
    @Override
    public long getItemId(int position) {
        return Long.parseLong(filteredBooks.get(position).getIsbn());
    }

    /**
     * Sort the internal list based on a given {@link CompareBookBy.SortOption}.
     *
     * @param option
     *      The book's value to be prioritized while sorting
     * @see CompareBookBy
     */
    public void sort(CompareBookBy.SortOption option) {
        Collections.sort(filteredBooks, new CompareBookBy(option));
        notifyDataSetChanged();
    }

    /**
     * A class used to compare two books by their title, author, or ISBN.
     *
     * @author Warren Stix
     * @version 0.3
     * @see Comparator
     */
    public static class CompareBookBy implements Comparator<Book> {

        public enum SortOption {
            TITLE,
            AUTHOR,
            ISBN;

            public int toInt() {
                switch (this) {
                    default:
                    case TITLE:
                        return 0;
                    case AUTHOR:
                        return 1;
                    case ISBN:
                        return 2;
                }
            }
        }

        private final SortOption option;

        /**
         * Construct a comparator that prioritizes books with a given {@link SortOption}.
         *
         * @param option
         *      The relevant {@link SortOption}
         *
         */
        CompareBookBy(@Nullable SortOption option) {
            this.option = option;
        }

        /**
         * A required method from the {@link Comparator} interface used to compare two elements of
         * the same class. In this case, {@link Book}s are compared lexicographically based on their
         * given {@link SortOption}.
         *
         * @param book1
         *      The first {@link Book} to compare
         * @param book2
         *      The second {@link Book} to compare
         * @return
         *      The comparative value of book1 to book2
         */
        @Override
        public int compare(@NonNull Book book1, @NonNull Book book2) {
            String book1Val;
            String book2Val;

            switch (option) {
                case ISBN:
                   book1Val = book1.getIsbn();
                   book2Val = book2.getIsbn();
                   break;

                case TITLE:
                    book1Val = book1.getTitle();
                    book2Val = book2.getTitle();
                    break;

                case AUTHOR:
                    book1Val = book1.getAuthor();
                    book2Val = book2.getAuthor();
                    break;

                default:
                    return 0;
            }

            return book1Val.compareTo(book2Val);
        }
    }

    /**
     * Change the internal filtered list to only show books with a given status.
     * @deprecated
     * @param statusEnum
     *      The status to filter by
     */
    public void filter(@Nullable final Book.StatusEnum statusEnum) {
//        db.getAllBooks(new OnSuccessListener<List<Book>>() {
//                    @Override
//                    public void onSuccess(List<Book> books) {
//                        for (Book book : books) {
//                            if (checkUser(book) &&
//                                (statusEnum == null ||
//                                 Book.StatusEnum.valueOf(book.getStatus()) == statusEnum))
//                            {
//                                filteredBooks.add(book);
//                            }
//                        }
//                       Log.d(ProgramTags.DB_ALL_FOUND, "All books in database successfully found");
//                    }
//                },
//                new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(ProgramTags.DB_ERROR, "Not all books could be found!" + e.toString());
//                    }
//                });
        filteredBooks.clear();


        notifyDataSetChanged();
    }

    /**
     * Delete a book from the filtered list of books.
     *
     * @param position
     *      the position in the filtered list at which a book must be deleted
     */
    public void delete(int position) {
        String isbn = filteredBooks.get(position).getIsbn();
        filteredBooks.remove(position);
    }
}
