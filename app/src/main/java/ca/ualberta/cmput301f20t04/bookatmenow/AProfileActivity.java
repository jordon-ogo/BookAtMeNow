package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * View A Profile that's not the logged in user's.
 */
public class AProfileActivity extends AppCompatActivity {

    private TextView aUsername;
    private TextView anEmail;
    private TextView aPhone;
    private TextView anAddress;

    DBHandler db;

    String uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_profile);

        db = new DBHandler();

        aUsername = findViewById(R.id.a_username);
        anEmail = findViewById(R.id.an_email);
        aPhone = findViewById(R.id.a_phone);
        anAddress = findViewById(R.id.an_address);
        aUsername.setTypeface(null, Typeface.BOLD);

        uuid = getIntent().getStringExtra(ProgramTags.PASSED_UUID);

        db.getUser(uuid, new OnSuccessListener<User>() {
            @Override
            public void onSuccess(User user) {
                aUsername.setText(user.getUsername());

                String email = "Email: ";
                SpannableString emailString = new SpannableString(email + user.getEmail());
                emailString.setSpan(new StyleSpan(Typeface.BOLD), 0, email.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                anEmail.setText(emailString);

                String phone = "Phone: ";
                String phoneNumber = user.getPhone();
                SpannableString phoneString = new SpannableString(phone + phoneNumber);
                phoneString.setSpan(new StyleSpan(Typeface.BOLD), 0, phone.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                aPhone.setText(phoneString);
                if(phoneNumber.equals("")) aPhone.setVisibility(View.INVISIBLE);

                String address = "Address: ";
                String addressLocation = user.getAddress();
                SpannableString addressString = new SpannableString(address + addressLocation);
                addressString.setSpan(new StyleSpan(Typeface.BOLD), 0, address.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                anAddress.setText(addressString);
                if(addressLocation.equals("")) anAddress.setVisibility(View.INVISIBLE);

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
}