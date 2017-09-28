package ru.evendate.android;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ru.evendate.android.ui.feed.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmitry on 16.09.17.
 */
@RunWith(AndroidJUnit4.class)
public class OnboardingIntentTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule
            = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void verifyOnboarding() {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.SHOW_ONBOARDING, true);
        mActivityRule.launchActivity(intent);
        onView(withText(mActivityRule.getActivity().getString(R.string.dialog_onboadring_title)))
                .check(matches(isDisplayed()));
    }
}
