package com.snail.swipemenulistview

import android.util.TypedValue
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat

class SwipeMenuLayout(internal var contentView: View, internal var menuView: SwipeMenuView, closeInterpolator: Interpolator? = null, openInterpolator: Interpolator? = null) : FrameLayout(contentView.context) {
    private var mSwipeDirection: Int = 0
    private var mDownX: Int = 0
    private var mDownY: Int = 0
    private var lastMoveX: Float = 0.toFloat()
    private var moveDis: Float = 0.toFloat()
    private var state = STATE_CLOSE
    private var mGestureDetector: GestureDetectorCompat? = null
    private var isFling: Boolean = false
    private val minFling = dp2px(5)
    private val maxVelocityx = dp2px(200)
    private var mOpenScroller: OverScroller? = null
    private var mCloseScroller: OverScroller? = null
    private var mBaseX: Int = 0
    var position: Int = 0
        set(position) {
            field = position
            menuView.position = position
        }
    private var mCloseInterpolator: Interpolator? = closeInterpolator
    private var mOpenInterpolator: Interpolator? = openInterpolator

    var swipEnable = true
    private var isClick: Boolean = false

    val isOpen: Boolean
        get() = state == STATE_OPEN

    init {
        this.menuView.setLayout(this)
        init()
    }

    fun setSwipeDirection(swipeDirection: Int) {
        mSwipeDirection = swipeDirection
    }

    private fun init() {
        layoutParams = AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val mGestureListener = object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                isFling = false
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (Math.abs(velocityX) > maxVelocityx && Math.abs(e1.x - e2.x) > minFling) {
                    isFling = true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }
        mGestureDetector = GestureDetectorCompat(context, mGestureListener)

        mCloseScroller = if (mCloseInterpolator != null) {
            OverScroller(context, mCloseInterpolator)
        } else {
            OverScroller(context)
        }
        mOpenScroller = if (mOpenInterpolator != null) {
            OverScroller(context, mOpenInterpolator)
        } else {
            OverScroller(context)
        }

        val contentParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        contentView.layoutParams = contentParams
        menuView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        addView(contentView)
        addView(menuView)
    }

    fun onSwipe(event: MotionEvent): Boolean {
        mGestureDetector!!.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x.toInt()
                mDownY = event.y.toInt()
                isFling = false
                isClick = true
            }
            MotionEvent.ACTION_MOVE -> {
                moveDis = lastMoveX - event.x
                lastMoveX = event.x
                var dis = (mDownX - event.x).toInt()
                if (Math.abs(dis) > dp2px(2)) {
                    isClick = false
                }
                if (state == STATE_OPEN) {
                    dis += menuView.width * mSwipeDirection
                }
                swipe(dis)
            }
            MotionEvent.ACTION_UP -> if (isFling) {
                if (Math.signum(moveDis) == mSwipeDirection.toFloat()) {
                    smoothOpenMenu()
                } else {
                    smoothCloseMenu()
                    return false
                }
            } else if (isClick || Math.abs(mDownX - event.x) < Math.abs(mDownY - event.y)) {
                smoothCloseMenu()
                return false
            } else if (Math.abs(mDownX - event.x) > menuView.width / 2 && Math.signum(mDownX - event.x) == mSwipeDirection.toFloat() && !isOpen ||
                    Math.abs(mDownX - event.x) < menuView.width / 2 && Math.signum(mDownX - event.x) == (-mSwipeDirection).toFloat() && isOpen ||
                    isOpen && (menuView.left == 0 || menuView.right == contentView.width)) {
                smoothOpenMenu()
            } else {
                smoothCloseMenu()
                return false
            }
        }
        return true
    }

    private fun swipe(dis: Int) {
        var distance = dis
        if (!swipEnable) {
            return
        }
        if (Math.signum(distance.toFloat()) != mSwipeDirection.toFloat()) {
            distance = 0
        } else if (Math.abs(distance) > menuView.width) {
            distance = menuView.width * mSwipeDirection
        }

        contentView.layout(-distance, contentView.top, contentView.width - distance, measuredHeight)

        if (mSwipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            menuView.layout(contentView.width - distance, menuView.top, contentView.width + menuView.width - distance, menuView.bottom)
        } else {
            menuView.layout(-menuView.width - distance, menuView.top, -distance, menuView.bottom)
        }
    }

    override fun computeScroll() {
        if (state == STATE_OPEN) {
            if (mOpenScroller!!.computeScrollOffset()) {
                swipe(mOpenScroller!!.currX * mSwipeDirection)
                postInvalidate()
            }
        } else {
            if (mCloseScroller!!.computeScrollOffset()) {
                swipe((mBaseX - mCloseScroller!!.currX) * mSwipeDirection)
                postInvalidate()
            }
        }
    }

    fun smoothCloseMenu() {
        state = STATE_CLOSE
        if (mSwipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            mBaseX = -contentView.left
            mCloseScroller!!.startScroll(0, 0, menuView.width, 0, 400)
        } else {
            mBaseX = menuView.right
            mCloseScroller!!.startScroll(0, 0, menuView.width, 0, 400)
        }
        postInvalidate()
    }

    fun smoothOpenMenu() {
        if (!swipEnable) {
            return
        }
        state = STATE_OPEN
        if (mSwipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            mOpenScroller!!.startScroll(-contentView.left, 0, menuView.width, 0, 400)
        } else {
            mOpenScroller!!.startScroll(contentView.left, 0, menuView.width, 0, 400)
        }
        postInvalidate()
    }

    fun closeMenu() {
        if (mCloseScroller!!.computeScrollOffset()) {
            mCloseScroller!!.abortAnimation()
        }
        state = STATE_CLOSE
        swipe(0)
    }

    fun openMenu() {
        if (!swipEnable) {
            return
        }
        state = STATE_OPEN
        swipe(menuView.width * mSwipeDirection)
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                context.resources.displayMetrics).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        menuView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        contentView.layout(0, 0, measuredWidth, contentView.measuredHeight)
        if (mSwipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            menuView.layout(measuredWidth, 0, measuredWidth + menuView.measuredWidth, contentView.measuredHeight)
        } else {
            menuView.layout(-menuView.measuredWidth, 0, 0, contentView.measuredHeight)
        }
    }

    fun setMenuHeight(measuredHeight: Int) {
        val params = menuView.layoutParams as FrameLayout.LayoutParams
        if (params.height != measuredHeight) {
            params.height = measuredHeight
            menuView.layoutParams = menuView.layoutParams
        }
    }

    companion object {
        private const val STATE_CLOSE = 0
        private const val STATE_OPEN = 1
    }
}
