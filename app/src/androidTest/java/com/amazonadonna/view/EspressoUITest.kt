package com.amazonadonna.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.GrantPermissionRule
import org.junit.Test
import org.junit.Rule


class EspressoUITest {

    @get: Rule
    var activityRule : IntentsTestRule<HomeScreen> = IntentsTestRule(HomeScreen::class.java)

    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_CONTACTS)

    @Test
    fun listAllArtisans_button_pressed() {
        onView(withId(R.id.listAllArtisan)).perform(click())

        //check that page has changed
        intended(hasComponent(ListAllArtisans::class.java!!.getName()))




    }



}