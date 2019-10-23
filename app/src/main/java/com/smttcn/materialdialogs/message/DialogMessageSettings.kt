/**
 * Designed and developed by Aidan Follestad (@smttcn)
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
package com.smttcn.materialdialogs.message

import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.annotation.StringRes
import com.smttcn.materialdialogs.MaterialDialog
import com.smttcn.materialdialogs.internal.message.LinkTransformationMethod
import com.smttcn.materialdialogs.utils.MDUtil.resolveString

/** @author Aidan Follestad (@smttcn) */
class DialogMessageSettings internal constructor(
  private val dialog: MaterialDialog,
  @Suppress("MemberVisibilityCanBePrivate")
  val messageTextView: TextView
) {
  private var isHtml: Boolean = false

  fun lineSpacing(multiplier: Float): DialogMessageSettings {
    messageTextView.setLineSpacing(0f, multiplier)
    return this
  }

  fun html(onLinkClick: ((link: String) -> Unit)? = null): DialogMessageSettings {
    isHtml = true
    if (onLinkClick != null) {
      messageTextView.transformationMethod = LinkTransformationMethod(onLinkClick)
    }
    messageTextView.movementMethod = LinkMovementMethod.getInstance()
    return this
  }

  internal fun setText(
    @StringRes res: Int?,
    text: CharSequence?
  ) {
    messageTextView.text = text.maybeWrapHtml(isHtml)
        ?: resolveString(dialog, res, html = isHtml)
  }

  private fun CharSequence?.maybeWrapHtml(isHtml: Boolean): CharSequence? {
    if (this == null) return null
    @Suppress("DEPRECATION")
    return if (isHtml) Html.fromHtml(this.toString()) else this
  }
}
