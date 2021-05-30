package jp.co.arsaga.extensions.view

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View


/**
 * アプリがバックグラウンドに待機した時とフォアグラウンドに戻ってきたときに
 * ルートビューの透明度を変更して端末所有者以外に画面の内容を見られないようにするクラス。
 *
 * ApplicationのregisterLifecycleCallbacksでこのクラスを登録することで有効化される。
 *
 * DialogFragmentやAlertDialogは別途対応が必要になる。
 */

class BackgroundSecureViewLifecycleCallback : Application.ActivityLifecycleCallbacks {
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        SecureInvisibleEventReceiver(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        activity.getRootView().run(Companion::onRerunVisible)
    }

    private inner class SecureInvisibleEventReceiver(val activity: Activity) : BroadcastReceiver() {
        private val backgroundEventFilter = IntentFilter()
            .apply { addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) }

        override fun onReceive(arg0: Context?, arg1: Intent?) {
            activity.getRootView().run(Companion::onHomeInvisible)
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
