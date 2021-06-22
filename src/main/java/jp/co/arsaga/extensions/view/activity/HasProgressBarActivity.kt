package jp.co.arsaga.extensions.view.activity

import android.widget.ProgressBar
import jp.co.arsaga.extensions.view.toggle

interface HasProgressBarActivity {

    fun getProgressBar(): ProgressBar?

    fun progressBarToggle(isVisible: Boolean?) {
        getProgressBar()?.toggle(isVisible == true)
    }
}