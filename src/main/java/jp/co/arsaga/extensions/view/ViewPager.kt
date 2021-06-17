package jp.co.arsaga.extensions.view

import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import kotlin.reflect.KMutableProperty0

@BindingAdapter("binding_currentIndex", "binding_isAnimate", requireAll = false)
fun ViewPager.currentIndex(index: Int, isAnimate: Boolean?) {
    if (index < 0) return
    if (index > this.adapter?.count ?: 0) return
    this.setCurrentItem(index, (isAnimate ?: true))
}

@BindingAdapter("binding_currentIndex", "binding_isAnimate", requireAll = false)
fun ViewPager2.currentIndex(index: Int, isAnimate: Boolean?) {
    if (index < 0) return
    if (index > this.adapter?.itemCount ?: 0) return
    this.setCurrentItem(index, (isAnimate ?: true))
}

@Suppress("UNUSED_PARAMETER")
@BindingAdapter("binding_pageObserve")
fun ViewPager2.pageObserve(collection: Collection<Any>?) {
    adapter?.notifyDataSetChanged()
}

abstract class ViewPager2PageRememberAdapter<T : ViewDataBinding>(
    val viewPager2: ViewPager2,
    val tabPositionInViewModel: KMutableProperty0<Int?>,
    lifecycleOwner: LifecycleOwner?
) : DataBindingAdapter<T>(lifecycleOwner) {
    init {
        rememberUserSwitchedTabPosition()
        apiDrivenSwitchTab()
    }

    private fun rememberUserSwitchedTabPosition() {
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabPositionInViewModel.set(position)
            }
        })
    }

    private fun apiDrivenSwitchTab() {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                restoreTabPosition()
            }
        })
    }

    private fun restoreTabPosition() {
        tabPositionInViewModel.get()?.let {
            viewPager2.setCurrentItem(it, false)
        }
    }
}