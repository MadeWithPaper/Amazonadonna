package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_artisan_payout.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import androidx.appcompat.app.AlertDialog
import android.util.Log
import com.amazonadonna.view.R
import kotlinx.android.synthetic.main.activity_artisan_profile.*


class ArtisanPayout : AppCompatActivity() {

    private val TAG = "ArtisanPayout.kt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_payout)

        val artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanPayout_balance.text = " $ ${artisan.balance}"
        artisanPayout_amount.setText(artisan.balance.toString())
        artisanPayout_dateTV.text = this.resources.getString(R.string.payout_date) + " " + getCurrDate()
        artisanPayout_continue.setOnClickListener {
            continueToSignature(artisan)
        }

        artisanPayout_datePicker.setOnClickListener {
            pickDate()
        }

    }

    private fun pickDate() {
        val date: Calendar = Calendar.getInstance()
        var initYear = date.get(Calendar.YEAR)
        var initMonth = date.get(Calendar.MONTH)
        var initDay = date.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            initMonth = monthOfYear + 1
            initDay = dayOfMonth
            initYear = year

            Log.i("ArtisanPayout", "new picked Date: " + initMonth + "/" + initDay + "/" + initYear)
            artisanPayout_dateTV.setText(this.resources.getString(R.string.payout_date) + initMonth + "/" + initDay + "/" + initYear)
            //lblDate.setText("" + dayOfMonth + " " + MONTHS[monthOfYear] + ", " + year)
        }, initYear, initMonth, initDay)
        dpd.show()
    }

    private fun getCurrDate(): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    private fun continueToSignature(artisan: Artisan) {
        //validate fields
        if (!validateFields()) {
            return
        } else {
            if (validateAmount(artisan)) {
                Log.w("ArtisanPayout", "Payout amount exceeded balance")
                val builder = AlertDialog.Builder(this@ArtisanPayout)
                builder.setTitle(this.resources.getString(R.string.payout_error_title))
                builder.setMessage(this.resources.getString(R.string.payout_error_message))
                builder.setOnDismissListener {
                    //Do nothing
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                val intent = Intent(this, PayoutSignature::class.java)
                intent.putExtra("artisan", artisan)
                intent.putExtra("payoutAmount", artisanPayout_amount.text.toString().toDouble())
                startActivity(intent)
                finish()
            }
        }
    }

    private fun validateAmount(artisan: Artisan): Boolean {
        return (artisanPayout_amount.text.toString().toDouble() > artisan.balance)
    }

    private fun validateFields(): Boolean {
        if (artisanPayout_amount.text.toString() == "."){
            artisanPayout_amount.error = this.resources.getString(R.string.payout_amount_format_error)
            return false
        }

        if (artisanPayout_amount.text.toString().isEmpty()){
            artisanPayout_amount.error = this.resources.getString(R.string.requiredFieldError)
            return false
        }

        return true
    }
}
