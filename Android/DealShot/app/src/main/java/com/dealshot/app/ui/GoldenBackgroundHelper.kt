package com.dealshot.app.ui

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout

object GoldenBackgroundHelper {

    fun apply(activity: Activity) {

        val root = activity.findViewById<ViewGroup>(
            android.R.id.content
        ) ?: return

        if (root.childCount == 0) return

        val existingContent = root.getChildAt(0)

        if (existingContent is FrameLayout &&
            existingContent.tag == "golden_background_root"
        ) return

        root.removeView(existingContent)

        val wrapper = FrameLayout(activity).apply {
            tag = "golden_background_root"
            setBackgroundColor(android.graphics.Color.rgb(5, 5, 9))
        }

        val background = AnimatedGoldenBackground(activity)

        wrapper.addView(
            background,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        if (existingContent.parent is ViewGroup) {
            (existingContent.parent as ViewGroup)
                .removeView(existingContent)
        }

        wrapper.addView(
            existingContent,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            wrapper,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }
}
