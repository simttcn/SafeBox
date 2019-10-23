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

import android.R.attr
import androidx.annotation.CheckResult
import com.afollestad.date.dayOfMonth
import com.afollestad.viewpagerdots.DotsIndicator
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.WhichButton.POSITIVE
import com.smttcn.materialdialogs.actions.setActionButtonEnabled
import com.smttcn.materialdialogs.callbacks.onDismiss
import com.smttcn.materialdialogs.customview.customView
import com.smttcn.materialdialogs.datetime.internal.DateTimePickerAdapter
import com.smttcn.materialdialogs.datetime.internal.TimeChangeListener
import com.smttcn.materialdialogs.datetime.utils.getDatePicker
import com.smttcn.materialdialogs.datetime.utils.getPageIndicator
import com.smttcn.materialdialogs.datetime.utils.getPager
import com.smttcn.materialdialogs.datetime.utils.getTimePicker
import com.smttcn.materialdialogs.datetime.utils.hour
import com.smttcn.materialdialogs.datetime.utils.isFutureTime
import com.smttcn.materialdialogs.datetime.utils.minute
import com.smttcn.materialdialogs.datetime.utils.toCalendar
import com.smttcn.materialdialogs.utils.MDUtil.isLandscape
import com.smttcn.materialdialogs.utils.MDUtil.resolveColor
import com.smttcn.safebox.R
import java.util.Calendar

typealias DateTimeCallback = ((dialog: MaterialDialog, datetime: Calendar) -> Unit)?

/**
 * Makes the dialog a date and time picker.
 */
fun MaterialDialog.dateTimePicker(
  minDateTime: Calendar? = null,
  currentDateTime: Calendar? = null,
  requireFutureDateTime: Boolean = false,
  show24HoursView: Boolean = false,
  autoFlipToTime: Boolean = true,
  dateTimeCallback: DateTimeCallback = null
): MaterialDialog {
  customView(
      R.layout.md_datetime_picker_pager,
      noVerticalPadding = true,
      dialogWrapContent = windowContext.isLandscape()
  )

  val viewPager = getPager().apply {
    adapter = DateTimePickerAdapter()
  }
  getPageIndicator()?.run {
    attachViewPager(viewPager)
    setDotTint(resolveColor(windowContext, attr = attr.textColorPrimary))
  }

  getDatePicker().apply {
    minDateTime?.let { setMinDate(it) }
    currentDateTime?.let { setDate(it) }
    addOnDateChanged { previous, date ->
      val futureTime = isFutureTime(getDatePicker(), getTimePicker())
      setActionButtonEnabled(
          POSITIVE, !requireFutureDateTime || futureTime
      )
      if (autoFlipToTime && didDateChange(previous, date)) {
        getPager().currentItem = 1
      }
    }
  }

  getTimePicker().apply {
    setIs24HourView(show24HoursView)
    hour(currentDateTime?.get(Calendar.HOUR_OF_DAY) ?: 12)
    minute(currentDateTime?.get(Calendar.MINUTE) ?: 0)

    setOnTimeChangedListener { _, _, _ ->
      val isFutureTime = isFutureTime(getDatePicker(), this)
      setActionButtonEnabled(
          POSITIVE,
          !requireFutureDateTime || isFutureTime
      )
    }
  }

  positiveButton(android.R.string.ok) {
    val selectedTime = toCalendar(getDatePicker(), getTimePicker())
    dateTimeCallback?.invoke(it, selectedTime)
  }
  negativeButton(android.R.string.cancel)

  if (requireFutureDateTime) {
    val changeListener = TimeChangeListener(windowContext, getTimePicker()) {
      val isFutureTime = isFutureTime(getDatePicker(), it)
      setActionButtonEnabled(
          POSITIVE,
          !requireFutureDateTime || isFutureTime
      )
    }
    onDismiss { changeListener.dispose() }
  }

  return this
}

private fun didDateChange(
  from: Calendar?,
  to: Calendar
): Boolean {
  if (from == null) return false
  return from.dayOfMonth != to.dayOfMonth
}

/**
 * Gets the currently selected date and time from a date/time picker dialog.
 */
@CheckResult fun MaterialDialog.selectedDateTime(): Calendar {
  return toCalendar(getDatePicker(), getTimePicker())
}
