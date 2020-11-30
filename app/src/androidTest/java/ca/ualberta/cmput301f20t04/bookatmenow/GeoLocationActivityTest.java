package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.Activity;
import android.widget.Button;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class GeoLocationActivityTest {
    private Solo solo;

    /**
     * Test class for the ScanBook activity. All the UI tests are written here. Robotium test
     * framework is used.
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

    @Test
    public void clickGeoLocationAndReturn() {
        //Go to the GeoLocation Activity from the ProfileActivity
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.clickOnButton("Select Address from Map");
        solo.waitForActivity(GeoLocation.class);
        solo.assertCurrentActivity("Wrong Activity", GeoLocation.class);

        //Press the cancel button to return to ProfileActivity.
        Button cancelButton = (Button) solo.getView(R.id.GeoLocation_button_cancel);
        solo.clickOnView(cancelButton);

        //Check that the ProfileActivity has been reached.
        solo.waitForActivity(ProfileActivity.class);
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);

    }

    @Test
    public void selectLocation() {
        //Go to the GeoLocation Activity from the ProfileActivity
        solo.assertCurrentActivity("Wrong Activity", ProfileActivity.class);
        solo.clickOnButton("Select Address from Map");
        solo.waitForActivity(GeoLocation.class);
        solo.assertCurrentActivity("Wrong Activity", GeoLocation.class);

        //Press the Set Address button without selecting an address and check that activity
        // doesn't change.
        Button setAddressButton = (Button) solo.getView(R.id.GeoLocation_button_setPickupLoc);
        solo.clickOnView(setAddressButton);
        solo.assertCurrentActivity("Wrong Activity", GeoLocation.class);
    }
}
