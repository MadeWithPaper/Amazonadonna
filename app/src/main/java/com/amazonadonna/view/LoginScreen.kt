package com.amazonadonna.view

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.util.Log
import android.content.Intent
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

import com.amazonaws.regions.Regions

import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext
import com.amazonadonna.artisanOnlyViews.HomeScreenArtisan


import kotlinx.android.synthetic.main.activity_login_screen.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails

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
    var userPool = CognitoUserPool(this@LoginScreen, "us-east-2_ViMIOaCbk","4in76ncc44ufi8n1sq6m5uj7p7", "12qfl0nmg81nlft6aunvj6ec0ocejfecdau80biodpubkfuna0ee", Regions.US_EAST_2)


    /**
     * Amazon Cognito for Artisans
     */
<<<<<<< HEAD
    private fun signInArtisan(email: String, password: String) {
=======

    private fun signUpNewArtisanDemo() {
        val intent =  Intent(this@LoginScreen, HomeScreenArtisan::class.java)
        //intent.putExtra("artisanID", idToken)
        startActivity(intent)
    }

    //TODO call this if password needs to be updated
    private fun updateArtisanPassword(email: String){
        val intent =  Intent(this@LoginScreen, ArtisanUpdatePassword::class.java)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }

    private fun signUpNewArtisan(email: String, password: String) {

        if (!validateInput()){
            return
        }

        //disable touch events once log in button is clicked
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        var userAttributes = CognitoUserAttributes()
        userAttributes.addAttribute("email", email)

>>>>>>> e07feef8905fc021ac0e77163eb793d7e32d1654
        var user = userPool.getUser(email)


        var authenticationHandler = object : AuthenticationHandler {
            override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                // Sign-in was successful, cognitoUserSession will contain tokens for the user
                Log.d("LoginScreen", "in authHandler success")
                var idToken = userSession?.idToken?.jwtToken
                Log.d("LoginScreen", idToken)
                // go to home screen

                val intent =  Intent(this@LoginScreen, HomeScreenArtisan::class.java)
                intent.putExtra("artisanID", idToken)
                startActivity(intent)
            }

            override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation?, userId: String?) {
                // The API needs user sign-in credentials to continue
                val authenticationDetails = AuthenticationDetails(userId, password, null)

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation?.setAuthenticationDetails(authenticationDetails)

                // Allow the sign-in to continue
                authenticationContinuation?.continueTask()
            }

            override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                // Multi-factor authentication is required, get the verification code from user
                //continuation?.setMfaCode(mfaVerificationCode)
                // Allow the sign-in process to continue
                continuation?.continueTask()
            }

            // Method is called when user logs in for first time with temp password
            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                Log.d("LoginScreen", "New user with temp password logging in")
                // Check the challenge name
                if("NEW_PASSWORD_REQUIRED".equals(continuation?.challengeName)) {
                    // A new user is trying to sign in for the first time after
                    // admin has created the user’s account
                    // Cast to NewPasswordContinuation for easier access to challenge parameters
                    var newPasswordContinuation : NewPasswordContinuation? = continuation as NewPasswordContinuation;

                    // Get the list of required parameters
                    var requiredAttributes = newPasswordContinuation?.requiredAttributes

                    // Get the current user attributes
                    var currUserAttributes = newPasswordContinuation?.currentUserAttributes

                    // Prompt user to set a new password and values for required attributes

                    // Set new user password
                    newPasswordContinuation?.setPassword(password)


                    // Allow the sign-in to complete
                    newPasswordContinuation?.continueTask();
                }
            }

            override fun onFailure(exception: Exception?) {
                Log.d("LoginScreen", "in authHandler fail")
                Log.d("LoginScreen", exception?.message)
            }
        }

        user.getSessionInBackground(authenticationHandler)
    }


    /**
     * Amazon OAuth for CGAs
     */
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

        //TODO implement Artisan login
        artisan_log_in_button.setOnClickListener {
<<<<<<< HEAD
            signInArtisan("teamamazonadonna@gmail.com", "Password1$")
=======
            //signUpNewArtisan("teamamazonadonna@gmail.com", "Password1$")
            //signUpNewArtisanDemo()
            updateArtisanPassword(email_et.text.toString())
            //signUpNewArtisan(email_et.text.toString(), password_et.text.toString())
>>>>>>> e07feef8905fc021ac0e77163eb793d7e32d1654
        }

        cga_log_in_button.setOnClickListener{
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

        log_in_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })
    }

    override fun onStart() {
        super.onStart()
        AuthorizationManager.getToken(this, scopes, checkTokenListener)
    }

    override fun onResume() {
        super.onResume()
        requestContext.onResume()
    }

    private fun validateInput() : Boolean {
        if (email_et.text.toString().isEmpty()){
            email_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (!(email_et.text.toString().contains("@"))){
            email_til.error = this.resources.getString(R.string.error_invalid_email)
            return false
        }

        if (password_et.text.toString().isEmpty()){
            password_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        return true
    }

    private fun test() {
        // go to home screen
        val intent =  Intent(this, HomeScreenArtisan::class.java)
        startActivity(intent)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
