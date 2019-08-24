package com.abhishek.calllog

import android.Manifest
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.provider.CallLog
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.abhishek.calllog.model.CallDetail
import com.abhishek.calllog.model.DataHolder
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val tag = "MainActivty"
    val READ_CALL_LOG = 1
    val PHONE_STATE = 2
    var list = mutableListOf<CallDetail>()
    val OUTGOING_CALL = 3
    lateinit var sharedPreferences:SharedPreferences

    val adapt  = CallAdapter()
    lateinit var listner: FragmentCallBack
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("com.abhishek,calllog.CallModel", Context.MODE_PRIVATE)
        Log.i(tag, "onCreate")
        listner = object : FragmentCallBack {
            override fun callBackListner(value: String) {
                button.text = value
            }
        }
        val bottomSheetFragment = BottomSheetFragment()
        button.setOnClickListener {
            bottomSheetFragment.show(supportFragmentManager, "bottom")
        }

        recycler_view.apply {

            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter =adapt
        }

        tv_count.text = adapt.itemCount.toString()
        permistions()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL)
        registerReceiver(OutGoingBroadCast(), intentFilter)

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALL_LOG)== PackageManager.PERMISSION_GRANTED)
        {
            syncCall()
        }
    }

    private fun permistions() {

        // Read call log permistion
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), READ_CALL_LOG);
            } else {
                println("READ_CALL_LOG request permisiion")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), READ_CALL_LOG);
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.PROCESS_OUTGOING_CALLS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.PROCESS_OUTGOING_CALLS), OUTGOING_CALL);
            } else {
                println("outgoing request permisiion")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.PROCESS_OUTGOING_CALLS), OUTGOING_CALL);
            }
        }


        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE))
            {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    PHONE_STATE)
            } else {
                println("read phonestate permisiion")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    PHONE_STATE
                )
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {
            OUTGOING_CALL-> {

                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "PROCESS_OUTGOING_CALLS Permistion Granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "PROCESS_OUTGOING_CALLS No Permistion Granted!", Toast.LENGTH_SHORT).show();
                    }
                    return
                }
            }
            PHONE_STATE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(this, "READ_PHONE_STATE Permistion Granted! for phone state", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, " READ_PHONE_STATE No Permistion Granted! for phone state", Toast.LENGTH_SHORT).show();
                    }
                    return
                }
            }

            READ_CALL_LOG -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                        == PackageManager.PERMISSION_GRANTED
                    ) {

                        Toast.makeText(this, "READ_CALL_LOG Permistion Granted!", Toast.LENGTH_SHORT).show();
                        syncCall()
                    } else {
                        Toast.makeText(this, "READ_CALL_LOG No Permistion Granted!", Toast.LENGTH_SHORT).show();
                    }
                    return
                }
            }
        }

    }

    fun syncCall()
    {
        val listmodel= sharedPreferences.getString("call",null)
        println("listmodel :: $listmodel")
        if (listmodel==null)
        {
            list.clear()

            val selection = "${CallLog.Calls.NUMBER} LIKE ?"


            val managedCursor= contentResolver?.query(
                CallLog.Calls.CONTENT_URI, null, selection, arrayOf("%9911222259%"),
                CallLog.Calls.DATE+" DESC")

            Log.i("Bradcast::","cursor::$managedCursor")

            managedCursor?.apply {
                val number = getColumnIndex(CallLog.Calls.NUMBER)
                val type = getColumnIndex(CallLog.Calls.TYPE)
                val date = getColumnIndex(CallLog.Calls.DATE)
                val duration = getColumnIndex(CallLog.Calls.DURATION)

                while (moveToNext())
                {
                    if (Integer.parseInt(getString(type))==CallLog.Calls.OUTGOING_TYPE
                        || Integer.parseInt(getString(type))==CallLog.Calls.BLOCKED_TYPE)
                    {

                        val model = CallDetail(getString(number),getString(date),getString(duration))
//                        Log.i("Braod"," number : ${getString(number)}")
//                        //Log.i("Braod"," type : ${callType(Integer.parseInt(getString(type)))}")
//                        Log.i("Braod"," duration : ${getString(duration)}")
//                        Log.i("Braod"," date : ${getString(date)}")
                        list.add(model)
                    }

                }
                close()
                adapt.addData(list)
                tv_count.text = adapt.itemCount.toString()
                val dataholder = DataHolder()
                dataholder.data = list

                sharedPreferences.edit().putString("call", Gson().toJson(dataholder)).commit()
            }
        }
        else
        {

            val list =   Gson().fromJson<DataHolder>(listmodel,DataHolder::class.java)
            println("dataholder :::${list.data}")
            list.data?.let {

                adapt.addData(it.toMutableList())
                tv_count.text = adapt.itemCount.toString()
            }

        }
    }
}
