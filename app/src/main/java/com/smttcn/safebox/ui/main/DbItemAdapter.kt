package com.smttcn.safebox.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smttcn.safebox.R
import com.smttcn.safebox.database.DbItem
import kotlinx.android.synthetic.main.recyclerview_item.view.*

class DbItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<DbItemAdapter.DbItemViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var dbItems = emptyList<DbItem>() // Cached copy of DbItems
    var onItemClick: ((DbItem) -> Unit)? = null // to point to the onItemClick method in MainActivity

    override fun getItemCount() = dbItems.size

    inner class DbItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener{
                // invoke the onItemClick method in MainActivity
                onItemClick?.invoke(dbItems[adapterPosition])
            }
        }

        fun bindItem(item: DbItem) {
            itemView.item_name.text = item.fileName
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DbItemViewHolder {
        val inflatedView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return DbItemViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: DbItemViewHolder, position: Int) {
        val current = dbItems[position]
        holder.bindItem(current)
    }

    internal fun setDbItems(items: List<DbItem>) {
        this.dbItems = items
        notifyDataSetChanged()
    }

}