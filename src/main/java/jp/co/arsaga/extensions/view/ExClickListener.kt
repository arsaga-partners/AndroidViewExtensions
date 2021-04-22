package jp.co.arsaga.extensions.view

import android.animation.AnimatorInflater
import android.app.AlertDialog
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.databinding.BindingAdapter
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.concurrent.atomic.AtomicBoolean

@BindingAdapter("binding_onOnceClick", "binding_clickIntervalMs", requireAll = false)
fun onOnceClick(view: View, onOnceClick: View.OnClickListener?, clickIntervalMs: Long = 1000L) {
    onOnceClick ?: return
    view.requestFocus()
    view.setOnClickListener(
        OnOnceClickListener(
            onOnceClick,
            clickIntervalMs
        )
    )
}

@BindingAdapter("binding_developLink")
fun developLink(view: View, developText: String?) {
    val alertMessage = (developText ?: "この機能は現在開発中です。\n 公開まで少々お待ちください。")
    onOnceClick(view, {
        AlertDialog.Builder(view.context)
            .setMessage(alertMessage)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }.show()
    })
}

@BindingAdapter("binding_popUpTo", "binding_navigateBackSideEffect", requireAll = false)
fun onTransitionBack(view: View, popUpTo: NavDirections?, navigateBackSideEffect: (() -> Unit)?) {
    onOnceClick(view, {
        navigateBackSideEffect?.let { it() }
        it.findNavController().run {
            popUpTo?.run {
                navigate(popUpTo)
            } ?: popBackStack()
        }
    })
}

fun findNavController(view: View) = view.findNavController()

@BindingAdapter(
    "binding_navigateArgsAction",
    "binding_navigateSideEffect",
    "binding_navigateAnimation",
    "binding_navigateExtrasSeed",
    "binding_navigateExtrasActivity",
    "binding_navController",
    requireAll = false
)
fun onTransitionClick(
    view: View,
    navigateArgsAction: NavDirections? = null,
    navigateSideEffect: View.OnClickListener? = null,
    navigateAnimation: NavigateAnimationType? = null,
    navigatorExtrasSeed: Map<String, View>? = null,
    navigatorExtrasActivity: ActivityOptionsCompat? = null,
    navController: NavController? = null
) {
    navigateArgsAction ?: return
    setTapReaction(view)
    onOnceClick(view, {
        navigateSideEffect?.onClick(it)
        (navController ?: it.findNavController()).run {
            navigatorExtrasSeed?.let {
                FragmentNavigator.Extras.Builder().apply {
                    it.forEach { sharedView -> addSharedElement(sharedView.value, sharedView.key) }
                }.build().let { sharedExtras ->
                    navigate(navigateArgsAction, sharedExtras)
                }
            } ?: navigatorExtrasActivity?.let {
                navigate(navigateArgsAction, ActivityNavigatorExtras(it))
            } ?: navigate(
                navigateArgsAction,
                animateNavOptionsFactory(
                    navigateAnimation
                )
            )
        }
    })
}

fun setTapReaction(view: View) {
    view.viewTreeObserver.addOnGlobalLayoutListener {
        if (view.rootView.width != view.run { width - marginStart - marginEnd }) {
            view.stateListAnimator = AnimatorInflater
                .loadStateListAnimator(view.context, R.animator.button_push_animation)
        }
    }
    when {
        view is CardView -> view.radius
        view.background is GradientDrawable -> (view.background as GradientDrawable).run {
            when (shape) {
                GradientDrawable.RECTANGLE -> cornerRadius
                else -> null
            }
        }
        else -> 0F
    }.let { radius ->
        radius ?: return@let OvalShape()
        val roundRectShapeParameterSize = 8
        RoundRectShape(FloatArray(roundRectShapeParameterSize).apply {
            repeat(size) {
                set(it, radius)
            }
        }, null, null)
    }.let {
        view.foreground = RippleDrawable(
            view.context.getColorStateList(R.color.selector_button_push),
            null,
            ShapeDrawable(it)
        )
    }
}

fun animateNavOptionsFactory() = animateNavOptionsFactory(null)
fun animateNavOptionsFactory(navigateAnimation: NavigateAnimationType?): NavOptions = navOptions {
    anim {
        (navigateAnimation ?: NavigateAnimationType.SLIDE).let {
            enter = it.enterAnimationId
            exit = it.exitAnimationId
            popEnter = it.popEnterAnimationId
            popExit = it.popExitAnimationId
        }
    }
}

@BindingAdapter("binding_toggleBottomSheet")
fun View.toggleBottomSheetState(bottomSheet: ViewGroup) {
    this.setOnClickListener {
        BottomSheetBehavior.from(bottomSheet).run {
            when (state) {
                BottomSheetBehavior.STATE_COLLAPSED -> state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.STATE_EXPANDED -> state = BottomSheetBehavior.STATE_COLLAPSED
                else -> {
                }
            }
        }
    }
}

private class OnOnceClickListener(
    private val clickListener: View.OnClickListener,
    private val intervalMs: Long = 1000L
) : View.OnClickListener {
    private val canClick = AtomicBoolean(true)

    override fun onClick(view: View?) {
        if (canClick.getAndSet(false)) {
            view?.run {
                postDelayed({ canClick.set(true) }, intervalMs)
                clickListener.onClick(view)
            }
        }
    }
}