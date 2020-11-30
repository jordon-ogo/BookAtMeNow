package ca.ualberta.cmput301f20t04.bookatmenow;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A notification in the app.
 * Has a type (request, accept aka approve, reject, return), sender and receiver, and a book it's about.
 */
public class Notification {
    public enum NotificationType {
        Request,
        Approve,
        Reject,
        Return
    }

    private String selfUUID;
    private String receiveUUID;
    private List<String> sender;
    private String type;
    private List<String> book;
    private String timestamp;

    /**
     * The notification message.
     * @param receiveUUID
     * @param sender
     * @param type
     * @param book
     * @param timestamp
     */
    public Notification(String receiveUUID, List<String> sender, String type, List<String> book, String timestamp) {
        this.receiveUUID = receiveUUID;
        this.sender = sender;
        for (NotificationType t : NotificationType.values()) {
            if (t.name().equals(type)) {
                this.type = type;
            }
        }
        if(this.type == null) {
            Log.e(ProgramTags.NOTIFICATION_ERROR, String.format("%s is not a valid notification type.", type));
        }
        this.book = book;
        this.timestamp = timestamp;
    }

    /**
     * Sets the timestamp of the notification.
     */
    public Notification() {
        Date currentTime = Calendar.getInstance().getTime();
        this.timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(currentTime);
    }

    /**
     * Gets UUID of recipient.
     * @return UUID of recipient
     */
    public String getReceiveUUID() {
        return receiveUUID;
    }

    /**
     * Sets UUID of recipient.
     * @param receiveUUID
     */
    public void setReceiveUUID(String receiveUUID) {
        this.receiveUUID = receiveUUID;
    }

    /**
     * Gets the sender of the notification.
     * @return sender (a username and unique user id as a list)
     */
    public List<String> getSender() {
        return sender;
    }

    /**
     * Sets the sender of the notification.
     * @param sender
     */
    public void setSender(List<String> sender) {
        this.sender = sender;
    }

    /**
     * Gets the type of notification.
     * @return one of request, accept aka approve, reject, return
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of notification.
     * @param type
     */
    public void setType(String type) {
        for (NotificationType t : NotificationType.values()) {
            if (t.name().equals(type)) {
                this.type = type;
            }
        }
        if(this.type == null) {
            Log.e(ProgramTags.NOTIFICATION_ERROR, String.format("%s is not a valid notification type.", type));
        }
    }

    /**
     * Gets the book the notification is about.
     * @return book
     */
    public List<String> getBook() {
        return book;
    }

    /**
     * Sets the book the notification is about.
     * @param book
     */
    public void setBook(List<String> book) {
        this.book = book;
    }

    /**
     * Gets the timestamp of the notification.
     * @return
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the currently logged in user's uuid.
     * @param uuid
     */
    public void setSelfUUID(String uuid) {
        this.selfUUID = uuid;
    }

    /**
     * Gets the currently logged in user's uuid.
     * @return
     */
    public String getSelfUUID() {
        return this.selfUUID;
    }
}
