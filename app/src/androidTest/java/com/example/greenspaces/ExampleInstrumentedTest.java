package com.example.greenspaces;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.TestOnly;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private ArrayList<Location> locationArrayList = new ArrayList<>();
    private ArrayList<String> location_ids = new ArrayList<>();

    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    /* @Rule
    public ActivityScenarioRule homeActivityTestRule = new ActivityScenarioRule<>(HomeActivity.class); */

    @Rule
    public ActivityTestRule<HomeActivity> mActivityRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.greenspaces", appContext.getPackageName());
    }

    // Section 1: Search
    // test 1
    // Search Fragment is launched and displayed
    @Test
    public void searchFragment(){
        FragmentScenario.launchInContainer(SearchFragment.class);
        onView(withId(R.id.textView_filter)).check(matches(withText("FILTER")));
        onView(withId(R.id.toggleButton_map)).perform(click());
    }

    // Helper method to return the itemCount of a given recyclerView
    private int getListCount(RecyclerView view){
        return view.getAdapter().getItemCount();
    }

    // Section 1.1: List Search
    // test 1.1.1
    // List Fragment is launched with given args
    @Test
    public void listFragment(){
        locationArrayList.add(new Location("0001", "Test Location", "", "", "", 0.0f,false));
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putSerializable("locationArrayList", locationArrayList);
        FragmentScenario scenario = FragmentScenario.launchInContainer(ListFragment.class, fragmentArgs);
        scenario.onFragment(fragment -> {
            assertTrue(getListCount(fragment.getView().findViewById(R.id.recyclerView_list)) == 1);
        });
    }

    // test 1.1.2
    // When list fragment is loaded with no results, the no results dialogue is displayed
    @Test
    public void listFragmentNoResults(){
        FragmentScenario.launchInContainer(ListFragment.class);
        onView(withId(R.id.textView_noResults)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Before
    public void setUp() throws Exception{
        Intents.init();
    }

    // test 1.1.3
    // When location is clicked from listFragment, a location activity is opened
    @Test
    public void listLocationSelected(){
        locationArrayList.add(new Location("0001", "Test Location", "", "", "", 0.0f,false));
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putSerializable("locationArrayList", locationArrayList);
        FragmentScenario.launchInContainer(ListFragment.class, fragmentArgs);
        onView(withId(R.id.recyclerView_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        intended(hasComponent(LocationActivity.class.getName()));
    }

    // test 1.1.4
    // When location is clicked from listFragment, the correct location activity is opened
    @Test
    public void listLocationSelectedDisplayed() throws InterruptedException {
        locationArrayList.add(new Location("263969892647989534", "Test Location", "", "", "", 0.0f,false));
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putSerializable("locationArrayList", locationArrayList);
        FragmentScenario.launchInContainer(ListFragment.class, fragmentArgs);
        onView(withId(R.id.recyclerView_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // delay the check so that the http call has a moment to execute
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.countDown();
        countDownLatch.await();
        onView(withId(R.id.textView_locationName)).check(matches(withText("Harding Golf Course")));
    }

    @After
    public void tearDown() throws Exception{
        Intents.release();
    }

    // Section 1.2 Map Search
    // test 1.2.1
    // Map View is opened
    @Test
    public void mapFragment(){
        location_ids.add("263969892647989534");
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putStringArrayList("location_ids", location_ids);
        FragmentScenario.launchInContainer(MapFragment.class, fragmentArgs);
        // test incomplete

    }

    // test 1.2.2
    // When map fragment is loaded with no results, the no results dialogue is displayed
    @Test
    public void mapFragmentNoResults(){
        FragmentScenario.launchInContainer(MapFragment.class);
        onView(withId(R.id.textView_noResultsPop)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    // helper method to simulate click on screen
    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER
        );
    }

    // test 1.2.3
    // When location on map is clicked, popup window is opened
    @Test
    public void mapPopUpDisplayed() throws InterruptedException {
        location_ids.add("263969892647989534");
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putSerializable("location_ids", location_ids);
        FragmentScenario.launchInContainer(MapFragment.class, fragmentArgs);

        onView(withId(R.id.mapView)).perform(clickXY(100,450));
        // onView(withId(R.id.linearLayout_locationItem)).check(matches(isDisplayed()));
    }

    // Section 2: User Fragment
    // test 2
    // Open User Fragment
    @Test
    public void userFragment(){
        FragmentScenario.launchInContainer(UserFragment.class);
        onView(withId(R.id.textView_userName)).check(matches(isDisplayed()));
        onView(withId(R.id.button_userReviews)).perform(click());
    }

    // Section 2.1: User Reviews
    // test 2.1.1
    // RecyclerView of user reviews is displayed when 'My Reviews' button is clicked
    @Test
    public void userMyReviews(){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("user_id", "111000376207439384827");
        editor.commit();

        FragmentScenario.launchInContainer(UserFragment.class);
        onView(withId(R.id.button_userReviews)).perform(click());
        
        FragmentScenario scenario = FragmentScenario.launchInContainer(MyReviewsFragment.class);
        scenario.onFragment(fragment -> {
            assertTrue(getListCount(fragment.getView().findViewById(R.id.recyclerView_myReviews)) == 1);
        });
    }

    // test 2.1.2
    // No reviews dialogue is displayed when 'My Reviews' fragment does not return any reviews
    @Test
    public void noReviews(){
        FragmentScenario.launchInContainer(MyReviewsFragment.class);
        onView(withId(R.id.textView_noReviews)).check(matches(isDisplayed()));
    }

    // Section 2.2: User Photos
    // test 2.2
    // Flexbox with user photos is displayed when 'My Photos' button is clicked
    @Test
    public void myPhotos(){
        FragmentScenario.launchInContainer(UserFragment.class);
        onView(withId(R.id.button_userPhotos)).perform(click());

        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putString("id", "111000376207439384827");
        fragmentArgs.putString("parent", "user");
        FragmentScenario.launchInContainer(PhotosFragment.class, fragmentArgs);

        onView(withId(R.id.flexBox_photos)).check(matches(isDisplayed()));
    }

    // test 2.2.2
    // No photos dialogue is displayed when 'My Photos' does not return any photos
    @Test
    public void noPhotos(){
        Bundle fragmentArgs = new Bundle();
        fragmentArgs.putString("id", "5");
        fragmentArgs.putString("parent", "user");
        FragmentScenario.launchInContainer(PhotosFragment.class, fragmentArgs);

        onView(withId(R.id.textView_noPhotos)).check(matches(isDisplayed()));
    }
}