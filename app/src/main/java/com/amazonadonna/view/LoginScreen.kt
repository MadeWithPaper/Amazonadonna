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

//import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandle
import com.amazonaws.regions.Regions

import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext
import com.amazonadonna.artisanOnlyViews.HomeScreenArtisan

import kotlinx.android.synthetic.main.activity_login_screen.*
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import android.R.attr.password
import android.widget.Toast
import com.amazon.identity.auth.device.api.authorization.ProfileScope.userId
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
    private fun signUpNewArtisan(email: String, password: String) {

        if (!validateInput()){
            return
        }

        var userAttributes = CognitoUserAttributes()
        userAttributes.addAttribute("email", email)

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

            override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onFailure(exception: Exception?) {
                Log.d("LoginScreen", "in authHandler fail")
                Log.d("LoginScreen", exception?.message)
            }
        }


        var signUpHandler = object : SignUpHandler {
            override fun onSuccess(user: CognitoUser?, signUpConfirmationState: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?) {
                Log.d("LoginScreen", "in signUpHandler success")
                if (!signUpConfirmationState) { // Artisan needs to be confirmed, should have been automatically sent a message
                    Log.d("LoginScreen", "Signup not confirmed")
                    Toast.makeText(this@LoginScreen, "Please confirm your email and try again", Toast.LENGTH_LONG)
                } else { // User is good, allow entry into app
                    Log.d("LoginScreen", "Signup confirmed")
                    user?.getSessionInBackground(authenticationHandler)
                }
            }

            override fun onFailure(exception: Exception?) {
                Log.d("LoginScreen", "in signupHandler fail")
                Log.d("LoginScreen", exception?.message)
                if (exception?.message!!.contains("UsernameExistsException")) { // Hacky, should be a way to log in user without signing up
                    user.getSessionInBackground(authenticationHandler)
                }
            }
        }

        // Sign up this user
        userPool.signUpInBackground(email, password, userAttributes, null, signUpHandler)
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
            //signUpNewArtisan("teamamazonadonna@gmail.com", "Password1$")
            signUpNewArtisan(email_et.text.toString(), password_et.text.toString())
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
