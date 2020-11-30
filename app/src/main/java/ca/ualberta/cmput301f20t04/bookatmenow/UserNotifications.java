package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity to view notifications. Has partially been adapted from
 * https://www.freecodecamp.org/news/how-to-implement-swipe-for-options-in-recyclerview/
 */
public class UserNotifications extends AppCompatActivity implements NotificationAdapter.OnNotificationListener {
    RelativeLayout main;
    private Context context;

    Intent intent;

    String uuid;
    String username;
    final private static int GOTO_REQUESTS = 0;
    final private static int GOTO_ABOOK = 1;
    final private static int GOTO_MYBOOK = 2;

    List<Notification> notifications;
    NotificationAdapter notificationAdapter;
    DBHandler db;
    RecyclerView rView;
    TextView noNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notifications);
        rView = (RecyclerView) this.findViewById(R.id.notifications_recyclerview);
        noNotifications = this.findViewById(R.id.noNotifications_UserNotifications);
        noNotifications.setVisibility(View.INVISIBLE);

        db = new DBHandler();
        intent = getIntent();
        context = this;
        notifications = new ArrayList<>();

        uuid = intent.getStringExtra(ProgramTags.PASSED_UUID);
        username = intent.getStringExtra(ProgramTags.PASSED_USERNAME);

        rView.setHasFixedSize(true);
        rView.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(UserNotifications.this, notifications, this);
        rView.setAdapter(notificationAdapter);

        //Get users notifications from the db.
        retrieveNotifications();

        // If there are no notifications, show the no notifications message.

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Drawable trashIcon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_delete_24);
            private final ColorDrawable bg = new ColorDrawable(Color.RED);
            
            @Override
            public boolean onMove(RecyclerView rView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * On notification swipe, remove the notification and delete it from the database.
             * @param viewHolder
             * @param direction
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final Notification n = notificationAdapter.getNotification(viewHolder.getAdapterPosition());
                int position = viewHolder.getAdapterPosition();
                notificationAdapter.removeNotification(position);

                // If there are no notifications left, show the no notifications message.
                if(notificationAdapter.getItemCount() == 0) {
                    noNotifications.setVisibility(View.VISIBLE);
                }

                Toast.makeText(context, "Removed notification for " + n.getBook().get(1), Toast.LENGTH_LONG).show();
                
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

            @Override
            public void onChildDraw(Canvas c, RecyclerView rView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, rView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View v = viewHolder.itemView;

                int iconMargin = (v.getHeight() - trashIcon.getIntrinsicHeight()) / 2;
                int iconTop = v.getTop() + (v.getHeight() - trashIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + trashIcon.getIntrinsicHeight();

                if (dX > 0) {
                    int iconLeft = v.getLeft() + iconMargin + trashIcon.getIntrinsicWidth();
                    int iconRight = v.getLeft() + iconMargin;
                    trashIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    bg.setBounds(v.getLeft(), v.getTop(), v.getLeft() + ((int) dX), v.getBottom());
                } else if (dX < 0) {
                    int iconLeft = v.getRight() - iconMargin - trashIcon.getIntrinsicWidth();
                    int iconRight = v.getRight() - iconMargin;
                    trashIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    bg.setBounds(v.getRight() + ((int) dX), v.getTop(), v.getRight(), v.getBottom());
                } else {
                    bg.setBounds(0, 0, 0, 0);
                }

                bg.draw(c);
                trashIcon.draw(c);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(rView);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    private void retrieveNotifications() {
        noNotifications.setVisibility(View.INVISIBLE);
        db.getNotifications(uuid, new OnSuccessListener<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notificationList) {
                notifications.clear();
                notifications.addAll(notificationList);
                notificationAdapter.notifyDataSetChanged();
                notificationAdapter.sortNotifications();
                if(notificationAdapter.getItemCount() == 0) {
                    noNotifications.setVisibility(View.VISIBLE);
                }

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(ProgramTags.DB_ERROR, String.format("Could not retrieve notifications for %s", uuid));
            }
        });
    }


    /**
     * On a notification being clicked, check the notification type and launch the relevant activity.
     * @param n notification object relevant to the position in the notification list that was clicked.
     */
    @Override
    public void onNotificationClick(Notification n) {
        Intent i;
        Log.e("Clicked on ", n.getTimestamp());
        Log.e("Type is ", n.getType());
        switch(n.getType()) {
            case ProgramTags.NOTIFICATION_REQUEST:
                i = new Intent(UserNotifications.this, BookRequests.class);
                i.putExtra(ProgramTags.PASSED_ISBN, n.getBook().get(0));
                startActivityForResult(i, GOTO_REQUESTS);
                break;

            case ProgramTags.NOTIFICATION_REJECT:

            case ProgramTags.NOTIFICATION_APPROVE:
                i = new Intent(UserNotifications.this, ABookActivity.class);
                i.putExtra(ProgramTags.PASSED_ISBN, n.getBook().get(0));
                i.putExtra(ProgramTags.PASSED_UUID, uuid);
                i.putExtra(ProgramTags.PASSED_USERNAME, username);
                startActivityForResult(i, GOTO_ABOOK);
                break;

            case ProgramTags.NOTIFICATION_RETURN:
                i = new Intent(UserNotifications.this, MyBookActivity.class);
                i.putExtra(ProgramTags.PASSED_ISBN, n.getBook().get(0));
                startActivityForResult(i, GOTO_MYBOOK);
                break;
        }
    }

    /**
     * When returning from another activity to the UserNotifications activity, reload the notifications
     * in case there have been changes.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            retrieveNotifications();
    }
}

