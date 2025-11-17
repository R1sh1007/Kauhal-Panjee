package com.kaushalpanjee.core.util.optimize


import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

fun Fragment.safeNavigate(direction: NavDirections) {
    if (!isAdded) return
    val navController = findNavController()

    val current = navController.currentDestination?.id
    val expected = direction.actionId

    if (current != expected) {
        navController.navigate(direction)
    }
}

fun Fragment.safePop() {
    if (isAdded) {
        findNavController().popBackStack()
    }
}
