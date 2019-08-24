package cn.wandersnail.listview.swipe;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

/**
 * date: 2019/8/24 12:25
 * author: zengfansheng
 */
abstract class SwipeMenuAdapter implements WrapperListAdapter, SwipeMenuView.OnSwipeItemClickListener {
    private final ListAdapter adapter;

    SwipeMenuAdapter(ListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return adapter;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return adapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return adapter.isEnabled(position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return adapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return adapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SwipeMenuLayout layout;
        SwipeMenuListView lv = (SwipeMenuListView) parent;
        if (convertView == null) {
            View contentView = adapter.getView(position, null, lv);
            SwipeMenu menu = new SwipeMenu();
            menu.itemViewType = getItemViewType(position);
            createMenu(menu);
            SwipeMenuView menuView = new SwipeMenuView(lv.getContext());
            menuView.initialize(menu);
            menuView.listener = this;
            layout = new SwipeMenuLayout(lv.getContext());
            layout.initialize(contentView, menuView, lv.openInterpolator, lv.closeInterpolator);
        } else {
            layout = (SwipeMenuLayout) convertView;
            layout.closeMenu();
            adapter.getView(position, layout.contentView, parent);
        }       
        layout.setPosition(position);
        if (adapter instanceof SwipeController) {
            layout.swipeEnabled = ((SwipeController) adapter).isSwipeEnabled(position);
        }
        return layout;
    }

    @Override
    public int getItemViewType(int position) {
        return adapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return adapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return adapter.isEmpty();
    }
        
    abstract void createMenu(SwipeMenu menu);
}
