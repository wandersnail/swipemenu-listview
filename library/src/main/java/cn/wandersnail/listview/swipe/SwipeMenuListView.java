package cn.wandersnail.listview.swipe;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

/**
 * date: 2019/8/24 12:14
 * author: zengfansheng
 */
public class SwipeMenuListView extends ListView {
    public static final int TOUCH_STATE_NONE = 0;
    public static final int TOUCH_STATE_X = 1;
    public static final int TOUCH_STATE_Y = 2;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;
    public static final int DIRECTION_LEFT_TO_RIGHT = -1;
    private SwipeMenuLayout touchView;
    private OnMenuStateChangeListener stateChangeListener;
    private OnMenuItemClickListener itemClickListener;
    private int direction = DIRECTION_RIGHT_TO_LEFT;
    private int maxX;
    private int maxY;
    private int touchState = TOUCH_STATE_NONE;
    Interpolator openInterpolator;
    Interpolator closeInterpolator;
    private float downX;
    private float downY;
    private int touchPosition;
    private SwipeMenuCreator menuCreator;
    private boolean isSwipeStarted;

    public SwipeMenuListView(Context context) {
        this(context, null);
    }

    public SwipeMenuListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        maxX = dp2px(3);
        maxY = dp2px(5);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(new SwipeMenuAdapter(adapter) {
            @Override
            void createMenu(SwipeMenu menu) {
                if (menuCreator != null) {
                    menuCreator.create(menu);
                }
            }

            @Override
            public void onItemClick(@NonNull SwipeMenuView view, @NonNull SwipeMenu menu, int index) {
                boolean flag = false;
                if (itemClickListener != null) {
                    flag = itemClickListener.onMenuItemClick(view.position, menu, index);
                }
                if (touchView != null && !flag) {
                    touchView.smoothCloseMenu();
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                //如果有打开了，触摸在菜单以外的都拦截
                if (touchView != null && touchView.isOpen() && !inRangeOfView(touchView.menuView, ev)) {
                    return true;
                }
                boolean handled = super.onInterceptTouchEvent(ev);
                touchState = TOUCH_STATE_NONE;
                touchPosition = pointToPosition((int) downX, (int) downY);
                View view = getChildAt(touchPosition - getFirstVisiblePosition());
                //只在空的时候赋值 以免每次触摸都赋值，会有多个open状态
                if (view instanceof SwipeMenuLayout) {
                    touchView = (SwipeMenuLayout) view;
                    touchView.swipeDirection = direction;
                }
                //如果摸在另外个view
                if (touchView != null && touchView.isOpen() && view != touchView) {
                    handled = true;
                }
                if (touchView != null) {
                    touchView.onSwipe(ev);
                }
                return handled;
            case MotionEvent.ACTION_MOVE:
                float dy = Math.abs(ev.getY() - downY);
                float dx = Math.abs(ev.getX() - downX);
                if (dy > maxY || dx > maxX) {
                    //每次拦截的down都把触摸状态设置成了TOUCH_STATE_NONE 只有返回true才会走onTouchEvent 所以写在这里就够了
                    if (touchState == TOUCH_STATE_NONE) {
                        if (dy > maxY) {
                            touchState = TOUCH_STATE_Y;
                        } else {
                            touchState = TOUCH_STATE_X;
                            if (stateChangeListener != null) {
                                stateChangeListener.onSwipeStart(touchPosition);
                            }
                        }
                    }
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() != MotionEvent.ACTION_DOWN && touchView == null) {
            return super.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSwipeStarted = false;
                int oldPos = touchPosition;
                downX = ev.getX();
                downY = ev.getY();
                touchState = TOUCH_STATE_NONE;
                touchPosition = pointToPosition((int) downX, (int) downY);
                if (touchPosition == oldPos && touchView != null && touchView.isOpen()) {
                    touchState = TOUCH_STATE_X;
                    touchView.onSwipe(ev);
                    return true;
                }
                View view = getChildAt(touchPosition - getFirstVisiblePosition());
                if (touchView != null && touchView.isOpen()) {
                    touchView.smoothCloseMenu();
                    touchView = null;
                    // return super.onTouchEvent(ev);
                    // try to cancel the touch event
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    onTouchEvent(cancelEvent);
                    if (stateChangeListener != null) {
                        stateChangeListener.onMenuClosed(oldPos);
                    }
                    return true;
                }
                if (view instanceof SwipeMenuLayout) {
                    touchView = (SwipeMenuLayout) view;
                    touchView.swipeDirection = direction;
                }
                if (touchView != null) {
                    touchView.onSwipe(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //有些可能有header,要减去header再判断
                touchPosition = pointToPosition((int) ev.getX(), (int) ev.getY()) - getHeaderViewsCount();
                //如果滑动了一下没完全展现，就收回去，这时候touchView已经赋值，再滑动另外一个不可以swipe的view
                //会导致touchView swipe。 所以要用位置判断是否滑动的是一个view
                //如果已经在滑动，手指滑出条目，继续让条目滑动
                if (!isSwipeStarted && (!touchView.swipeEnabled || touchPosition != touchView.getPosition())) {
                    return super.onTouchEvent(ev);
                }
                float dy = Math.abs(ev.getY() - downY);
                float dx = Math.abs(ev.getX() - downX);
                if (touchState == TOUCH_STATE_X) {
                    if (touchView != null) {
                        touchView.onSwipe(ev);
                    }
                    getSelector().setState(new int[0]);
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                } else if (touchState == TOUCH_STATE_NONE) {
                    if (dy > maxY) {
                        touchState = TOUCH_STATE_Y;
                    } else if (dx > maxX) {
                        isSwipeStarted = true;
                        touchState = TOUCH_STATE_X;
                        if (stateChangeListener != null) {
                            stateChangeListener.onSwipeStart(touchPosition);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (touchState == TOUCH_STATE_X) {
                    if (touchView != null) {
                        boolean isBeforeOpen = touchView.isOpen();
                        touchView.onSwipe(ev);
                        boolean isAfterOpen = touchView.isOpen();
                        if (isBeforeOpen != isAfterOpen && stateChangeListener != null) {
                            if (isAfterOpen) {
                                stateChangeListener.onMenuOpened(touchPosition);
                            } else {
                                stateChangeListener.onMenuClosed(touchPosition);
                            }
                        }
                        if (stateChangeListener != null) {
                            stateChangeListener.onSwipeEnd(touchPosition);
                        }
                        if (!isAfterOpen) {
                            touchPosition = -1;
                            touchView = null;
                        }
                    }
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.onTouchEvent(ev);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 平滑打开菜单
     *
     * @param position 要打开菜单条目的位置
     */
    public void smoothOpenMenu(int position) {
        openMenu(position, true);
    }

    /**
     * 平滑关闭菜单
     */
    public void smoothCloseMenu() {
        closeMenu(true);
    }

    /**
     * 打开菜单，无过渡动画
     *
     * @param position 要打开菜单条目的位置
     */
    public void openMenuImmediately(int position) {
        openMenu(position, false);
    }

    /**
     * 关闭菜单，无过渡动画
     */
    public void closeMenuImmediately() {
        closeMenu(false);
    }

    private void openMenu(int position, boolean smooth) {
        if (position >= getFirstVisiblePosition() && position <= getLastVisiblePosition()) {
            View view = getChildAt(position - getFirstVisiblePosition());
            if (view instanceof SwipeMenuLayout) {
                touchPosition = position;
                closeMenu(smooth);
                touchView = (SwipeMenuLayout) view;
                touchView.swipeDirection = direction;
                if (smooth) {
                    touchView.smoothOpenMenu();
                } else {
                    touchView.openMenu();
                }
            }
        }
    }

    private void closeMenu(boolean smooth) {
        if (touchView != null && touchView.isOpen()) {
            if (smooth) {
                touchView.smoothCloseMenu();
            } else {
                touchView.closeMenu();
            }
        }
    }

    /**
     * 设置菜单关闭动画插值器
     */
    public void setCloseInterpolator(Interpolator closeInterpolator) {
        this.closeInterpolator = closeInterpolator;
    }

    /**
     * 设置菜单打开动画插值器
     */
    public void setOpenInterpolator(Interpolator openInterpolator) {
        this.openInterpolator = openInterpolator;
    }

    /**
     * 设置菜单创建器
     */
    public void setMenuCreator(@NonNull SwipeMenuCreator creator) {
        menuCreator = creator;
    }

    /**
     * 设置滑动方向
     *
     * @param direction {@link #DIRECTION_RIGHT_TO_LEFT}，{@link #DIRECTION_LEFT_TO_RIGHT}
     */
    public void setSwipeDirection(int direction) {
        this.direction = direction;
    }

    /**
     * 设置菜单条目点击监听
     */
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.itemClickListener = listener;
    }

    /**
     * 菜单条目状态监听
     */
    public void setOnMenuStateChangeListener(OnMenuStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    /**
     * 判断点击事件是否在某个view
     *
     * @param view 判断的view
     * @param ev   事件
     * @return 返回是否在
     */
    public static boolean inRangeOfView(@NonNull View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        return !(ev.getRawX() < x || ev.getRawX() > x + view.getWidth() || ev.getRawY() < y ||
                ev.getRawY() > y + view.getHeight());
    }

    /**
     * 菜单条目点击监听
     */
    public interface OnMenuItemClickListener {
        /**
         * 菜单条目被点击
         *
         * @param position 菜单在ListView中的位置
         * @param menu     菜单
         * @param index    菜单条目在菜单中的索引
         */
        boolean onMenuItemClick(int position, @NonNull SwipeMenu menu, int index);
    }

    /**
     * 菜单条目状态监听
     */
    public interface OnMenuStateChangeListener {
        /**
         * 滑动开始
         *
         * @param position 滑动的菜单条目在ListView中的位置
         */
        void onSwipeStart(int position);

        /**
         * 滑动结束
         *
         * @param position 滑动的菜单条目在ListView中的位置
         */
        void onSwipeEnd(int position);

        /**
         * 菜单打开了
         *
         * @param position 滑动的菜单条目在ListView中的位置
         */
        void onMenuOpened(int position);

        /**
         * 菜单关闭了
         *
         * @param position 滑动的菜单条目在ListView中的位置
         */
        void onMenuClosed(int position);
    }
}
