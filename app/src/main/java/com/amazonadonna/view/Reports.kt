package com.amazonadonna.view

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amazonadonna.database.AppDatabase
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.ReportTarget
import kotlinx.android.synthetic.main.activity_reports.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import android.graphics.pdf.PdfDocument
import kotlinx.android.synthetic.main.activity_view_report.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.EnumSet.range


class Reports : AppCompatActivity(), CoroutineScope {

    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val SELECT_REPORT_TYPE = "--Select Report Type--"
    private val reportTypeSpinnerValues = arrayOf(SELECT_REPORT_TYPE, "Performance", "Account Summary", "Account Payables")
    private var reportType = SELECT_REPORT_TYPE
    private var reportTargetList : MutableList<ReportTarget> = mutableListOf()
    private val TAG = "Reports.kt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        //report target recycler view setup
        reportTargetRV.layoutManager = LinearLayoutManager(this)
        reportTargetRV.adapter = ReportTargetAdapter(this, reportTargetList)
        reportTargetRV.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        //report type spinner setup
        val reportTypeArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reportTypeSpinnerValues)
        reportTypeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reportTypeSpinner.adapter = reportTypeArrayAdapter

        reportTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reportType = reportTypeSpinner.selectedItem.toString()
            }
        }

        generateReports_Button.setOnClickListener {
            generateReport()
        }

        reportSelectAll.setOnClickListener {
            selectAll(true)
            reportTargetRV.adapter!!.notifyDataSetChanged()
        }

        reportSelectNone.setOnClickListener {
            selectAll(false)
            reportTargetRV.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onStart() {
        super.onStart()
        job = Job()

        launch {
            val dbArtisans: List<Artisan> = getArtisansFromDb()
            reportTargetList = reportTargetWrapper(dbArtisans)
            runOnUiThread {
                reportTargetRV.adapter = ReportTargetAdapter(applicationContext, reportTargetList)
            }
        }

        Log.d("ListAllArtisans", "fetching")
    }

    private fun reportTargetWrapper(artisans : List<Artisan>) : MutableList<ReportTarget> {
        val wrappedList : MutableList<ReportTarget> = mutableListOf()
        artisans.forEach {
            wrappedList.add(ReportTarget(it, false))
        }
        return wrappedList
    }

    private suspend fun getArtisansFromDb() = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(application).artisanDao().getAll() as List<Artisan>
    }

    private fun selectAll(mode : Boolean) {
        reportTargetList.forEach{
            it.selected = mode
        }
    }

    private fun generateReport() {
        if (!validateInfo()){
            Log.d(TAG, "failed validate check")
            return
        }
        val selectedTargets : MutableList<ReportTarget> = mutableListOf()
        reportTargetList.forEach{
            if (it.selected){
                selectedTargets.add(it)
            }
        }

        when (reportType) {
            "Performance" -> generatePerformanceReport(selectedTargets)
            "Account Summary" -> generateSummaryReport(selectedTargets)
            "Account Payables" -> generatePayablesReport(selectedTargets)
        }
        Log.i(TAG, "generated $reportType")
        Log.i(TAG, "selected list: $selectedTargets")
    }

    private fun validateInfo() : Boolean {
        if (reportType == SELECT_REPORT_TYPE){
            return false
        }

        //TODO add more checks if necessary
        return true
    }

    //TODO optimization pdf scaling
