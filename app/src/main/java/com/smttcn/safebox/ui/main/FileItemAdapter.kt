package com.smttcn.safebox.ui.main

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.recyclerview_item.view.*

class FileItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var fileItems = emptyList<FileDirItem>() // Cached copy of FileDirItem
    private val currentContext = context
    private var currSelectedItemIndex = -1
    private var prevSelectedItemIndex = -1

    var onItemClick: ((View, FileDirItem, Int, Int) -> Unit)? = null // to point to the onItemClick method in MainActivity

    inner class FileItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener{

                prevSelectedItemIndex = currSelectedItemIndex
                currSelectedItemIndex = absoluteAdapterPosition

                if (prevSelectedItemIndex >= 0 && prevSelectedItemIndex != currSelectedItemIndex) {
                    fileItems[prevSelectedItemIndex].isSelected = false
                    notifyItemChanged(prevSelectedItemIndex)
                }

                fileItems[currSelectedItemIndex].isSelected = !fileItems[currSelectedItemIndex].isSelected
                notifyItemChanged(currSelectedItemIndex)

                // invoke the onItemClick method in MainActivity
                onItemClick?.invoke(it, fileItems[currSelectedItemIndex], prevSelectedItemIndex, currSelectedItemIndex)

            }
        }

        fun bindItem(item: FileDirItem) {
            if (item.thumbnailDrawable != null)
                itemView.item_thumbnail.setImageDrawable(item.thumbnailDrawable)
            else
                itemView.item_thumbnail.setImageResource(R.drawable.ic_file_gray_24dp)

            itemView.item_name.text = item.filename

            if (item.isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundSelected))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundNormal))
            }
        }


    }

    override fun getItemCount(): Int {
        return fileItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        var holder = FileItemViewHolder(inflater.inflate(R.layout.recyclerview_item, parent, false))
        return holder
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        val current = fileItems.get(position)
        holder.bindItem(current)
    }

    fun getSelectedItem(): FileDirItem? {

        for (item in fileItems) {
            if (item.isSelected)
                return item
        }
        return null
    }

    fun getSelectedItemCount(): Int {
        var count = 0

        for (item in fileItems) {
            if (item.isSelected) count++
        }

        return count
    }

    internal fun setFileItems(items: List<FileDirItem>) {
        this.fileItems = items
        notifyDataSetChanged()
    }

}