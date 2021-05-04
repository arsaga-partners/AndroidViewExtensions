package jp.co.arsaga.extensions.view

import android.view.View
import android.widget.ScrollView

fun ScrollView.smoothScrollToChildViewTop(childView: View?) {
    childView?.let {
        smoothScrollTo(it.left, it.top)
    }
}