//    Artisans/Communities Performance/Stats/Transaction History Report
    private fun generatePerformanceReport(selectedTargets : MutableList<ReportTarget>) {
        // create a new document
        val document = PdfDocument()
        // crate a page description
        var pageNum = 1
        selectedTargets.forEach{
            //Log.i(TAG, "index $pageNum, ${selectedTargets.size}")
            val currArtisan = it.artisan
            val pageInfo = PdfDocument.PageInfo.Builder(300, 400, pageNum).create()
            // start a page
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            //title
            canvas.drawText("Performance Report", 100f, 10f, paint)
            //artisan info
            canvas.drawText("Date: ${getCurrentDate("date")}", 0f, 25f, paint)
            canvas.drawText("Artisan Name: ${currArtisan.artisanName}", 0f, 40f, paint)
            canvas.drawText("Balance: $${currArtisan.balance}", 0f, 55f, paint)
            canvas.drawText("Item Sold: ${(0..100).random()}", 0f, 70f, paint)
            attachPayoutHistory(canvas)
            //canvas.drawText("Payout History: ${currArtisan.artisanName}", 0f, 25f, paint)

            // finish the page
            document.finishPage(page)
            pageNum++
        }
            //save pdf file
        val pdfName = "Performance_Report_${getCurrentDate("file")}.pdf"
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = openFileOutput(pdfName, Context.MODE_PRIVATE)
            document.writeTo(fileOutputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
            document.close()
        }

        val intent = Intent(this, ViewReport::class.java)
        intent.putExtra("reportName", pdfName)
        startActivity(intent)
        finish()
    }
    //    Artisans/Communities Accounting summary (units sold, $) / Financial Report
    private fun generateSummaryReport(selectedTargets : MutableList<ReportTarget>) {
        // create a new document
        val document = PdfDocument()
        // crate a page description
        var pageNum = 1
        selectedTargets.forEach{
            //Log.i(TAG, "index $pageNum, ${selectedTargets.size}")
            val currArtisan = it.artisan
            val pageInfo = PdfDocument.PageInfo.Builder(300, 400, pageNum).create()
            // start a page
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            //title
            canvas.drawText("Account Summary Report", 100f, 10f, paint)
            //artisan info
            canvas.drawText("Artisan Name: ${currArtisan.artisanName}", 0f, 25f, paint)
            // finish the page
            document.finishPage(page)
            pageNum++
        }
        //save pdf file
        val pdfName = "Account_Summary_Report_${getCurrentDate("file")}.pdf"
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = openFileOutput(pdfName, Context.MODE_PRIVATE)
            document.writeTo(fileOutputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
            document.close()
        }

        val intent = Intent(this, ViewReport::class.java)
        intent.putExtra("reportName", pdfName)
        startActivity(intent)
        finish()
    }

    //    Artisans/Communities Account Payables/Receivables Report
    private fun generatePayablesReport(selectedTargets : MutableList<ReportTarget>) {
        // create a new document
        val document = PdfDocument()
        // crate a page description
        var pageNum = 1
        selectedTargets.forEach{
            //Log.i(TAG, "index $pageNum, ${selectedTargets.size}")
            val currArtisan = it.artisan
            val pageInfo = PdfDocument.PageInfo.Builder(300, 400, pageNum).create()
            // start a page
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            //title
            canvas.drawText("Account Payables Report", 100f, 10f, paint)
            //artisan info
            canvas.drawText("Artisan Name: ${currArtisan.artisanName}", 0f, 25f, paint)
            // finish the page
            document.finishPage(page)
            pageNum++
        }
        //save pdf file
        val pdfName = "Account_payables_Report_${getCurrentDate("file")}.pdf"
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = openFileOutput(pdfName, Context.MODE_PRIVATE)
            document.writeTo(fileOutputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            fileOutputStream?.close()
            document.close()
        }

        val intent = Intent(this, ViewReport::class.java)
        intent.putExtra("reportName", pdfName)
        startActivity(intent)
        finish()
    }

    private fun getCurrentDate(format : String) : String {
        var dateFormat : SimpleDateFormat? = null
        when (format){
            "file" -> dateFormat = SimpleDateFormat("yyy_mm_dd_HHmmss", Locale.getDefault())
            "date" -> dateFormat = SimpleDateFormat("dd-mm-yyyy", Locale.getDefault())
        }
        //val dateFormat = SimpleDateFormat("yyy_mm_dd_HHmmss", Locale.getDefault())
        val date = Date()
        return dateFormat!!.format(date)
    }

    private fun attachPayoutHistory(canvas: Canvas) {

    }
}
