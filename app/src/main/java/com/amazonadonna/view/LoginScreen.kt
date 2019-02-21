package com.amazonadonna.view

import android.accounts.Account
import android.arch.persistence.room.RoomDatabase
import android.content.ContentResolver
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.util.Log
import android.content.Intent

import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.sync.ArtisanSync

import kotlinx.android.synthetic.main.activity_login_screen.*

const val AUTHORITY = "com.amazonadonna.provider"
const val ACCOUNT_TYPE = "amazonadonna.com"
const val ACCOUNT = "dummyaccount3"
const val SECONDS_PER_MINUTE = 60L
const val SYNC_INTERVAL_IN_MINUTES = 60L
const val SYNC_INTERVAL = SYNC_INTERVAL_IN_MINUTES * SECONDS_PER_MINUTE

class LoginScreen : AppCompatActivity() {

    private var requestContext : RequestContext = RequestContext.create(this)
    private lateinit var mAccount : Account
    // A content resolver for accessing the provider
    private lateinit var mResolver: ContentResolver

    private var signUpListener = object  : AuthorizeListener() {
        /* Authorization was completed successfully. */
        override fun onSuccess(result: AuthorizeResult) {
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
                val intent = Intent(this@LoginScreen, HomeScreen::class.java)
//                intent.putExtra("cgaId", cgaID)
                startActivity(intent)
                Log.d("LoginScreen", ar?.accessToken)
                finish()
            }
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

        //--------------------------------------------------------//
        // UNCOMMENT THE METHOD CALL BELOW TO CLEAR SQLITE TABLES //
        //--------------------------------------------------------//
         //ArtisanSync.resetLocalDB(applicationContext)
        //--------------------------------------------------------//

        ArtisanSync.sync(applicationContext)
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

    // Create a new dummy account for the sync adapter
    /*private fun createSyncAccount(): Account {
        val accountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        return Account(ACCOUNT, ACCOUNT_TYPE).also { newAccount ->
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {
                /*
                 * If you don't set android:syncable="true" in
                 * in your <provider> element in the manifest,
                 * then call context.setIsSyncable(account, AUTHORITY, 1)
                 * here.
                 */
            } else {
                /*
                 * The account exists or some other error occurred. Log this, report it,
                 * or handle it internally.
                 */
                Log.e("LoginScreen", "Error creating sync account")
            }
        }
    }*/
}
