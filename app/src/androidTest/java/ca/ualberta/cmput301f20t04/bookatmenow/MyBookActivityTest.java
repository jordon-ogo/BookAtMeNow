package ca.ualberta.cmput301f20t04.bookatmenow;

import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

public class MyBookActivityTest {
    private Solo solo;

    /**
     * Test class for MainActivity. Robotium test framework is used.
     */
    @Rule
    public ActivityTestRule<MyBookActivity> rule =
            new ActivityTestRule<MyBookActivity>(MyBookActivity.class, true, true);

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
    public void clickScan() {
        solo.assertCurrentActivity("MyBookActivity", MyBookActivity.class);
        solo.clickOnButton("Scan In");
        solo.assertCurrentActivity("ScanBook", ScanBook.class);
    }

    @Test
    public void clickSaveNoEntries() {
        solo.assertCurrentActivity("MyBookActivity", MyBookActivity.class);
        solo.clickOnButton("Save");
        solo.clickOnButton("OK");
    }

    /**
     * Will not add to DB because there is no user attached to it.
     */
    @Test
    public void addBook() {
        solo.assertCurrentActivity("MyBookActivity", MyBookActivity.class);
        solo.enterText((EditText) solo.getView(R.id.myBook_title_edittext), "Harry Potter and the Philosopher's Stone");
        solo.enterText((EditText) solo.getView(R.id.myBook_author_edittext), "J.K. Rowling");
        solo.enterText((EditText) solo.getView(R.id.myBook_isbn_edittext), "9780439554930");
        Button available = (Button) solo.getView(R.id.myBook_set_status);
        assertEquals(available.getText(), "Available");
        solo.clickOnButton("Save");
    }


    /**
     * Closes the activity after each test
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
