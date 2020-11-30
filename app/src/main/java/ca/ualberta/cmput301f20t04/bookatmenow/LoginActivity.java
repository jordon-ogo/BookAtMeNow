package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * Login using username/email and password that exist in the database.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText logInUser;
    private EditText logInPW;
    private Button loginBtn;
    private Button createAccBtn;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        logInUser = findViewById(R.id.login_user);
        logInPW = findViewById(R.id.login_pw);

        final DBHandler db = new DBHandler();

        // Dialog for username/password error
        final AlertDialog.Builder invalidLoginDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Invalid Username or Password")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loginBtn.setEnabled(true);
                        createAccBtn.setEnabled(true);
                    }
                });

        // Dialog for database error
        final AlertDialog.Builder databaseErrorDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Invalid Username or Password")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loginBtn.setEnabled(true);
                        createAccBtn.setEnabled(true);
                    }
                });

        // This needs to be reworked into one authentication method
        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String usernameOrEmail = logInUser.getText().toString();
                final String password = logInPW.getText().toString();

                //Check that values have been entered for both username/email and password.
                if(checkEntered(usernameOrEmail, password)) {
                    // if username exists
                    loginBtn.setEnabled(false);
                    createAccBtn.setEnabled(false);
                    db.loginHandler(usernameOrEmail, password,
                            new OnSuccessListener<List<String>>() {
                                @Override
                                public void onSuccess(List<String> s) {
                                    if (s != null) {
                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                        i.putExtra(FireStoreMapping.USER_FIELDS_ID, s.get(0));
                                        i.putExtra(FireStoreMapping.USER_FIELDS_USERNAME, s.get(1));
                                        startActivity(i);
                                        finish();
                                    } else {
                                        logInPW.setText("");
                                        invalidLoginDialog.show();
                                    }
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    databaseErrorDialog.show();
                                }
                            });
                }

            }
        }); // end of setOnClickListener

        createAccBtn = findViewById(R.id.create_acc_btn);
        createAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
            }
        });
    }

    /**
     * Checks that the user has entered a username and password.  If the username or password is
     * missing, adds an error to the relevant EditText.
     * @param usernameOrEmail   users entered username or email
     * @param password  users entered password
     * @return whether user has entered values for both credentials.
     */
    private boolean checkEntered(String usernameOrEmail, String password) {
        boolean credsCheck = true;
        if(usernameOrEmail.trim().length() == 0) {
            logInUser.setError("Please enter a username or email.");
            credsCheck = false;
        }
        if(password.trim().length() == 0) {
            logInPW.setError("Please enter a password.");
            credsCheck = false;
        }
        return credsCheck;
    }
}