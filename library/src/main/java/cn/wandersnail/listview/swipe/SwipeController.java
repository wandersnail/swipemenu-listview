package cn.wandersnail.listview.swipe;

/**
 * 滑动控制器接口
 * 
 * date: 2019/8/24 10:37
 * author: zengfansheng
 */
public interface SwipeController {
    /**
     * 根据位置获取是否支持滑动
     *
     * @param position item的位置
     */
    boolean isSwipeEnabled(int position);
}
