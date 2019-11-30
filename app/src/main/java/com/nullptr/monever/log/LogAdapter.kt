package com.nullptr.monever.log

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.nullptr.monever.R
import java.text.SimpleDateFormat

const val datePattern = "dd/MM HH:mm"

class LogAdapter(private val context: Context, var logs: List<Log>) : BaseAdapter() {

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
        val textView = rowView.findViewById(R.id.logText) as TextView
        val happyCountTextView = rowView.findViewById(R.id.happyRating) as TextView
        val logDateTextView = rowView.findViewById(R.id.logDate) as TextView
        textView.text = logs[i].text
        happyCountTextView.text = logs[i].happyRating.toString()

        val df = SimpleDateFormat(datePattern)
        val logCreationDate = df.format(logs[i].creationDate)
        logDateTextView.text = logCreationDate
        return rowView
    }
}
