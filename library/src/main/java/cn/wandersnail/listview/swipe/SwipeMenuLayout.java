package cn.wandersnail.listview.swipe;

import android.content.Context;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;

/**
 * date: 2019/8/24 10:57
 * author: zengfansheng
 */
class SwipeMenuLayout extends FrameLayout {
    View contentView;
    SwipeMenuView menuView;
    int swipeDirection;
    private int downX;
    private int downY;
    private int minFling;
    private int maxVelocityX;
    private int baseX;
    private int position;
    private float lastMoveX;
    private float moveDis;
    private boolean isOpen;
    boolean swipeEnabled = true;
    boolean isFling;
    private boolean isClick;
    private GestureDetectorCompat gestureDetector;
    private OverScroller openScroller;
    private OverScroller closeScroller;
    
    SwipeMenuLayout(@NonNull Context context) {
        super(context);
        minFling = dp2px(5);
        maxVelocityX = dp2px(200);
    }
    
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, 
                getContext().getResources().getDisplayMetrics());
    }
    
    void initialize(View contentView, SwipeMenuView menuView, Interpolator openInterpolator, Interpolator closeInterpolator) {
        this.contentView = contentView;
        this.menuView = menuView;
        menuView.setLayout(this);
        setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                isFling = false;
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityX) > maxVelocityX && Math.abs(e1.getX() - e2.getX()) > minFling) {
                    isFling = true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
        closeScroller = new OverScroller(getContext(), closeInterpolator);
        openScroller = new OverScroller(getContext(), openInterpolator);
        contentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        menuView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(contentView);
        addView(menuView);
    }
    
    int getPosition() {
        return position;
    }
    
    void setPosition(int position) {
        this.position = position;
        menuView.position = position;
    }
    
    boolean isOpen() {
        return isOpen;
    }
    
    boolean onSwipe(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                isFling = false;
                isClick = true;
        		break;
            case MotionEvent.ACTION_MOVE:		
                moveDis = lastMoveX - event.getX();
                lastMoveX = event.getX();
                int dis = (int) (downX - event.getX());
                if (Math.abs(dis) > dp2px(2)) {
                    isClick = false;
                }
                if (isOpen) {
                    dis += menuView.getWidth() * swipeDirection;
                }
                swipe(dis);
        		break;
            case MotionEvent.ACTION_UP:
                if (isFling) {
                    if (Math.signum(moveDis) == swipeDirection) {
                        smoothOpenMenu();
                    } else {
                        smoothCloseMenu();
                        return false;
                    }
                } else if (isClick || Math.abs(downX - event.getX()) < Math.abs(downY - event.getY())) {
                    smoothCloseMenu();
                    return false;
                } else if ((Math.abs(downX - event.getX()) > menuView.getWidth() / 2f && 
                        Math.signum(downX - event.getX()) == swipeDirection && !isOpen) ||
                        (Math.abs(downX - event.getX()) < menuView.getWidth() / 2f && 
                                Math.signum(downX - event.getX()) == -swipeDirection && isOpen) ||
                        (isOpen && (menuView.getLeft() == 0 || menuView.getRight() == contentView.getWidth()))) {
                    smoothOpenMenu();
                } else {
                    smoothCloseMenu();
                    return false;
                }
                break;
        }
        return true;
    }
    
    private void swipe(int distance) {
        if (!swipeEnabled) {
            return;
        }
        if (Math.signum(distance) != swipeDirection) {
            distance = 0;
        } else if (Math.abs(distance) > menuView.getWidth()) {
            distance = menuView.getWidth() * swipeDirection;
        }
        contentView.layout(-distance, contentView.getTop(), contentView.getWidth() - distance, getMeasuredHeight());
        if (swipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            menuView.layout(contentView.getWidth() - distance, menuView.getTop(), 
                    contentView.getWidth() + menuView.getWidth() - distance, menuView.getBottom());
        } else {
            menuView.layout(-menuView.getWidth() - distance, menuView.getTop(), -distance, menuView.getBottom());
        }
    }

    @Override
    public void computeScroll() {
        if (isOpen) {
            if (openScroller.computeScrollOffset()) {
                swipe(openScroller.getCurrX() * swipeDirection);
                invalidate();
            }
        } else if (closeScroller.computeScrollOffset()) {
            swipe((baseX - closeScroller.getCurrX()) * swipeDirection);
            invalidate();
        }
    }

    void smoothOpenMenu() {
        if (!swipeEnabled) {
            return;
        }
        isOpen = true;
        if (swipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            openScroller.startScroll(-contentView.getLeft(), 0, menuView.getWidth(), 0, 400);
        } else {
            openScroller.startScroll(contentView.getLeft(), 0, menuView.getWidth(), 0, 400);
        }
        postInvalidate();
    }
    
    void smoothCloseMenu() {
        isOpen = false;
        if (swipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            baseX = -contentView.getLeft();
            closeScroller.startScroll(0, 0, menuView.getWidth(), 0, 400);
        } else {
            baseX = menuView.getRight();
            closeScroller.startScroll(0, 0, menuView.getWidth(), 0, 400);
        }
        postInvalidate();
    }
    
    void closeMenu() {
        if (closeScroller.computeScrollOffset()) {
            closeScroller.abortAnimation();
        }
        isOpen = false;
        swipe(0);
    }
    
    void openMenu() {
        if (!swipeEnabled) {
            return;
        }
        isOpen = true;
        swipe(menuView.getWidth() * swipeDirection);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        menuView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), View.MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        contentView.layout(0, 0, getMeasuredWidth(), contentView.getMeasuredHeight());
        if (swipeDirection == SwipeMenuListView.DIRECTION_RIGHT_TO_LEFT) {
            menuView.layout(getMeasuredWidth(), 0, getMeasuredWidth() + menuView.getMeasuredWidth(), contentView.getMeasuredHeight());
        } else {
            menuView.layout(-menuView.getMeasuredWidth(), 0, 0, contentView.getMeasuredHeight());
        }
    }
    
    void setMenuHeight(int measuredHeight) {
        LayoutParams params = (LayoutParams) menuView.getLayoutParams();
        if (params.height != measuredHeight) {
            params.height = measuredHeight;
            menuView.setLayoutParams(params);
        }
    }
}
