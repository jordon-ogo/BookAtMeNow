package ca.ualberta.cmput301f20t04.bookatmenow;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater layoutInflater;
    private List<Notification> notifications;
    private OnNotificationListener onNotificationListener;

    public NotificationAdapter(Context context, List<Notification> notifications, OnNotificationListener onNotificationListener) {
        layoutInflater = LayoutInflater.from(context);
        this.notifications = notifications;
        this.onNotificationListener = onNotificationListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = layoutInflater.inflate(R.layout.notification_row, parent, false);
        return new MyViewHolder(view, onNotificationListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Notification n = notifications.get(position);
        if(holder instanceof MyViewHolder){
            ((MyViewHolder)holder).timestamp.setText(n.getTimestamp());
            ((MyViewHolder)holder).message.setText(messageBuilder(n.getType(), n.getSender().get(1),
                    n.getBook().get(1)));
        }

    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void removeNotification(int position) {
        notifications.remove(position);
        notifyDataSetChanged();
    }

    /**
     * Sorts the notifications list by newest to oldest notification based on their timestamps.
     */
    public void sortNotifications() {
        Collections.sort(notifications, new Comparator<Notification>() {
            final DateFormat dFormat = new SimpleDateFormat("yyyy/mm/dd HH:mm");

            public int compare(Notification n1, Notification n2) {
                try {
                    return dFormat.parse(n2.getTimestamp()).compareTo(dFormat.parse(n1.getTimestamp()));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    public Notification getNotification(int adapterPosition) {
        return notifications.get(adapterPosition);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        OnNotificationListener onNotificationListener;
        TextView timestamp;
        TextView message;

        public MyViewHolder(View itemView, OnNotificationListener onNotificationListener) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.notification_row_timestamp);
            message = itemView.findViewById(R.id.notification_row_message);
            this.onNotificationListener = onNotificationListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            try {
                Notification n = notifications.get(getAdapterPosition());
                onNotificationListener.onNotificationClick(n);
            } catch (Exception e) {

            }

        }
    }

    public interface OnNotificationListener {
        void onNotificationClick(Notification n);
    }

    /**
     * Builds message relevant to the notification in question.
     * @param nType Type of the notification
     * @param sName Name of the user who caused the notification to be sent.
     * @param bTitle Title of the book the notification is about.
     * @return notification message.
     */
    private SpannableString messageBuilder(String nType, String sName, String bTitle) {
        String sentenceFiller, sentenceStart, sentenceEnd;
        SpannableString messageString;

        switch(nType) {
            case "Request":
                sentenceFiller = " has requested ";
                messageString = new SpannableString(sName + sentenceFiller + bTitle);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), 0, sName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.ITALIC), sName.length() +
                        sentenceFiller.length(), sName.length() + sentenceFiller.length() +
                        bTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), sName.length() +
                        sentenceFiller.length(), sName.length() + sentenceFiller.length() +
                        bTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case "Approve":
                sentenceStart = "Your request for ";
                sentenceEnd = " was accepted.";
                messageString = new SpannableString(sentenceStart + bTitle + sentenceEnd);
                messageString.setSpan(new StyleSpan(Typeface.ITALIC), sentenceStart.length() - 1,
                        sentenceStart.length() + bTitle.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), sentenceStart.length() - 1,
                        sentenceStart.length() + bTitle.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case "Reject":
                sentenceStart = "Your request for ";
                sentenceEnd = " was rejected.";
                messageString = new SpannableString(sentenceStart + bTitle + sentenceEnd);
                messageString.setSpan(new StyleSpan(Typeface.ITALIC), sentenceStart.length() - 1,
                        sentenceStart.length() + bTitle.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), sentenceStart.length() - 1,
                        sentenceStart.length() + bTitle.length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case "Return":
                sentenceFiller = " would like to return ";
                messageString = new SpannableString(sName + sentenceFiller + bTitle);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), 0, sName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.ITALIC), sName.length() +
                        sentenceFiller.length(), sName.length() + sentenceFiller.length() +
                        bTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageString.setSpan(new StyleSpan(Typeface.BOLD), sName.length() +
                        sentenceFiller.length(), sName.length() + sentenceFiller.length() +
                        bTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                break;

            default:
                messageString = new SpannableString("Error occurred while building this message.");
        }

        return messageString;
    }
}