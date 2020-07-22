package com.smttcn.safebox.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.pdfview.PDFView
import com.smttcn.commons.activities.BaseActivity
import com.smttcn.commons.extensions.getFilenameFromPath
import com.smttcn.commons.helpers.INTENT_VIEW_FILE_PATH
import com.smttcn.commons.manager.FileManager
import com.smttcn.safebox.R
import kotlinx.android.synthetic.main.activity_view_pdf.*
import com.smttcn.commons.extensions.lowerCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PdfViewActivity : BaseActivity() {

    lateinit var myContext: Context
    var filePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        initActivity()
        initActivityUI()

    }


    private fun initActivity() {
        myContext = this
        filePath = intent.getStringExtra(INTENT_VIEW_FILE_PATH)
        setContentView(R.layout.activity_view_pdf)
    }


    private fun initActivityUI() {

        if (filePath.length > 0) {

            var viewer = pdfView.fromFile(filePath)

            supportActionBar?.title = filePath.getFilenameFromPath()
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            viewer.show()
            showProgressBar(false)
        } else {
            finish()
        }

    }


    override fun onDestroy() {
        FileManager.emptyCacheFolder()
        super.onDestroy()
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun showProgressBar(show: Boolean) {
        if (show) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            pdfViewActivityProgressBarContainer.visibility = View.VISIBLE
            pdfView.visibility = View.GONE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            pdfViewActivityProgressBarContainer.visibility = View.GONE
            pdfView.visibility = View.VISIBLE
        }
    }


}