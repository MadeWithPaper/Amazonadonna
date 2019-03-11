package com.amazonadonna.view

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
import kotlinx.android.synthetic.main.activity_report_type.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ReportType : AppCompatActivity(), CoroutineScope {

    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val SELECT_REPORT_TYPE = "--Select Report Type--"
    private val reportTypeSpinnerValues = arrayOf(SELECT_REPORT_TYPE, "Fiance", "Performance", "Account Summary", "Account Payables")
    private var reportType = SELECT_REPORT_TYPE
    private var reportTargetList : MutableList<ReportTarget> = mutableListOf()
    private val TAG = "ReportType.kt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_type)

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

        Log.i(TAG, "selected list: $selectedTargets")
    }

    private fun validateInfo() : Boolean {
        if (reportType == SELECT_REPORT_TYPE){
            return false
        }

        //TODO add more checks if necessary
        return true
    }
}
