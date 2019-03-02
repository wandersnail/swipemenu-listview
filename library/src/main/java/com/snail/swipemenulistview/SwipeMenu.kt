package com.snail.swipemenulistview

import android.content.Context
import java.util.*

class SwipeMenu(val context: Context) {
    private val mItems: MutableList<SwipeMenuItem>
    var viewType: Int = 0

    val menuItems: List<SwipeMenuItem>
        get() = mItems

    init {
        mItems = ArrayList()
    }

    fun addMenuItem(item: SwipeMenuItem) {
        mItems.add(item)
    }

    fun removeMenuItem(item: SwipeMenuItem) {
        mItems.remove(item)
    }

    fun getMenuItem(index: Int): SwipeMenuItem {
        return mItems[index]
    }
}
