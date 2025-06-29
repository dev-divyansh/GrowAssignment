package com.divyansh.growassignment.presentation.watchlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.TextView
import com.divyansh.growassignment.R
import com.divyansh.growassignment.data.models.WatchlistEntity

class WatchlistDialogAdapter(
    private val context: Context,
    private var watchlists: List<WatchlistEntity>,
    private var selectedPosition: Int = -1
) : BaseAdapter() {

    fun updateData(newWatchlists: List<WatchlistEntity>) {
        watchlists = newWatchlists
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    fun getSelectedWatchlistId(): Long? {
        return if (selectedPosition >= 0 && selectedPosition < watchlists.size) {
            watchlists[selectedPosition].id
        } else null
    }

    override fun getCount(): Int = watchlists.size

    override fun getItem(position: Int): WatchlistEntity = watchlists[position]

    override fun getItemId(position: Int): Long = watchlists[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_watchlist_dialog, parent, false)

        val watchlist = getItem(position)
        val tvWatchlistName = view.findViewById<TextView>(R.id.tvWatchlistName)
        val rbSelected = view.findViewById<RadioButton>(R.id.rbSelected)

        tvWatchlistName.text = watchlist.name
        rbSelected.isChecked = position == selectedPosition

        view.setOnClickListener {
            setSelectedPosition(position)
        }

        return view
    }
} 