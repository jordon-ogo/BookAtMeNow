package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Create new user with unique username, with or without optional fields.
 * See user details if currently logged in.
 * Edit user details if currently logged in.
 */
public class ProfileActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private EditText emailEditText;

    // optional ones:
    private EditText phoneEditText;
    private EditText addressEditText;

    // buttons
    private Button saveProfileButton;
    private Button logoutButton;
    private Button cancelButton;
    private Button addressButton;

    private String uuid;

    // for Address
    Geocoder geocoder;
    List<Address> addresses;

    /**
     * Check email address format.
     * @param email
     * @return true if email is valid, false if it is not
     */
    public static boolean validEmail(String email) {
        if (email == null)
            return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Password is alphanumeric.
     * TODO: allow special characters.
     */
    static final Pattern PW_REGEX  = Pattern.compile(
            "^[\\w+&*-]+"
    );

    public static boolean validPassword(String password) {
        if (password == null)
            return false;
        return PW_REGEX.matcher(password).matches();
    }

    /**
     * Location for address.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){//confirm button was pressed
            String lat = data.getStringExtra("lat");
            String lng = data.getStringExtra("lng");
            try {
                addresses = geocoder.getFromLocation(Double.valueOf(lat), Double.valueOf(lng), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            addressEditText.setText(addresses.get(0).getAddressLine(0));
            Log.i("AppInfo", "address is: " + String.valueOf(addresses.get(0).getAddressLine(0)));

        }
    }

    /**
     * Adding/updating a user profile.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_edit_profile);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText passwordConfirmEditText = findViewById(R.id.password_confirm);
        final EditText emailEditText = findViewById(R.id.email);
        // optional ones:
        final EditText phoneEditText = findViewById(R.id.phone);
        addressEditText = findViewById(R.id.address);

        // buttons
        final Button saveProfileButton = findViewById(R.id.profile_save);
        final Button logoutButton = findViewById(R.id.logout);
        final Button cancelButton = findViewById(R.id.profile_cancel);
        Button addressButton = findViewById(R.id.address_button);

        final DBHandler db = new DBHandler();

        final User[] currenUser = new User[1];

        geocoder = new Geocoder(this, Locale.getDefault());

        uuid = getIntent().getStringExtra("uuid");
        logoutButton.setVisibility(View.VISIBLE);

        // if user exists, allow edit profile
        if (uuid != null) {
            db.usernameExists(uuid, new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    db.getUser(uuid, new OnSuccessListener<User>() {
                        @Override
                        public void onSuccess(User user) {
                            currenUser[0] = user;
                            usernameEditText.setText(user.getUsername());
                            emailEditText.setText(user.getEmail());
                            phoneEditText.setText(user.getPhone());
                            addressEditText.setText(user.getAddress());
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            recreate();
                        }
                    });
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {
            logoutButton.setVisibility(View.INVISIBLE);
        }

        /**
         * Go to Geolocation activity to set address.
         */
        addressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, GeoLocation.class);
                intent.putExtra(ProgramTags.LOCATION_PURPOSE, "getLocation");
                intent.putExtra(ProgramTags.LOCATION_MESSAGE, "UserLocSelect");
                startActivityForResult(intent, 1);
            }
        });

        /**
         * Back to previous activity.
         */
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /**
         * Back to login activity.
         */
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        // Dialog for mismatch password error
        final AlertDialog.Builder mismatchPasswordDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Passwords don't match!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelButton.setEnabled(true);
                        logoutButton.setEnabled(true);
                    }
                });
        // Dialog for invalid password error
        final AlertDialog.Builder invalidPasswordDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Invalid password!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelButton.setEnabled(true);
                        logoutButton.setEnabled(true);
                    }
                });

        // Dialog for invalid email error
        final AlertDialog.Builder invalidEmailDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Invalid email!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelButton.setEnabled(true);
                        logoutButton.setEnabled(true);
                    }
                });

        // Dialog for email in use error
        final AlertDialog.Builder busyEmailDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Email already in use!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelButton.setEnabled(true);
                        logoutButton.setEnabled(true);
                    }
                });

        // Dialog for username taken error
        final AlertDialog.Builder busyUsernameDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Username is unavailable!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelButton.setEnabled(true);
                        logoutButton.setEnabled(true);
                    }
                });

        // Dialog for database error
        final AlertDialog.Builder databaseErrorDialog = new AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("Invalid input!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelButton.setEnabled(true);
                        logoutButton.setEnabled(true);
                    }
                });

        /**
         * Add/Update user profile in db.
         */
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                String passwordConfirm = passwordConfirmEditText.getText().toString();
                final String email = emailEditText.getText().toString();
                // optional
                final String phone = phoneEditText.getText().toString();
                final String address = addressEditText.getText().toString();

                if (uuid == null || password.length() > 0 || passwordConfirm.length() > 0){
                    if (!password.equals(passwordConfirm)) {
                        mismatchPasswordDialog.show();
                    } else if (!validPassword(password)) {
                        invalidPasswordDialog.show();
                    }
                }

                if (!validEmail(email)) {
                    invalidEmailDialog.show();
                }

                if ((username.length() > 0
                        && password.length() > 0
                        && password.equals(passwordConfirm)
                        && validEmail(email) && validPassword(password)) || (currenUser[0] != null && validEmail(email) && username.length() > 0)) {

                    db.usernameExists(username,
                            new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(final String existingUsernameUUID) {
                                    db.emailExists(email,
                                            new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String existingEmailUUID) {
                                                    if (existingEmailUUID == null && existingUsernameUUID == null) {
                                                        User userToSave = new User(username, password, email);
                                                        userToSave.setPhone(phone);
                                                        userToSave.setAddress(address);
                                                        if (uuid == null) {
                                                            db.addUser(userToSave,
                                                                    new OnSuccessListener<Boolean>() {
                                                                        @Override
                                                                        public void onSuccess(Boolean aBoolean) {
                                                                            finish();
                                                                        }
                                                                    },
                                                                    new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            e.printStackTrace();
                                                                            databaseErrorDialog.show();
                                                                        }
                                                                    });
                                                        } else {
                                                            Log.e("Nikita test", "got here");
                                                            userToSave.setUserID(uuid);
                                                            db.updateUser(userToSave,
                                                                    new OnSuccessListener<Boolean>() {
                                                                        @Override
                                                                        public void onSuccess(Boolean aBoolean) {
                                                                            finish();
                                                                        }
                                                                    },
                                                                    new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            e.printStackTrace();
                                                                            databaseErrorDialog.show();
                                                                        }
                                                                    });
                                                        }
                                                    } else if ((existingEmailUUID != null || existingUsernameUUID != null) && uuid != null) {
                                                        if (existingEmailUUID != null && !currenUser[0].getEmail().equals(email)) {
                                                            busyEmailDialog.show();
                                                        } else if (existingUsernameUUID != null && !currenUser[0].getUsername().equals(username)) {
                                                            busyUsernameDialog.show();
                                                        } else {
                                                            User userToSave = new User(username, password, email);
                                                            userToSave.setPhone(phone);
                                                            userToSave.setAddress(address);
                                                            userToSave.setUserID(uuid);
                                                            db.updateUser(userToSave,
                                                                    new OnSuccessListener<Boolean>() {
                                                                        @Override
                                                                        public void onSuccess(Boolean aBoolean) {
                                                                            finish();
                                                                        }
                                                                    },
                                                                    new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            e.printStackTrace();
                                                                            databaseErrorDialog.show();
                                                                        }
                                                                    });
                                                        }
                                                    } else {
                                                        if (existingEmailUUID != null) {
                                                            busyEmailDialog.show();
                                                        } else {
                                                            busyUsernameDialog.show();
                                                        }
                                                    }
                                                }
                                            },
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    e.printStackTrace();
                                                    databaseErrorDialog.show();
                                                }
                                            });
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    databaseErrorDialog.show();
                                }
                            });
                }
            }
        });
    }
}
