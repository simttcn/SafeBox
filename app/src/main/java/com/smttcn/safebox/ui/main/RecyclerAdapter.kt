package com.smttcn.safebox.ui.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.smttcn.commons.models.FileDirItem
import timber.log.Timber
import com.smttcn.commons.extensions.inflate
import com.smttcn.safebox.R
import com.smttcn.safebox.database.StoreItem
import kotlinx.android.synthetic.main.recyclerview_item.view.*

class RecyclerAdapter(private val items: ArrayList<StoreItem>) : RecyclerView.Adapter<RecyclerAdapter.ItemHolder>() {


    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerAdapter.ItemHolder, position: Int) {
        val item = items[position]
        holder.bindItem(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ItemHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item, false)
        return ItemHolder(inflatedView)
    }

    class ItemHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        companion object {
            private val ITEM_KEY = "FILEDIRITEM"
        }

        private var view: View = v
        private var item: StoreItem? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            Timber.d("RecyclerView CLICK!")

//            val context = itemView.context
//            val showPhotoIntent = Intent(context, PhotoActivity::class.java)
//            showPhotoIntent.putExtra(PHOTO_KEY, photo)
//            context.startActivity(showPhotoIntent)
        }

        fun bindItem(item: StoreItem) {
            this.item = item
            //Picasso.with(view.context).load(photo.url).into(view.itemImage)
            //view.itemDate.text = photo.humanDate
            //view.itemDescription.text = photo.explanation
            view.item_name.text = item.fileName
        }
    }

}