package ca.ualberta.cmput301f20t04.bookatmenow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;

/**
 * A simple {@link ArrayAdapter} to be used in concert with {@link DBHandler#bookRequests}.
 *
 * @author Warren Stix
 * @see ArrayAdapter
 * @version 0.2
 */
public class RequestAdapter extends ArrayAdapter<User> {
    private LinkedList<User> requests;
    private Context context;

    /**
     * Construct a viewable list of requests from a given {@link LinkedList} of {@link User}s.
     * <p>
     * The {@link LinkedList} was chosen to represent a list of requests because it can easily be
     * deleted from at any point and will never need to be sorted.
     *
     * @param context
     *      The context of the calling activity, used to display objects on the screen
     * @param requests
     *      The list of {@link User}s to display
     */
    RequestAdapter(Context context, LinkedList<User> requests) {
        super(context, 0, requests);

        this.context = context;
        this.requests = requests;
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
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.request_row, parent,false);
        }

        User requester = requests.get(position);

        TextView displayName = convertView.findViewById(R.id.display_name_text);

        if (requester.getUsername() != null) {
            displayName.setText(requester.getUsername());
        } else {
            displayName.setText(requester.getEmail());
        }

        // Add a click listener to the confirm buttons that will give the position of the click
        // to the clickedAccept function in the BookRequests activity.
        convertView.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof BookRequests) {
                    ((BookRequests) context).clickedAccept(position);
                }
            }
        });

        // Add a click listener to the reject buttons that will give the position of the click
        // to the removeRequest function in the BookRequests activity.
        convertView.findViewById(R.id.reject_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof BookRequests) {
                    ((BookRequests) context).removeRequest(position);
                }
            }
        });

        return convertView;
    }
}
