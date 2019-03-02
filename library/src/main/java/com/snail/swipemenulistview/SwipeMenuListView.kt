package com.snail.swipemenulistview

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.ListAdapter
import android.widget.ListView

class SwipeMenuListView : ListView {
    private var mDirection = 1//swipe from right to left by default

    private var maxY = 5
    private var maxX = 3
    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()
    private var mTouchState: Int = 0
    private var mTouchPosition: Int = 0
    private var mTouchView: SwipeMenuLayout? = null
    private var mOnSwipeListener: OnSwipeListener? = null

    private var mMenuCreator: SwipeMenuCreator? = null
    private var mOnMenuItemClickListener: OnMenuItemClickListener? = null
    private var mOnMenuStateChangeListener: OnMenuStateChangeListener? = null
    var closeInterpolator: Interpolator? = null
    var openInterpolator: Interpolator? = null
    private var isSwipeStarted: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        maxX = dp2px(maxX)
        maxY = dp2px(maxY)
        mTouchState = TOUCH_STATE_NONE
    }

    override fun setAdapter(adapter: ListAdapter) {
        super.setAdapter(object : SwipeMenuAdapter(context, adapter) {
            override fun createMenu(menu: SwipeMenu) {
                mMenuCreator?.create(menu)
            }

            override fun onItemClick(view: SwipeMenuView, menu: SwipeMenu, index: Int) {
                var flag = false
                if (mOnMenuItemClickListener != null) {
                    flag = mOnMenuItemClickListener!!.onMenuItemClick(view.position, menu, index)
                }
                if (mTouchView != null && !flag) {
                    mTouchView!!.smoothCloseMenu()
                }
            }
        })
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        //在拦截处处理，在滑动设置了点击事件的地方也能swip，点击时又不能影响原来的点击事件
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = ev.x
                mDownY = ev.y
                //如果有打开了，触摸在菜单以外的都拦截
                if (mTouchView != null && mTouchView!!.isOpen && !inRangeOfView(mTouchView!!.menuView, ev)) {
                    return true
                }
                var handled = super.onInterceptTouchEvent(ev)
                mTouchState = TOUCH_STATE_NONE
                mTouchPosition = pointToPosition(ev.x.toInt(), ev.y.toInt())
                val view = getChildAt(mTouchPosition - firstVisiblePosition)

                //只在空的时候赋值 以免每次触摸都赋值，会有多个open状态
                if (view is SwipeMenuLayout) {
                    mTouchView = view
                    mTouchView!!.setSwipeDirection(mDirection)
                }
                //如果摸在另外个view
                if (mTouchView != null && mTouchView!!.isOpen && view !== mTouchView) {
                    handled = true
                }

                mTouchView?.onSwipe(ev)
                return handled
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = Math.abs(ev.y - mDownY)
                val dx = Math.abs(ev.x - mDownX)
                if (dy > maxY || dx > maxX) {
                    //每次拦截的down都把触摸状态设置成了TOUCH_STATE_NONE 只有返回true才会走onTouchEvent 所以写在这里就够了
                    if (mTouchState == TOUCH_STATE_NONE) {
                        if (dy > maxY) {
                            mTouchState = TOUCH_STATE_Y
                        } else {
                            mTouchState = TOUCH_STATE_X
                            if (mOnSwipeListener != null) {
                                mOnSwipeListener!!.onSwipeStart(mTouchPosition)
                            }
                        }
                    }
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_DOWN && mTouchView == null)
            return super.onTouchEvent(ev)
        val action = ev.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                isSwipeStarted = false
                val oldPos = mTouchPosition
                mDownX = ev.x
                mDownY = ev.y
                mTouchState = TOUCH_STATE_NONE
                mTouchPosition = pointToPosition(ev.x.toInt(), ev.y.toInt())
                if (mTouchPosition == oldPos && mTouchView != null && mTouchView!!.isOpen) {
                    mTouchState = TOUCH_STATE_X
                    mTouchView!!.onSwipe(ev)
                    return true
                }
                val view = getChildAt(mTouchPosition - firstVisiblePosition)
                if (mTouchView != null && mTouchView!!.isOpen) {
                    mTouchView!!.smoothCloseMenu()
                    mTouchView = null
                    // return super.onTouchEvent(ev);
                    // try to cancel the touch event
                    val cancelEvent = MotionEvent.obtain(ev)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL
                    onTouchEvent(cancelEvent)
                    if (mOnMenuStateChangeListener != null) {
                        mOnMenuStateChangeListener!!.onMenuClose(oldPos)
                    }
                    return true
                }
                if (view is SwipeMenuLayout) {
                    mTouchView = view
                    mTouchView!!.setSwipeDirection(mDirection)
                }
                if (mTouchView != null) {
                    mTouchView!!.onSwipe(ev)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                //有些可能有header,要减去header再判断
                mTouchPosition = pointToPosition(ev.x.toInt(), ev.y.toInt()) - headerViewsCount
                //如果滑动了一下没完全展现，就收回去，这时候mTouchView已经赋值，再滑动另外一个不可以swip的view
                //会导致mTouchView swip 。 所以要用位置判断是否滑动的是一个view
                //如果已经在滑动，手指滑出条目，继续让条目滑动
                if (!isSwipeStarted && (!mTouchView!!.swipEnable || mTouchPosition != mTouchView!!.position)) {
                    return super.onTouchEvent(ev)
                }
                val dy = Math.abs(ev.y - mDownY)
                val dx = Math.abs(ev.x - mDownX)
                if (mTouchState == TOUCH_STATE_X) {
                    if (mTouchView != null) {
                        mTouchView!!.onSwipe(ev)
                    }
                    selector.state = intArrayOf(0)
                    ev.action = MotionEvent.ACTION_CANCEL
                    super.onTouchEvent(ev)
                    return true
                } else if (mTouchState == TOUCH_STATE_NONE) {
                    if (dy > maxY) {
                        mTouchState = TOUCH_STATE_Y
                    } else if (dx > maxX) {
                        isSwipeStarted = true
                        mTouchState = TOUCH_STATE_X
                        mOnSwipeListener?.onSwipeStart(mTouchPosition)
                    }
                }
            }
            MotionEvent.ACTION_UP -> if (mTouchState == TOUCH_STATE_X) {
                if (mTouchView != null) {
                    val isBeforeOpen = mTouchView!!.isOpen
                    mTouchView!!.onSwipe(ev)
                    val isAfterOpen = mTouchView!!.isOpen
                    if (isBeforeOpen != isAfterOpen && mOnMenuStateChangeListener != null) {
                        if (isAfterOpen) {
                            mOnMenuStateChangeListener!!.onMenuOpen(mTouchPosition)
                        } else {
                            mOnMenuStateChangeListener!!.onMenuClose(mTouchPosition)
                        }
                    }
                    mOnSwipeListener?.onSwipeEnd(mTouchPosition)
                    if (!isAfterOpen) {
                        mTouchPosition = -1
                        mTouchView = null
                    }
                }
                ev.action = MotionEvent.ACTION_CANCEL
                super.onTouchEvent(ev)
                return true
            }
        }
        return super.onTouchEvent(ev)
    }

    fun smoothOpenMenu(position: Int) {
        if (position in firstVisiblePosition..lastVisiblePosition) {
            val view = getChildAt(position - firstVisiblePosition)
            if (view is SwipeMenuLayout) {
                mTouchPosition = position
                if (mTouchView != null && mTouchView!!.isOpen) {
                    mTouchView!!.smoothCloseMenu()
                }
                mTouchView = view
                mTouchView!!.setSwipeDirection(mDirection)
                mTouchView!!.smoothOpenMenu()
            }
        }
    }

    fun smoothCloseMenu() {
        if (mTouchView != null && mTouchView!!.isOpen) {
            mTouchView!!.smoothCloseMenu()
        }
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                context.resources.displayMetrics).toInt()
    }

    fun setMenuCreator(menuCreator: SwipeMenuCreator) {
        this.mMenuCreator = menuCreator
    }

    fun setOnMenuItemClickListener(onMenuItemClickListener: OnMenuItemClickListener) {
        this.mOnMenuItemClickListener = onMenuItemClickListener
    }

    fun setOnSwipeListener(onSwipeListener: OnSwipeListener) {
        this.mOnSwipeListener = onSwipeListener
    }

    fun setOnMenuStateChangeListener(onMenuStateChangeListener: OnMenuStateChangeListener) {
        mOnMenuStateChangeListener = onMenuStateChangeListener
    }

    interface OnMenuItemClickListener {
        fun onMenuItemClick(position: Int, menu: SwipeMenu, index: Int): Boolean
    }

    interface OnSwipeListener {
        fun onSwipeStart(position: Int)
        fun onSwipeEnd(position: Int)
    }

    interface OnMenuStateChangeListener {
        fun onMenuOpen(position: Int)
        fun onMenuClose(position: Int)
    }

    fun setSwipeDirection(direction: Int) {
        mDirection = direction
    }

    companion object {
        private const val TOUCH_STATE_NONE = 0
        private const val TOUCH_STATE_X = 1
        private const val TOUCH_STATE_Y = 2

        const val DIRECTION_RIGHT_TO_LEFT = 1
        const val DIRECTION_LEFT_TO_RIGHT = -1

        /**
         * 判断点击事件是否在某个view
         * @param view 判断的view
         * @param ev 事件
         * @return 返回是否在
         */
        fun inRangeOfView(view: View, ev: MotionEvent): Boolean {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val x = location[0]
            val y = location[1]
            return !(ev.rawX < x || ev.rawX > x + view.width || ev.rawY < y || ev.rawY > y + view.height)
        }
    }
}
