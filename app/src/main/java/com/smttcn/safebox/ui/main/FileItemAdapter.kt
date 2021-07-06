package com.smttcn.safebox.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.smttcn.commons.extensions.formatSize
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.recyclerview_item.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class FileItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder>(), Filterable {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var fileItems: MutableList<FileDirItem> // Cached copy of FileDirItem
    private lateinit var fileItemsFiltered: MutableList<FileDirItem> // Cached copy of filtered FileDirItem
    private val currentContext = context
    private var currSelectedItemIndex = -1
    private var prevSelectedItemIndex = -1

    var onItemClick: ((View, FileDirItem, Int, Int) -> Unit)? = null // to point to the onItemClick method in MainActivity
    var onItemPopupMenuClick: ((View, FileDirItem, Int) -> Unit)? = null // to point to the onItemClick method in MainActivity

    inner class FileItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            itemView.setOnClickListener{

                prevSelectedItemIndex = currSelectedItemIndex
                currSelectedItemIndex = absoluteAdapterPosition

                /*
                // disable item selection because we perform action via context menu now
                if (prevSelectedItemIndex >= 0 && prevSelectedItemIndex != currSelectedItemIndex) {
                    fileItems[prevSelectedItemIndex].isSelected = false
                }
                fileItems[currSelectedItemIndex].isSelected = !fileItems[currSelectedItemIndex].isSelected
                */

                //notifyItemChanged(prevSelectedItemIndex)
                //notifyItemChanged(currSelectedItemIndex)

                // invoke the onItemClick method in MainActivity
                onItemClick?.invoke(it, fileItemsFiltered[currSelectedItemIndex], prevSelectedItemIndex, currSelectedItemIndex)

            }
        }

        fun bindItem(item: FileDirItem) {
            var thumbnail = item.getThumbnailDrawable()
            if (thumbnail != null)
                itemView.item_thumbnail.setImageDrawable(thumbnail)
            else
                itemView.item_thumbnail.setImageResource(R.drawable.ic_file)

            itemView.item_name.text = item.getOriginalFilename() //item.filename

            itemView.item_size.text = item.getFileSize().formatSize() //item.size

//            if (item.isSelected) {
//                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundSelected))
//            } else if (item.isOptionMenuActive) {
//                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundOptionMenuActive))
//            }else {
//                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundNormal))
//            }
        }

    }

    override fun getItemCount(): Int {
        return fileItemsFiltered.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        var holder = FileItemViewHolder(inflater.inflate(R.layout.recyclerview_item, parent, false))
        return holder
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        val currentItem = fileItemsFiltered.get(position)

        holder.itemView.popup_menu.setOnClickListener {

            onItemPopupMenuClick?.invoke(holder.itemView, currentItem, position)

        }
        holder.bindItem(currentItem)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                if (charString.isEmpty()) {

                    fileItemsFiltered = fileItems

                } else {

                    val filteredList = ArrayList<FileDirItem>()
                    fileItems
                        .filter {
                            (it.getOriginalFilename().toLowerCase(Locale.getDefault()).contains(charString.toLowerCase(Locale.getDefault())))
                        }
                        .forEach { filteredList.add(it) }
                    fileItemsFiltered = filteredList


                }
                return FilterResults().apply { values = fileItemsFiltered }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {

                @Suppress("UNCHECKED_CAST")
                fileItemsFiltered = if (results?.values == null)
                    ArrayList()
                else
                    results.values as MutableList<FileDirItem>

                notifyDataSetChanged()
            }
        }
    }

    fun getSelectedItem(): FileDirItem? {

        for (item in fileItemsFiltered) {
            if (item.isSelected)
                return item
        }
        return null
    }

    fun getSelectedItemCount(): Int {
        var count = 0

        for (item in fileItemsFiltered) {
            if (item.isSelected) count++
        }

        return count
    }

    internal fun setFileItems(items: MutableList<FileDirItem>) {
        this.fileItems = items
        this.fileItemsFiltered = items
        notifyDataSetChanged()
    }

    internal fun deleteFileItem(position: Int) {

        var item = fileItemsFiltered[position]

        // ask for the decrypting password for this file
        var dialog = MaterialDialog(currentContext).show {
            icon(R.drawable.ic_warning)
            title(R.string.dlg_title_delete_item)
            cornerRadius(5.0f)
            customView(R.layout.delete_file_view)
            positiveButton(R.string.btn_delete) {

                if (FileManager.deleteFile(File(item.path))) {
                    fileItemsFiltered.remove(item)
                    fileItems.remove(item)
                    notifyDataSetChanged()
                }

            }
            negativeButton(R.string.btn_cancel)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
        }

        val filename: TextView = dialog.getCustomView().findViewById(R.id.filename)
        filename.text = item.getOriginalFilename()

    }

}