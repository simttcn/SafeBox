package com.smttcn.safebox.ui.main

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.smttcn.commons.Manager.FileManager
import com.smttcn.commons.extensions.showMessageDialog
import com.smttcn.commons.models.FileDirItem
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.recyclerview_item.view.*
import java.io.File

class FileItemAdapter internal constructor(context: Context) : RecyclerView.Adapter<FileItemAdapter.FileItemViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var fileItems: MutableList<FileDirItem> // Cached copy of FileDirItem
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

                notifyItemChanged(prevSelectedItemIndex)
                notifyItemChanged(currSelectedItemIndex)

                // invoke the onItemClick method in MainActivity
                onItemClick?.invoke(it, fileItems[currSelectedItemIndex], prevSelectedItemIndex, currSelectedItemIndex)

            }
        }

        fun bindItem(item: FileDirItem) {
            var thumbnail = item.getThumbnailDrawable()
            if (thumbnail != null)
                itemView.item_thumbnail.setImageDrawable(thumbnail)
            else
                itemView.item_thumbnail.setImageResource(R.drawable.ic_file_gray_24dp)

            itemView.item_name.text = item.getOriginalFilename() //item.filename

            if (item.isSelected) {
                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundSelected))
            } else if (item.isOptionMenuActive) {
                itemView.setBackgroundColor(ContextCompat.getColor(currentContext, R.color.colorItemBackgroundOptionMenuActive))
            }else {
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
        val currentItem = fileItems.get(position)

        holder.itemView.popup_menu.setOnClickListener {

            onItemPopupMenuClick?.invoke(holder.itemView, currentItem, position)

        }
        holder.bindItem(currentItem)
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

    internal fun setFileItems(items: MutableList<FileDirItem>) {
        this.fileItems = items
        notifyDataSetChanged()
    }

    internal fun deleteFileItem(position: Int) {

        var item = fileItems[position]

        // ask for the decrypting password for this file
        MaterialDialog(currentContext).show {
            title(R.string.dlg_title_delete_item)
            message(null, currentContext.getString(R.string.dlg_msg_delete_item) + "\n\n" + item.filename)
            positiveButton(R.string.btn_delete) {

                if (FileManager.deleteFile(File(item.path))) {
                    fileItems.removeAt(position)
                    notifyDataSetChanged()
                }

            }
            negativeButton(R.string.btn_cancel)
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
        }

    }

}