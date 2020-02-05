package com.example.capaproject

import android.view.MotionEvent
import androidx.core.view.MotionEventCompat
import android.view.View.OnLongClickListener
import android.appwidget.AppWidgetHostView
import android.content.Context
import android.util.Log


class WidgetView : AppWidgetHostView {

    private var longClick: OnLongClickListener? = null
    private var down: Long = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, animationIn: Int, animationOut: Int) : super(
        context,
        animationIn,
        animationOut
    ) {
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        this.longClick = l
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (MotionEventCompat.getActionMasked(ev)) {
            MotionEvent.ACTION_DOWN -> down = System.currentTimeMillis()
            MotionEvent.ACTION_UP -> {
                val upVal = System.currentTimeMillis() - down > 300L
                if (upVal) {
                    longClick!!.onLongClick(this@WidgetView)
                    removeView(this@WidgetView)
                    Log.d("TAG","long click")
                    return true
                }
            }
        }

        return false
    }
}