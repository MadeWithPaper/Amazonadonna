package com.amazonadonna.model

import android.content.Context

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.regions.Regions

class SigninHelper(context : Context, signupCallback : SignUpHandler) {

    private val userPool = CognitoUserPool(context, "us-east-1_8U1ix0dOw", "143e5qqmq52kq9tkvqvvvau2t4", null, Regions.US_EAST_1)
    private val signupCallback = signupCallback

    fun signUpArtisan(username: String, password: String, email: String?, phoneNumber: String?) {
        // Create a CognitoUserAttributes object and add user attributes
        val userAttributes = CognitoUserAttributes()

        // Add the user attributes. Attributes are added as key-value pairs
        // Adding user's phone number
        if (phoneNumber != null) {
            userAttributes.addAttribute("phone_number", phoneNumber)
        }

        // Adding user's email address
        userAttributes.addAttribute("email", email)

        userPool.signUpInBackground(username, password, userAttributes, null, signupCallback)
    }

}