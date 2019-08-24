package com.abhishek.calllog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.net.ConnectivityManager
import android.os.Build
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.provider.CallLog
import com.abhishek.calllog.model.CallDetail
import com.abhishek.calllog.model.DataHolder
import com.google.gson.Gson

class OutGoingBroadCast : BroadcastReceiver() {

    var total_outgoing_Call=0
    var list = mutableListOf<CallDetail>()
    var sharedPreferences :SharedPreferences?=null
    override fun onReceive(context: Context?, intent: Intent?) {

        sharedPreferences =  context?.getSharedPreferences("com.abhishek,calllog.CallModel", Context.MODE_PRIVATE)
        val action = intent?.action

        action?.apply {
            if (contains(Intent.ACTION_NEW_OUTGOING_CALL))
            {
                val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                Log.i("Bradcast::","Outgoing: No:: $number")

                if (number.equals("9911222259") || number.equals("+919911222259"))
                {
                    Log.i("Bra","number find::")
                    sharedPreferences?.edit()?.putString("callNo",number)?.commit()
                 }
                else
                {
                    Log.i("Bra","number NOT find::")
                    sharedPreferences?.edit()?.putString("callNo",null)?.commit()

                    Log.i("Bra","shared pre::${sharedPreferences?.getString("callNo",null)}")

                }
        }


        val phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

         if (TelephonyManager.EXTRA_STATE_IDLE.equals(phoneState)) {

             val selection = "${CallLog.Calls.NUMBER} LIKE ?"

             val callNo = sharedPreferences?.getString("callNo",null)

             Log.i("Bra","callNo on phone cut::$callNo")

             if (callNo!=null)
             {
                 val managedCursor= context?.contentResolver?.query(CallLog.Calls.CONTENT_URI, null, selection, arrayOf("%9911222259%"),CallLog.Calls.DATE+" DESC")

                 managedCursor?.apply {
                     val number = getColumnIndex(CallLog.Calls.NUMBER)
                     val type = getColumnIndex(CallLog.Calls.TYPE)
                     val date = getColumnIndex(CallLog.Calls.DATE)
                     val duration = getColumnIndex(CallLog.Calls.DURATION)
                     while (moveToNext())
                     {
                         Log.i("Bra","callNo move type::${Integer.parseInt(getString(type))}")
                         if (Integer.parseInt(getString(type))==CallLog.Calls.OUTGOING_TYPE || Integer.parseInt(getString(type))==CallLog.Calls.BLOCKED_TYPE)
                         {
                             val model = CallDetail(getString(number),getString(date),getString(duration))
                             Log.i("Braod"," number : ${getString(number)}")
                             Log.i("Braod"," duration : ${getString(duration)}")
                             Log.i("Braod"," date : ${getString(date)}")
                             list.add(model)
                         }
                     }
                     close()
                     val dataholder = DataHolder()
                     dataholder.data = list
                     Log.i("Bra",sharedPreferences?.getString("call",null))
                     sharedPreferences?.edit()?.putString("call", Gson().toJson(dataholder))?.commit()
                     Log.i("Bra",sharedPreferences?.getString("call",null))
                 }

             }
         }

//        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
//
//            val noConnectivity = intent?.getBooleanExtra(
//                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false
//            )
//            if (noConnectivity!!.not()) {
//                Toast.makeText(context,"Wifi is connected",Toast.LENGTH_SHORT).show()
//                Log.i("BradCastReiver","wifi connection is connected")
//           }
//           else {
//               Log.i("BradCastReiver","wifi connection is lost")
//            }
       }
    }

    fun callType(type:Int): String {
        Log.i("Bra","$type")

        when(type)
        {


            CallLog.Calls.OUTGOING_TYPE -> {
                total_outgoing_Call++
                return "OutGoing"
            }

            CallLog.Calls.INCOMING_TYPE -> {

                return "Incoming"
            }
        }
        return ""
    }
}