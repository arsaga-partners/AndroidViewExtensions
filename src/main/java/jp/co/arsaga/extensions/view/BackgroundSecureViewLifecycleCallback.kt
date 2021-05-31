package jp.co.arsaga.extensions.view

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity


/**
 * アプリがバックグラウンドに待機した時とフォアグラウンドに戻ってきたときに
 * ルートビューの透明度を変更して端末所有者以外に画面の内容を見られないようにするクラス。
 *
 * ApplicationのregisterLifecycleCallbacksでこのクラスを登録することで有効化される。
 */

class BackgroundSecureViewLifecycleCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    private fun getDialogRootView(dialogFragment: DialogFragment): View? = dialogFragment
        .dialog?.window?.decorView?.rootView

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        SecureInvisibleEventReceiver(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        activity.getRootView().run(Companion::onRerunVisible)
        if (activity is FragmentActivity) activity.getAllDialogFragment()
            .forEach { getDialogRootView(it)?.run(BackgroundSecureViewLifecycleCallback::onRerunVisible) }
    }

    private inner class SecureInvisibleEventReceiver(val activity: Activity) : BroadcastReceiver() {
        private val backgroundEventFilter = IntentFilter()
            .apply { addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) }

        override fun onReceive(arg0: Context?, arg1: Intent?) {
            activity.getRootView().run(Companion::onHomeInvisible)
            if (activity is FragmentActivity) activity.getAllDialogFragment()
                .forEach { getDialogRootView(it)?.run(BackgroundSecureViewLifecycleCallback::onHomeInvisible) }
        }

        init {
            activity.registerReceiver(this, backgroundEventFilter)
        }
    }

    companion object {
        fun onHomeInvisible(view: View) {
            view.alpha = 0F
        }

        fun onRerunVisible(view: View) {
            val animateDuration = view.resources
                .getInteger(android.R.integer.config_longAnimTime)
                .toLong()
            view.animate()
                .setDuration(animateDuration)
                .alpha(1F)
        }
    }
}
