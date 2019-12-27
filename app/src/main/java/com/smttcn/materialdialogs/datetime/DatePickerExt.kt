/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package com.smttcn.materialdialogs.datetime

import android.R.string
import androidx.annotation.CheckResult
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.WhichButton.POSITIVE
import com.smttcn.materialdialogs.actions.setActionButtonEnabled
import com.smttcn.materialdialogs.callbacks.onDismiss
import com.smttcn.materialdialogs.customview.customView
import com.smttcn.materialdialogs.datetime.internal.TimeChangeListener
import com.smttcn.materialdialogs.datetime.utils.getDatePicker
import com.smttcn.materialdialogs.datetime.utils.isFutureDate
import com.smttcn.materialdialogs.utils.MDUtil.isLandscape
import com.smttcn.safebox.R
import java.util.Calendar

/**
 * Makes the dialog a date picker.
 */
fun MaterialDialog.datePicker(
  minDate: Calendar? = null,
  maxDate: Calendar? = null,
  currentDate: Calendar? = null,
  requireFutureDate: Boolean = false,
  dateCallback: DateTimeCallback = null
): MaterialDialog {
  customView(
      R.layout.md_datetime_picker_date,
      noVerticalPadding = true,
      dialogWrapContent = windowContext.isLandscape()
  )

  check(minDate == null || currentDate == null || minDate.before(currentDate)) {
    "Your `minDate` must be less than `currentDate`."
  }
  check(maxDate == null || currentDate == null || maxDate.after(currentDate)) {
    "Your `maxDate` must be bigger than `currentDate`."
  }

  getDatePicker().apply {
    minDate?.let { setMinDate(it) }
    maxDate?.let { setMaxDate(it) }
    currentDate?.let { setDate(it) }

    addOnDateChanged { _: Calendar, _: Calendar ->
      val isFutureDate = getDatePicker().isFutureDate()
      setActionButtonEnabled(
        POSITIVE,
        !requireFutureDate || isFutureDate
      )
    }
  }

  positiveButton(string.ok) {
    getDatePicker().getDate()?.let { datetime ->
      dateCallback?.invoke(it, datetime)
    }
  }
  negativeButton(string.cancel)

  if (requireFutureDate) {
    val changeListener = TimeChangeListener(windowContext, getDatePicker()) {
      val isFutureDate = it.isFutureDate()
      setActionButtonEnabled(
          POSITIVE,
          !requireFutureDate || isFutureDate
      )
    }
    onDismiss { changeListener.dispose() }
  }

  return this
}

/**
 * Gets the currently selected date from a date picker dialog.
 */
@CheckResult fun MaterialDialog.selectedDate(): Calendar {
  return getDatePicker().getDate()!!
}
