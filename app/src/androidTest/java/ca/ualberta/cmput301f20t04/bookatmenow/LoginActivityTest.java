package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.Activity;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LoginActivityTest {
    private Solo solo;

    /**
     * Test class for LoginActivity. Robotium test framework is used.
     */
    @Rule
    public ActivityTestRule<LoginActivity> rule =
            new ActivityTestRule<LoginActivity>(LoginActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), rule.getActivity());
    }

    /**
     * Gets the Activity
     * @throws Exception
     */
    public void start() throws Exception {
        Activity activity = rule.getActivity();
    }

    /**
     * Checks successful login and logout.
     */
    @Test
    public void successfulLoginLogoutTest() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.enterText((EditText) solo.getView(R.id.login_user), "Steve");
        solo.enterText((EditText) solo.getView(R.id.login_pw), "steve01");
        solo.clickOnButton("Login");
        solo.waitForActivity("MainActivity");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        FloatingActionButton profile = (FloatingActionButton) solo.getView(R.id.floating_edit_profile);
        solo.clickOnView(profile);
        solo.waitForActivity("ProfileActivity");
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.clickOnButton("Logout");
        solo.waitForActivity("LoginActivity"); // check if everything else finished
    }

    /**
     * Checks wrong username entry but correct password.
     */
    @Test
    public void wrongUsernameTest() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.enterText((EditText) solo.getView(R.id.login_user), "NotUser");
        solo.enterText((EditText) solo.getView(R.id.login_pw), "password");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
    }

    /**
     * Checks wrong password entry but correct username.
     */
    @Test
    public void wrongPasswordTest() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.enterText((EditText) solo.getView(R.id.login_user), "CustomUsername");
        solo.enterText((EditText) solo.getView(R.id.login_pw), "wrong");
        solo.clickOnButton("Login");
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
    }

    @Test
    public void clickCreateAccount() {
        solo.assertCurrentActivity("Wrong activity", LoginActivity.class);
        solo.clickOnButton("Create Account");
        solo.waitForActivity("ProfileActivity");
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
    }

    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
