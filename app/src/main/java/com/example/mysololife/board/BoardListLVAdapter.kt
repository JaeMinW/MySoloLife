package com.example.mysololife.board

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mysololife.R
import com.example.mysololife.utils.FBAuth

class BoardListLVAdapter(val boardList : MutableList<BoardModel>) : BaseAdapter() {

    override fun getCount(): Int {
        return boardList.size
    }

    override fun getItem(position: Int): Any {
        return boardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        // Reuse existing view if possible, otherwise inflate a new one
        if (convertView == null) {
            view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.board_list_item, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.itemView),
                view.findViewById(R.id.titleArea),
                view.findViewById(R.id.timeArea),
                view.findViewById(R.id.contentArea)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        // Get the item at the current position
        val item = boardList[position]

        // Initialize the view elements
        viewHolder.title.text = item.title
        viewHolder.content.text = item.content
        viewHolder.time.text = item.time

        // Set the background color based on condition
        if (item.uid == FBAuth.getUid()) {
            viewHolder.itemLinearLayoutView.setBackgroundColor(Color.parseColor("#ffa500"))
        } else {
            viewHolder.itemLinearLayoutView.setBackgroundColor(Color.TRANSPARENT) // Reset background color if needed
        }

        return view
    }

    // ViewHolder class to hold the view references
    private class ViewHolder(
        val itemLinearLayoutView: LinearLayout,
        val title: TextView,
        val time: TextView,
        val content: TextView
    )
}
//        ListView 중첩출력현상
//        뷰 재활용하면서 발생하는 버그(background color 설정관련)
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//
//        var view = convertView

//        if(convertView == null){
//
//            view = LayoutInflater.from(parent?.context).inflate(R.layout.board_list_item, parent, false)
//
//        }
//
//        val itemLinearLayoutView = view?.findViewById<LinearLayout>(R.id.itemView)
//        val title = view?.findViewById<TextView>(R.id.titleArea)
//        val time = view?.findViewById<TextView>(R.id.timeArea)
//        val content = view?.findViewById<TextView>(R.id.contentArea)
//
//        if(boardList[position].uid.equals(FBAuth.getUid())){
//            itemLinearLayoutView?.setBackgroundColor(Color.parseColor("#ffa500"))
//        }
//
//        title!!.text = boardList[position].title
//        content!!.text = boardList[position].content
//        time!!.text = boardList[position].time
//
//        return view!!
//    }
//}