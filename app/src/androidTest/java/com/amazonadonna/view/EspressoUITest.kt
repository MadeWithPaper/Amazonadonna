package com.amazonadonna.view

import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.rule.GrantPermissionRule
import org.junit.Test
import org.junit.Rule

class EspressoUITest {

    @get: Rule
    var activityRule : IntentsTestRule<HomeScreen> = IntentsTestRule(HomeScreen::class.java)

    @Rule @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_CONTACTS)

    @Test
    fun login_button_pressed() {


    }

}