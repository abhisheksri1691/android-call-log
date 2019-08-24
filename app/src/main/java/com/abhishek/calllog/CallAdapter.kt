package com.abhishek.calllog

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.abhishek.calllog.model.CallDetail
import kotlinx.android.synthetic.main.adapter_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class CallAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var  callist = mutableListOf<CallDetail>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): RecyclerView.ViewHolder {

        val view  = LinearLayout.inflate(viewGroup.context,R.layout.adapter_layout,null)
        return  CallViewHolder(view)
    }

    override fun getItemCount(): Int {
       return callist.size
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, p1: Int) {

        (viewHolder as CallViewHolder).bind(callist.get(p1))
    }

    fun addData(list: MutableList<CallDetail>) {

        callist = list
        notifyDataSetChanged()
    }


    inner  class  CallViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView)
    {
        fun  bind(get: CallDetail) {
            itemView.tv_number.text = get.mobileNo
            itemView.tv_duration.text = get.timeDuration + " min"

            if (get.time != null) {
                val date = Date(get.time!!.toLong())
                val smp = SimpleDateFormat("dd MMM yy hh:mm aa")
                itemView.tv_date.text = smp.format(date)

            }
        }
    }

}