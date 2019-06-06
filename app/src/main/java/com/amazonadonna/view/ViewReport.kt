package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.graphics.pdf.PdfRenderer
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import kotlinx.android.synthetic.main.activity_view_report.*
import java.io.File
import java.io.IOException

class ViewReport : AppCompatActivity() {

    private val TAG = "ViewReport.kt"
    private lateinit var docName: String
    private lateinit var file : File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)
        docName = intent.extras!!.getString("reportName")!!
        showPage()

        viewReport_save.setOnClickListener {
            saveReport()
        }
    }

    private fun showPage() {
        file = File(this.filesDir, docName)

        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var rendererPage: PdfRenderer.Page? = null

        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            //min. API Level 21
            pdfRenderer = PdfRenderer(fileDescriptor!!)

            //Display page 0
            rendererPage = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(
                    resources.displayMetrics.densityDpi * rendererPage.width / 72,
                    resources.displayMetrics.densityDpi * rendererPage.height / 72,
                    Bitmap.Config.ARGB_8888
            )

            rendererPage.render(bitmap, null, null,
                    PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

            pdfImageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            rendererPage!!.close()
            pdfRenderer!!.close()
            fileDescriptor!!.close()
        }
    }

    private fun saveReport() {
        val intent = Intent(this, Reports::class.java)
        startActivity(intent)
        finish()
    }
}
