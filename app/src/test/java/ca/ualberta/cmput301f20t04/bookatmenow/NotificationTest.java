package ca.ualberta.cmput301f20t04.bookatmenow;

import android.util.Log;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NotificationTest {
    @Test
    public void testNotificationBasic() {
        ArrayList<String> sender = new ArrayList<String>();
        sender.add("18f298ca-h345-9i3r-1234-e9855cb320c8");
        sender.add("Michelle");
        ArrayList<String> book = new ArrayList<String>();
        book.add("9780439554930");
        book.add("J.K. Rowling");
        Notification n = new Notification("18f298ca-h345-9i3r-1234-e9855cb320c8", sender, ProgramTags.NOTIFICATION_RETURN, book, "2020/11/25 16:23");
        assertEquals("18f298ca-h345-9i3r-1234-e9855cb320c8", n.getReceiveUUID());
        assertEquals(sender, n.getSender());
        assertEquals(ProgramTags.NOTIFICATION_RETURN, n.getType());
        assertEquals(book, n.getBook());
        assertEquals("2020/11/25 16:23", n.getTimestamp());
    }

    @Test
    public void testNotificationTimestampCreation() {
        Notification n = new Notification();
        assertTrue(checkTimestampFormat(n.getTimestamp()));

    }

    public boolean checkTimestampFormat(String timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        formatter.setLenient(false);

        try {
            Date notificationDate = formatter.parse(timestamp);
            return true;
        } catch (ParseException e) {
            return false;
        }

    }
}
