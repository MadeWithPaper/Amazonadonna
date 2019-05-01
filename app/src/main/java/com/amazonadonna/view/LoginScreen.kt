package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.util.Log
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.view.WindowManager

import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext

import kotlinx.android.synthetic.main.activity_login_screen.*

const val AUTHORITY = "com.amazonadonna.provider"
const val ACCOUNT_TYPE = "amazonadonna.com"
const val ACCOUNT = "dummyaccount3"
const val SECONDS_PER_MINUTE = 60L
const val SYNC_INTERVAL_IN_MINUTES = 60L
const val SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE

class LoginScreen : AppCompatActivity() {

    private var requestContext : RequestContext = RequestContext.create(this)
    private val scopes : Array<Scope> = arrayOf(ProfileScope.profile(), ProfileScope.postalCode(), ProfileScope.profile())
    private lateinit var alertDialog : AlertDialog

    private var signUpListener = object  : AuthorizeListener() {
        /* Authorization was completed successfully. */
        override fun onSuccess(result: AuthorizeResult) {
            /* Your app is now authorized for the requested scopes */
            Log.d("LoginScreen", "successful signup: "+result)
            val intent = Intent(this@LoginScreen, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        /* There was an error during the attempt to authorize the application. */
        override fun onError(ae: AuthError) {
            /* Inform the user of the error */
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            alertDialog.dismiss()
        }

        /* Authorization was cancelled before it could be completed. */
        override fun onCancel(cancellation: AuthCancellation) {
            /* Reset the UI to a ready-to-login state */
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            alertDialog.dismiss()
        }
    }

    private var checkTokenListener = object  : Listener<AuthorizeResult, AuthError> {
        override fun onSuccess(ar: AuthorizeResult?) {
            if(ar?.accessToken != null) { //user already signed in to app
                val intent = Intent(this@LoginScreen, HomeScreen::class.java)
                startActivity(intent)
                Log.d("LoginScreen", ar?.accessToken)
                finish()
            }
            else {
                Log.d("LoginScreen", "token not found: "+ar)
            }
        }

        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
            Log.d("LoginScreen", "error geting token: "+ae)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        requestContext.registerListener(signUpListener)

        email_sign_in_button.setOnClickListener { test()/*attemptLogin()*/ }

        login_with_amazon.setOnClickListener{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            AuthorizationManager.authorize(AuthorizeRequest
                    .Builder(requestContext)
                    .addScopes(ProfileScope.profile(), ProfileScope.postalCode(), ProfileScope.profile())
                    .showProgress(true)// if you change these, need to also change the scopes val at top to match
                    .build())

            alertDialog = AlertDialog.Builder(this@LoginScreen).create()
            alertDialog.setTitle("Logging In")
            alertDialog.setMessage("Please wait while login is completed...")
            alertDialog.setCanceledOnTouchOutside(false)
            alertDialog.show()
        }
    }

    override fun onStart() {
        super.onStart()
        AuthorizationManager.getToken(this, scopes, checkTokenListener)
    }

    override fun onResume() {
        super.onResume()
        requestContext.onResume()
    }

    private fun test() {
        // go to home screen
        val intent =  Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }
}
