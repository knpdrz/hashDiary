package com.nullptr.monever

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


/**
 * Created by Moon on 20.10.2019.
 */
class LogAdapter(private val context: Context, private var logs: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return logs.size
    }

    override fun getItem(i: Int): Any? {
        return logs[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.log_list_item, viewGroup, false)
        val textView = rowView.findViewById(R.id.log_date) as TextView
        textView.text = logs[i]
        return rowView
    }
}
