package com.smttcn.safebox.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.smttcn.safebox.databinding.RecyclerviewItemBinding
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.smttcn.commons.extensions.*
import com.smttcn.commons.helpers.ENCRYPTED_FILE_EXT
import com.smttcn.commons.manager.FileManager
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class FileItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder>(), Filterable {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var fileItems: MutableList<FileDirItem> // Cached copy of FileDirItem
    private var fileItemsFiltered: MutableList<FileDirItem> // Cached copy of filtered FileDirItem
    private val currentContext = context
    private var currSelectedItemIndex = -1
    private var prevSelectedItemIndex = -1

    var onItemClick: ((View, FileDirItem, Int, Int) -> Unit)? = null // to point to the onItemClick method in MainActivity
    var onItemPopupMenuClick: ((View, RecyclerviewItemBinding, FileDirItem, Int) -> Unit)? = null // to point to the onItemClick method in MainActivity

    inner class FileItemViewHolder(val binding: RecyclerviewItemBinding) : RecyclerView.ViewHolder(binding.root) {

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
            val thumbnail = item.getThumbnailDrawable()
            if (thumbnail != null)
                binding.itemThumbnail.setImageDrawable(thumbnail)
            else
                binding.itemThumbnail.setImageResource(R.drawable.ic_file)

            binding.itemName.text = item.getOriginalFilename() //item.filename

            binding.itemName.text = item.getFileSize().formatSize() //item.size

//            if (item.isSelected) {
//                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundSelected))
//            } else if (item.isOptionMenuActive) {
//                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundOptionMenuActive))
//            }else {
//                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundNormal))
//            }
        }

    }

    init {
        fileItems = mutableListOf()
        fileItemsFiltered = mutableListOf()
    }

    override fun getItemCount(): Int {
        return fileItemsFiltered.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileItemViewHolder {
        val binding = RecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return FileItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        val currentItem = fileItemsFiltered[position]

        holder.binding.popupMenu.setOnClickListener {

            onItemPopupMenuClick?.invoke(holder.itemView, holder.binding, currentItem, position)

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
                            (it.getOriginalFilename().lowercase(Locale.getDefault()).contains(charString.lowercase(Locale.getDefault())))
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

        var dialog = MaterialDialog(currentContext).show {
            icon(R.drawable.ic_warning)
            title(R.string.dlg_title_delete_item)
            cornerRadius(5.0f)
            customView(R.layout.dialog_delete_file_view)
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

    internal fun renameFileItem(position: Int, newFilename: String) : Boolean {

        var item = fileItemsFiltered[position]

        val fromFilePath = item.path
        val toFilePath = item.path.getPathOnly().appendFilename(newFilename).appendExtension(ENCRYPTED_FILE_EXT)

        return FileManager.renameFile(fromFilePath, toFilePath)

    }

}