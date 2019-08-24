package cn.wandersnail.listview.swipe;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 侧滑菜单
 * 
 * date: 2019/8/24 10:39
 * author: zengfansheng
 */
public class SwipeMenu {
    private final List<SwipeMenuItem> items = new ArrayList<>();
    //菜单所在条目的类型
    int itemViewType;

    /**
     * 获取菜单所在条目的类型
     */
    public int getItemViewType() {
        return itemViewType;
    }

    @NonNull
    public List<SwipeMenuItem> getMenuItems() {
        return items;
    }
    
    public void addItem(@NonNull SwipeMenuItem item) {
        items.add(item);
    }
    
    public void removeItem(@NonNull SwipeMenuItem item) {
        items.remove(item);
    }
    
    public SwipeMenuItem getItem(int index) {
        return items.get(index);
    }
}
