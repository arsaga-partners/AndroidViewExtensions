package jp.co.arsaga.extensions.view.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import jp.co.arsaga.extensions.view.toggle

interface HasProgressBarActivity {

    fun getProgressBar(): ProgressBar?

    fun progressBarToggle(isVisible: Boolean?) {
        getProgressBar()?.toggle(isVisible == true)
    }

    abstract class LoadingHandler :  Application.ActivityLifecycleCallbacks {

        protected abstract val apiConnectionCounter: LiveData<Int>

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (
                activity is HasProgressBarActivity
                && activity is FragmentActivity
            ) apiConnectionCounter.observe(activity) {
                it ?: return@observe
                activity.getProgressBar()?.toggle(it != 0)
            }
        }
    }
}