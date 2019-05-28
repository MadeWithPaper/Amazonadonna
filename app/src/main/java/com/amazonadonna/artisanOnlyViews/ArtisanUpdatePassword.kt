package com.amazonadonna.artisanOnlyViews

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.activity_artisan_update_password.*

class ArtisanUpdatePassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_update_password)

        if (intent.extras != null) {
            //pre-fill in email passed from login screen
            artisanUpdate_email_et.setText(intent.getStringExtra("email"))
        } else {
            //Intent null, email was not included
        }

        artisanUpdatePasswordButton.setOnClickListener {
            updateArtisanAccountPassword()
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

    private fun updateArtisanAccountPassword(){
        if (!fieldValidation()) {
            //error in field validation
            return
        }

        //TODO update artisan cognito password
        //TODO log user in
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
