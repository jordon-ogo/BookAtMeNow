package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.Activity;
import android.widget.Button;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ScanBookActivityTest {
    private Solo solo;

    /**
     * Test class for the ScanBook activity. All the UI tests are written here. Robotium test
     * framework is used.
     */
    @Rule
    public ActivityTestRule<ScanBook> rule =
            new ActivityTestRule<ScanBook>(ScanBook.class, true, true);

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
     * Checks that scanner launches
     */
    @Test
    public void launchScannerTest() {
        solo.assertCurrentActivity("Scan Book", ScanBook.class);
        Button useIsbn = (Button) solo.getView(R.id.scanbook_done_button);
        // No ISBN scanned, so will stay on activity.
        solo.clickOnView(useIsbn);
        solo.assertCurrentActivity("Scan Book", ScanBook.class);
    }

}
