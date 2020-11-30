package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.Activity;
import android.media.Image;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MainActivityTest {
    private Solo solo;

    /**
     * Test class for MainActivity. Robotium test framework is used.
     */
    @Rule
    public ActivityTestRule<MainActivity> rule =
            new ActivityTestRule<MainActivity>(MainActivity.class, true, true);

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
     * Check that tabs are the same activity.
     */
    @Test
    public void clickMenuItems() {
        solo.clickOnMenuItem("MY BOOKS");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("BORROWED");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("ALL BOOKS");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("REQUESTED");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
    }

    @Test
    public void clickBookList() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.waitForActivity(MainActivity.class);
        solo.clickInList(1);
        solo.assertCurrentActivity("Wrong Activity", ABookActivity.class);
        Button request = (Button) solo.getView(R.id.abook_request_button);
        solo.clickOnView(request);
        Button owner = (Button) solo.getView(R.id.abook_owner_button);
        solo.clickOnView(owner);
        solo.assertCurrentActivity("Wrong Activity", AProfileActivity.class);
    }

    /**
     * Checks that addBook button goes to my book activity.
     */
    @Test
    public void addBook() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("MY BOOKS");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        FloatingActionButton add = (FloatingActionButton) solo.getView(R.id.floating_add);
        solo.clickOnView(add);
        solo.assertCurrentActivity("Wrong Activity", MyBookActivity.class);
    }

    /**
     * Check sort button.
     */
    @Test
    public void clickSortCancel() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        ImageButton sort = (ImageButton) solo.getView(R.id.sort);
        solo.clickOnView(sort);
    }

    /**
     * Check filter button.
     */
    @Test
    public void clickFilterCancel() {
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        solo.clickOnMenuItem("MY BOOKS");
        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);
        FloatingActionButton filter = (FloatingActionButton) solo.getView(R.id.floating_filter);
        solo.clickOnView(filter);
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
