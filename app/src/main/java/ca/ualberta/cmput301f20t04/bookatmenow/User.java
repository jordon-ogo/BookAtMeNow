package ca.ualberta.cmput301f20t04.bookatmenow;

import java.util.regex.Pattern;

/**
 * Represents a user in the app.
 * Has a unique username
 * Has password and email.
 * Can have a phone # and/or address.
 * @author Jeanne Coleongco
 * @version 0.3
 */
public class User {
    /**
     * Unique user ID, randomly generated on account creation.
     * Having it enables changing one's account details without losing information on books owned.
     */
    private String userId;

    /**
     * Unique username, checked on account creation.
     */
    private String username;

    /**
     * Password must be letters, numbers, no spaces.
     */
    private String password;
    private String email;

    /**
     * Optional fields.
     */
    private String phone;
    private String address;

    /**
     * Constructs basic user.
     * @param username
     * @param password
     * @param email
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * Constructs user with optional phone # or email.
     * Differentiates between phone # or email and assigns to the appropriate variable.
     * @param username
     * @param password
     * @param phoneOrAddress
     * @param email
     */
    public User(String username, String password, String phoneOrAddress, String email) {
        this.username = username;
        this.password = password;

        final Pattern p = Pattern.compile("[0-9]+");
        if (p.matcher(phoneOrAddress).matches()) {
            this.phone = phoneOrAddress;
        } else {
            this.address = phoneOrAddress;
        }

        this.email = email;
    }

    /**
     * constructs user with everything.
     * @param username
     * @param password
     * @param phone
     * @param email
     * @param address
     */
    public User(String username, String password, String phone, String email, String address) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    /**
     * Constructor for DB handler.
     */
    public User() {}

    /**
     * Gets user's unique id.
     * @return unique user id
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Only used when the user is first created.
     * Should not be updated otherwise.
     * @param id
     */
    public void setUserID(String id) {
        this.userId = id;
    }

    /**
     * Gets the unique username.
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username on account creation.
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user's password.
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password on account creation.
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets user's phone #.
     * @return phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone # on account creation.
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets user's email.
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets user's email on account creation.
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets user's address.
     * @return address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets user's address on account creation.
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }
}
