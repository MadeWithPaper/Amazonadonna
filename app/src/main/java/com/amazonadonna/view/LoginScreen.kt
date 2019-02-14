package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.util.Log
import android.content.Intent

import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext

import kotlinx.android.synthetic.main.activity_login_screen.*


class LoginScreen : AppCompatActivity() {

    private var cgaID : String? = null
    private var requestContext : RequestContext = RequestContext.create(this)

    private var signUpListener = object  : AuthorizeListener() {
        /* Authorization was completed successfully. */
        override fun onSuccess(result: AuthorizeResult) {
            User.fetch(this@LoginScreen, getUserInfoListener)
            /* Your app is now authorized for the requested scopes */
            val intent = Intent(this@LoginScreen, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        /* There was an error during the attempt to authorize the application. */
        override fun onError(ae: AuthError) {
            /* Inform the user of the error */
        }

        /* Authorization was cancelled before it could be completed. */
        override fun onCancel(cancellation: AuthCancellation) {
            /* Reset the UI to a ready-to-login state */
        }
    }

    private var checkTokenListener = object  : Listener<AuthorizeResult, AuthError> {
        override fun onSuccess(ar: AuthorizeResult?) {
            if(ar?.accessToken != null) { //user already signed in to app
                User.fetch(this@LoginScreen, getUserInfoListener)

                val intent = Intent(this@LoginScreen, HomeScreen::class.java)
                startActivity(intent)
                Log.d("LoginScreen", ar?.accessToken)
                finish()
            }
        }

        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
        }
    }

    private var getUserInfoListener = object : Listener<User, AuthError> {
        override fun onSuccess(p0: User?) {
            cgaID = p0!!.userId.substringAfter("amzn1.account.")
            Log.d("LoginScreen",cgaID)
        }

        override fun onError(ae: AuthError?) {
            //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        requestContext.registerListener(signUpListener)


        email_sign_in_button.setOnClickListener { test()/*attemptLogin()*/ }

        login_with_amazon.setOnClickListener(View.OnClickListener {
            _ -> AuthorizationManager.authorize(AuthorizeRequest
                        .Builder(requestContext)
                        .addScopes(ProfileScope.profile(),  ProfileScope.userId())
                        .build())
        })
    }

    override fun onStart() {
        super.onStart()
        val scopes : Array<Scope> = arrayOf(ProfileScope.profile(), ProfileScope.postalCode())
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
