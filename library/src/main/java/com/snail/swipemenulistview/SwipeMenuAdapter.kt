package com.snail.swipemenulistview

import android.content.Context
import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.WrapperListAdapter

open class SwipeMenuAdapter(private val mContext: Context, private val mAdapter: ListAdapter) : WrapperListAdapter, SwipeMenuView.OnSwipeItemClickListener {
    private var onMenuItemClickListener: SwipeMenuListView.OnMenuItemClickListener? = null

    override fun getCount(): Int {
        return mAdapter.count
    }

    override fun getItem(position: Int): Any {
        return mAdapter.getItem(position)
    }

    override fun getItemId(position: Int): Long {
        return mAdapter.getItemId(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout: SwipeMenuLayout
        if (convertView == null) {
            val contentView = mAdapter.getView(position, null, parent)
            val menu = SwipeMenu(mContext)
            menu.viewType = getItemViewType(position)
            createMenu(menu)
            val menuView = SwipeMenuView(menu, parent as SwipeMenuListView)
            menuView.onSwipeItemClickListener = this
            layout = SwipeMenuLayout(contentView, menuView, parent.closeInterpolator, parent.openInterpolator)
            layout.position = position
        } else {
            layout = convertView as SwipeMenuLayout
            layout.closeMenu()
            layout.position = position
            mAdapter.getView(position, layout.contentView, parent)
        }
        if (mAdapter is SwipeController) {
            val swipEnable = (mAdapter as SwipeController).getSwipeEnableByPosition(position)
            layout.swipEnable = swipEnable
        }
        return layout
    }

    open fun createMenu(menu: SwipeMenu) {}

    override fun onItemClick(view: SwipeMenuView, menu: SwipeMenu, index: Int) {
        onMenuItemClickListener?.onMenuItemClick(view.position, menu, index)
    }

    fun setOnSwipeItemClickListener(onMenuItemClickListener: SwipeMenuListView.OnMenuItemClickListener?) {
        this.onMenuItemClickListener = onMenuItemClickListener
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        mAdapter.registerDataSetObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        mAdapter.unregisterDataSetObserver(observer)
    }

    override fun areAllItemsEnabled(): Boolean {
        return mAdapter.areAllItemsEnabled()
    }

    override fun isEnabled(position: Int): Boolean {
        return mAdapter.isEnabled(position)
    }

    override fun hasStableIds(): Boolean {
        return mAdapter.hasStableIds()
    }

    override fun getItemViewType(position: Int): Int {
        return mAdapter.getItemViewType(position)
    }

    override fun getViewTypeCount(): Int {
        return mAdapter.viewTypeCount
    }

    override fun isEmpty(): Boolean {
        return mAdapter.isEmpty
    }

    override fun getWrappedAdapter(): ListAdapter {
        return mAdapter
    }

}
