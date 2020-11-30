package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProfileActivityTest {
    private Solo solo;
    DBHandler db = new DBHandler();

    /**
     * Test class for ProfileActivity. Robotium test framework is used.
     */
    @Rule
    public ActivityTestRule<ProfileActivity> rule =
            new ActivityTestRule<ProfileActivity>(ProfileActivity.class, true, true);

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
     * Checks that Cancel button brings user back to Home screen.
     */
    @Test
    public void clickCancel() {
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.clickOnButton("Cancel");
    }

    /**
     * Checks that select address goes into Geolocation.
     */
    @Test
    public void clickAddress() {
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.clickOnButton("Select Address from Map");
        solo.waitForActivity("GeoLocation");
        solo.assertCurrentActivity("Wrong Activity", GeoLocation.class);
    }

    /**
     * Checks filling all user fields.
     */
    @Test
    public void fullUser() {
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.enterText((EditText) solo.getView(R.id.username), "Jane Doe");
        solo.enterText((EditText) solo.getView(R.id.password), "janedoe");
        solo.enterText((EditText) solo.getView(R.id.password_confirm), "janedoe");
        solo.enterText((EditText) solo.getView(R.id.phone), "7801234567");
        solo.enterText((EditText) solo.getView(R.id.email), "jane@gmail.com");
        solo.enterText((EditText) solo.getView(R.id.address), "91210 Beverly Hills");
        solo.clickOnButton("Cancel");
    }

    /**
     * Checks filling all but optional user fields.
     * Will fail if user already exists in db.
     */
    @Test
    public void partialUser() {
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.enterText((EditText) solo.getView(R.id.username), "Jane Doe");
        solo.enterText((EditText) solo.getView(R.id.password), "janedoe");
        solo.enterText((EditText) solo.getView(R.id.password_confirm), "janedoe");
        solo.enterText((EditText) solo.getView(R.id.email), "jane@gmail.com");
        solo.clickOnButton("Cancel");
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
