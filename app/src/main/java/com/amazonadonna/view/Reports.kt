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
import com.amazonadonna.model.Payout
import kotlinx.android.synthetic.main.activity_view_report.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.EnumSet.range
import kotlin.collections.HashMap

class Reports : AppCompatActivity(), CoroutineScope {

    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val SELECT_REPORT_TYPE = "--Select Report Type--"
    private val reportTypeSpinnerValues = arrayOf(SELECT_REPORT_TYPE, "Performance", "Account Summary", "Account Payables")
    private var reportType = SELECT_REPORT_TYPE
    private var reportTargetList : MutableList<ReportTarget> = mutableListOf()
    private val TAG = "Reports.kt"
    private val SPACING = 15f
    private val payoutHistoryMap : HashMap<String, List<Payout>> = HashMap()

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

        if (selectedTargets.isEmpty()) {
            return
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
//            attachPayoutHistory(canvas, currArtisan.artisanId, paint)
            canvas.drawText("Payout History: ", 0f, 85f, paint)

            canvas.drawText("1 : 2/10/2019 $342.12", 10f, 100f, paint)
            canvas.drawText("2 : 2/25/2019 $3664.24", 10f, 115f, paint)
            canvas.drawText("3 : 3/11/2019 $123.45", 10f, 130f, paint)

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
            canvas.drawText("Account Summary Report", 80f, 10f, paint)
            //artisan info
            canvas.drawText("Artisan Name: ${currArtisan.artisanName}", 0f, 25f, paint)
            canvas.drawText("Account History: ${currArtisan.artisanName}", 0f, 40f, paint)
            canvas.drawText("1 : Name Plate Bar Bracelet $16.57 x 3 : $49.71", 10f, 55f, paint)
            canvas.drawText("2 : To Do List Coffee Cup $16.99 x 8 : $135.92", 10f, 70f, paint)
            canvas.drawText("3 : Kitchen Herbs Art $19.97 x 4 : $79.88", 10f, 85f, paint)
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
            canvas.drawText("Account Payables Report", 80f, 10f, paint)
            //artisan info
            canvas.drawText("Artisan Name: ${currArtisan.artisanName}", 0f, 25f, paint)
            canvas.drawText("Payable History", 0f, 40f, paint)
            canvas.drawText("1 : 1/1/2019 $342.12", 10f, 55f, paint)
            canvas.drawText("2 : 1/15/2019 $364.24", 10f, 70f, paint)
            canvas.drawText("3 : 2/1/2019 $123.45", 10f, 85f, paint)
            canvas.drawText("4 : 2/17/2019 $34222.12", 10f, 100f, paint)
            canvas.drawText("5 : 2/27/2019 $33664.24", 10f, 115f, paint)
            canvas.drawText("6 : 3/1/2019 $14423.45", 10f, 130f, paint)
            canvas.drawText("7 : 3/5/2019 $3452.12", 10f, 145f, paint)
            canvas.drawText("8 : 3/15/2019 $23664.24", 10f, 160f, paint)
            canvas.drawText("9 : 3/21/2019 $123.45", 10f, 175f, paint)
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

//    private fun attachPayoutHistory(canvas: Canvas, artisanId : String, paint : Paint) {
//        launch {
//            Log.d(TAG, "payout list:  $payoutList")
//            runOnUiThread {  var payoutNum = 1
//                payoutList.forEach {
//                    canvas.drawText("$payoutNum : ${it.date}, ${it.amount}", 0f, 70f + (SPACING * payoutNum), paint)
//                    payoutNum++
//                }
//            }
//        }
//    }

    private suspend fun getPayoutsByArtisanIDFromDb(artisanId: String) = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(application).payoutDao().getAllByArtisanId(artisanId) as List<Payout>
    }
}
