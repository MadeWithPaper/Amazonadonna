package com.amazonadonna.amazonhandmade

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.GrantPermissionRule
import org.junit.Test
import org.junit.Rule

class EspressoUITest {

    @get: Rule
    var activityRule : IntentsTestRule<LoginScreen> = IntentsTestRule(LoginScreen::class.java)

    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_CONTACTS)

    @Test
    fun login_button_pressed() {

        onView(withId(R.id.email_sign_in_button)).perform(click())
        Log.d("INFO", "ran test")
        intended(hasComponent(HomeScreen::class.java.getName()))

    }

}