package com.amazonadonna.amazonhandmade

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Test
import androidx.test.rule.ActivityTestRule
import org.junit.Rule

class EspressoUITest {

    @get: Rule
    var activityRule : ActivityTestRule<LoginScreen> = ActivityTestRule(LoginScreen::class.java)


    @Test
    fun login_button_pressed() {

        //TODO add false emails and passwords
        Intents.init()
        onView(withId(R.id.email_sign_in_button)).perform(click())
        println("RUNNING A TEST IN UI")
        intended(hasComponent(HomeScreen::class.java.getName()))
        Intents.release()
    }


}