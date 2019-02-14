package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_artisan_payout.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.DatePicker
import android.app.DatePickerDialog
import android.util.Log
import java.time.Month
import javax.xml.datatype.DatatypeConstants.MONTHS


class ArtisanPayout : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artisan_payout)

        val artisan = intent.extras?.getSerializable("artisan") as Artisan

        artisanPayout_amount.setText(artisan.balance.toString())
        artisanPayout_dateTV.setText("Date: " + getCurrDate())
        artisanPayout_continue.setOnClickListener {
            continueToSignature()
        }

        artisanPayout_datePicker.setOnClickListener {
            pickDate()
        }
    }

    private fun pickDate() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        //Log.i("ArtisanPayout", "before Date: " + month + "/" + day + "/" + year)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in textbox
            Log.i("ArtisanPayout", "new picked Date: " + monthOfYear + "/" + dayOfMonth + "/" + year)
            artisanPayout_dateTV.setText("Date: " + monthOfYear + "/" + dayOfMonth + "/" + year)
            //lblDate.setText("" + dayOfMonth + " " + MONTHS[monthOfYear] + ", " + year)
        }, year, month+1, day)

        //Log.i("ArtisanPayout", "Date: " + month + "/" + day + "/" + year)

        dpd.show()
    }

    private fun getCurrDate() : String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
    private fun continueToSignature() {
        val intent = Intent(this, PayoutSignature::class.java)
        startActivity(intent)
    }
}
