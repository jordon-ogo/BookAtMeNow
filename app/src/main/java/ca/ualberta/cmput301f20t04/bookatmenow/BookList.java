package ca.ualberta.cmput301f20t04.bookatmenow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * An abstract definition of an adapter for a {@link android.widget.ListView} that displays a
 * filtered {@link List} of books. The specific form this list takes is up to the derived class to
 * implement.
 * <p>
 * This class has been completely deprecated because this "all-in-one" style of Adapter does not
 * play well with async databases like FireStore - the list would never be constructed properly.
 *
 * @author Warren Stix
 * @see BaseAdapter
 * @see RequestList
 * @version 0.6
 *
 * @deprecated
 */
public abstract class BookList extends BaseAdapter {
    private Context context;

    protected DBHandler db;
    protected List<Book> filteredBooks;

    /**
     * Construct a filtered list of books from a FireStore database.
     *
     * @param context
     *      The context of the containing {@link android.app.Activity}
     * @param filteredBooks
     *      The list containing the corresponding books from the database
     */
    public BookList(Context context, List<Book> filteredBooks) {
        this.context = context;
        this.filteredBooks = filteredBooks;

        db = new DBHandler();
    }

    /**
     * A required method from {@link BaseAdapter} for getting the length of the internal list.
     *
     * @return
     *      The length of the internal filtered list
     */
    @Override
    public int getCount() {
        return filteredBooks.size();
    }

    /**
     * A required method from {@link BaseAdapter} for getting an element of the internal list at a
     * given position.
     *
     * @param position
     *      The position in the internal filtered list from which to retrieve the element
     * @return
     *      The element in the internal filtered list at the given position
     */
    @Override
    public Object getItem(int position) {
        return filteredBooks.get(position);
    }

    /**
     * A helper method that helps a subclass to inflate a row of a {@link android.widget.ListView}.
     *
     * @param convertView
     *      The {@link BaseAdapter}'s given {@link View} at this position to convert into a row of
     *      the internal list
     * @param parent
     *      The {@link ViewGroup} containing the elements of the {@link android.widget.ListView}
     * @param xml
     *      A unique identifying integer referring to the ID of the XML layout resource file that
     *      contains the {@link View} each row will be built on
     * @return
     *      The original given {@link View}, converted into a row of the internal list
     */
    protected View inflate_helper(View convertView, ViewGroup parent, int xml) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(xml, parent, false);
        }
        return convertView;
    }
}
