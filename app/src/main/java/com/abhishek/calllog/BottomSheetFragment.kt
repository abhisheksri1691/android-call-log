package com.abhishek.calllog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import kotlinx.android.synthetic.main.buttom_sheet_fragment.*

class BottomSheetFragment : BottomSheetDialogFragment() {
    var lisner:FragmentCallBack?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.buttom_sheet_fragment,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        imv_close.setOnClickListener {
            dialog.cancel()
        }

        radio_group.setOnCheckedChangeListener { group, checkedId ->

            val radio_group = group.findViewById<RadioButton>(checkedId)
            println("clcked month:::${radio_group.text}")
            lisner?.callBackListner(radio_group.text.toString())
            dialog.cancel()
        }
    }

    override fun onStart() {
        super.onStart()
         lisner= (activity as MainActivity).listner
    }
}