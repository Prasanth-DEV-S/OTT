package com.dealshot.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.navigation.NavController

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun NavController.safePopBackStack(
    context: Context,
    homeRoutePrefix: String = "home"
) {
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        val currentRoute = currentBackStackEntry?.destination?.route.orEmpty()
        if (!currentRoute.startsWith(homeRoutePrefix) && !currentRoute.startsWith("start")) {
            navigate(homeRoutePrefix) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            context.findActivity()?.finish()
        }
    }
}
