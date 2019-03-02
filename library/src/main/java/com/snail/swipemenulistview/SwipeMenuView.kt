package com.snail.swipemenulistview

import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class SwipeMenuView(private val mMenu: SwipeMenu, val listView: SwipeMenuListView) : LinearLayout(mMenu.context), OnClickListener {
    private var mLayout: SwipeMenuLayout? = null
    var onSwipeItemClickListener: OnSwipeItemClickListener? = null
    var position: Int = 0

    init {
        val items = mMenu.menuItems
        for ((id, item) in items.withIndex()) {
            addItem(item, id)
        }
    }

    private fun addItem(item: SwipeMenuItem, id: Int) {
        val params = LinearLayout.LayoutParams(item.width, LinearLayout.LayoutParams.MATCH_PARENT)
        val parent = LinearLayout(context)
        parent.id = id
        parent.gravity = Gravity.CENTER
        parent.orientation = LinearLayout.VERTICAL
        parent.layoutParams = params
        parent.background = item.background
        parent.setOnClickListener(this)
        addView(parent)

        if (item.icon != null) {
            parent.addView(createIcon(item))
        }
        if (!TextUtils.isEmpty(item.title)) {
            parent.addView(createTitle(item))
        }

    }

    private fun createIcon(item: SwipeMenuItem): ImageView {
        val iv = ImageView(context)
        iv.setImageDrawable(item.icon)
        return iv
    }

    private fun createTitle(item: SwipeMenuItem): TextView {
        val tv = TextView(context)
        tv.text = item.title
        tv.gravity = Gravity.CENTER
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, item.titleSize.toFloat())
        tv.setTextColor(item.titleColor)
        return tv
    }

    override fun onClick(v: View) {
        if (onSwipeItemClickListener != null && mLayout!!.isOpen) {
            onSwipeItemClickListener!!.onItemClick(this, mMenu, v.id)
        }
    }

    fun setLayout(mLayout: SwipeMenuLayout) {
        this.mLayout = mLayout
    }

    interface OnSwipeItemClickListener {
        fun onItemClick(view: SwipeMenuView, menu: SwipeMenu, index: Int)
    }
}
