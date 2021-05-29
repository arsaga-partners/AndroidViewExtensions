package jp.co.arsaga.extensions.view

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController


fun FragmentActivity.setTitleNavHostFragment(navHostFragmentId: Int, toolbar: Toolbar) {
    findNavControllerByNavHostFragmentId(
        supportFragmentManager,
        navHostFragmentId
    ).let { navController ->
        toolbar.apply {
            setupWithNavController(navController)
            setNavigationOnClickListener {
                HasReturnInitiativeFragment
                    .returnHandling(getCurrentDisplayFragment(navHostFragmentId)) {
                        navController.navigateUp()
                    }
            }
        }
    }
}

fun FragmentActivity.safeShowDialogFragment(dialogFragment: DialogFragment) {
    if (supportFragmentManager.findFragmentByTag(dialogFragment.javaClass.name) is DialogFragment) return
    dialogFragment.show(supportFragmentManager, dialogFragment.javaClass.name)
}

fun FragmentActivity.dismissAllDialogFragment() {
    getAllDialogFragment().forEach { it.dismissAllowingStateLoss() }
}

fun FragmentActivity.getAllDialogFragment(): List<DialogFragment> = supportFragmentManager
    .fragments
    .filterIsInstance<DialogFragment>()

fun FragmentActivity.setDestinationChangeListener(
    navHostFragmentId: Int,
    destinationChangedListenerList: List<NavController.OnDestinationChangedListener>
) {
    findNavControllerByNavHostFragmentId(supportFragmentManager, navHostFragmentId).let {
        destinationChangedListenerList.fold(it) { acc, onDestinationChangedListener ->
            acc.apply { addOnDestinationChangedListener(onDestinationChangedListener) }
        }
    }
}

fun FragmentActivity.getCurrentDisplayFragment(
    navHostFragmentId: Int
): Fragment? = getCurrentDisplayFragment(supportFragmentManager, navHostFragmentId)

fun Fragment.getCurrentDisplayFragment(
    navHostFragmentId: Int
): Fragment? = getCurrentDisplayFragment(childFragmentManager, navHostFragmentId)

private fun getCurrentDisplayFragment(
    fragmentManager: FragmentManager,
    navHostFragmentId: Int
): Fragment? = fragmentManager
    .findFragmentById(navHostFragmentId)
    ?.childFragmentManager
    ?.fragments
    ?.getOrNull(0)

fun findNavControllerByNavHostFragmentId(
    fragmentManager: FragmentManager,
    navHostFragmentId: Int
): NavController = (fragmentManager.findFragmentById(navHostFragmentId) as NavHostFragment)
    .navController

fun Activity.getRootView(): View = findViewById(android.R.id.content)