package com.snail.swipemenulistview


import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class SwipeMenuItem(private val mContext: Context) {
    var id: Int = 0
    var title: String? = null
    var icon: Drawable? = null
    var background: Drawable? = null
    var titleColor: Int = 0
    var titleSize: Int = 0
    var width: Int = 0

    fun setTitle(resId: Int) {
        title = mContext.getString(resId)
    }

    fun setIcon(resId: Int) {
        this.icon = ContextCompat.getDrawable(mContext, resId)
    }

    fun setBackground(resId: Int) {
        this.background = ContextCompat.getDrawable(mContext, resId)
    }
}
