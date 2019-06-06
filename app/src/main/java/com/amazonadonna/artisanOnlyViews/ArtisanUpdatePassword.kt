package com.amazonadonna.artisanOnlyViews

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import android.util.Log
import com.amazonadonna.view.R
import com.amazonadonna.model.Artisan
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.regions.Regions
import kotlinx.android.synthetic.main.activity_artisan_update_password.*

class ArtisanUpdatePassword : AppCompatActivity() {

    private var userPool = CognitoUserPool(this@ArtisanUpdatePassword, this.resources.getString(R.string.userPoolID),this.resources.getString(R.string.clientID), this.resources.getString(R.string.clientScret), Regions.US_EAST_2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_update_password)
        var artisan : Artisan? = null

        if (intent.extras != null) {
            //pre-fill in email passed from login screen
            artisanUpdate_email_et.setText(intent.getStringExtra("email"))
            artisan = intent.extras!!.get("artisan") as Artisan
        } else {
            //Intent null, email was not included
        }

        artisanUpdatePasswordButton.setOnClickListener {
            updateArtisanAccountPassword(artisan!!)
        }

        artisanUpdatePasswordLayout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                hideKeyboard(v)
                return true
            }
        })
    }

    private fun fieldValidation() : Boolean {
        //TODO add more checks to according to cognito password requirements
        if (artisanUpdate_email_et.text.toString().isEmpty()){
            artisanUpdate_email_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (!(artisanUpdate_email_et.text.toString().contains("@"))){
            artisanUpdate_email_til.error = this.resources.getString(R.string.error_invalid_email)
            return false
        }

        if (artisanUpdate_password_et.text.toString().isEmpty()){
            artisanUpdate_password_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (artisanUpdate_confirm_password_et.text.toString().isEmpty()){
            artisanUpdate_confirm_password_til.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        if (artisanUpdate_confirm_password_et.text.toString() != artisanUpdate_password_et.text.toString()){
            artisanUpdate_confirm_password_til.error = this.resources.getString(R.string.artisan_update_password_password_not_match_error)
            return false
        }

        return true
    }

    private fun updateArtisanAccountPassword(artisan: Artisan){
        if (!fieldValidation()) {
            //error in field validation
            return
        }

        var user = userPool.getUser(artisan.email)

        var changePasswordHandler = object : GenericHandler {
            override fun onSuccess() {
                Log.d("ArtisanUpdatePassword", "in updatepassword success")
                artisan.newAccount = false
                val intent =  Intent(this@ArtisanUpdatePassword, HomeScreenArtisan::class.java)
                intent.putExtra("artisan", artisan)
                startActivity(intent)
            }

            override fun onFailure(exception: Exception?) {
                Log.d("ArtisanUpdatePassword", "update password failed: "+exception?.message)
                Log.d("ArtisanUpdatePassword", "update password failed: "+exception?.localizedMessage)
                Toast.makeText(this@ArtisanUpdatePassword, "Unable to change password. Please " +
                        "ensure old password is correct and you have new password is the same in both fields.", Toast.LENGTH_LONG)
            }
        }

        user.changePassword(artisanUpdate_oldpassword_et.text.toString(), artisanUpdate_password_et.text.toString(), changePasswordHandler)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
