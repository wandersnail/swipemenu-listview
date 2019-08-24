package cn.wandersnail.listview.swipe;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * date: 2019/8/24 10:53
 * author: zengfansheng
 */
class SwipeMenuView extends LinearLayout {
    private static final int INDEX_TAG = 1088337442;
    private SwipeMenuLayout layout;
    OnSwipeItemClickListener listener;
    private SwipeMenu menu;
    int position;
    
    SwipeMenuView(Context context) {
        super(context);
    }
    
    void initialize(SwipeMenu menu) {
        this.menu = menu;
        List<SwipeMenuItem> items = menu.getMenuItems();
        for (int i = 0; i < items.size(); i++) {
            addItem(items.get(i), i);
        }
    }
    
    void setLayout(SwipeMenuLayout layout) {
        this.layout = layout;
    }       
    
    private void addItem(SwipeMenuItem item, int index) {
        LinearLayout parent = new LinearLayout(getContext());
        parent.setLayoutParams(new LayoutParams(item.width, LayoutParams.MATCH_PARENT));
        parent.setTag(INDEX_TAG, index);
        parent.setGravity(Gravity.CENTER);
        parent.setOrientation(VERTICAL);
        parent.setBackground(item.background);
        parent.setOnClickListener(v -> {
            if (listener != null && layout.isOpen()) {
                listener.onItemClick(this, menu, (Integer) v.getTag(INDEX_TAG));
            }
        });
        addView(parent);
        if (item.icon != null) {
            if (item.iconParams != null) {
                parent.addView(createIcon(item), item.iconParams);
            } else {
                parent.addView(createIcon(item));
            }
        }
        if (!TextUtils.isEmpty(item.title)) {
            if (item.titleParams != null) {
                parent.addView(createTitle(item), item.titleParams);
            } else {
                parent.addView(createTitle(item));
            }
        }
    }
    
    private ImageView createIcon(SwipeMenuItem item) {
        ImageView iv = new ImageView(getContext());
        iv.setImageDrawable(item.icon);
        return iv;
    }
    
    private TextView createTitle(SwipeMenuItem item) {
        TextView tv = new TextView(getContext());
        tv.setText(item.title);
        tv.setGravity(Gravity.CENTER);
        if (item.titleSize != 0) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, item.titleSize);
        }
        tv.setTextColor(item.titleColor);
        if (item.typeface != null) {
            tv.setTypeface(item.typeface);
        }
        return tv;
    }
    
    interface OnSwipeItemClickListener {
        void onItemClick(@NonNull SwipeMenuView view, @NonNull SwipeMenu menu, int index);
    }
}
