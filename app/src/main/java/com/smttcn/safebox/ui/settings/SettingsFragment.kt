package com.smttcn.safebox.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.smttcn.safebox.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        val textView: TextView = root.findViewById(R.id.text_settings)
        textView.text = getString(R.string.navigation_settings)

        val changePasswordTextView: TextView = root.findViewById(R.id.text_change_password)
        changePasswordTextView.text = getString(R.string.change_password)

        return root
    }
}