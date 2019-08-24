package cn.wandersnail.listview.swipe;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * 菜单条目
 * <p>
 * date: 2019/8/24 10:40
 * author: zengfansheng
 */
public class SwipeMenuItem {
    String title;
    Drawable icon;
    LinearLayout.LayoutParams iconParams;
    LinearLayout.LayoutParams titleParams;
    Drawable background;
    int titleColor = Color.BLACK;
    int titleSize;
    Typeface typeface;
    int width;
    
    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(Context context, @StringRes int resId) {
        this.title = context.getString(resId);
    }

    public void setTitleParams(LinearLayout.LayoutParams titleParams) {
        this.titleParams = titleParams;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setIcon(Context context, @DrawableRes int resId) {
        this.icon = ContextCompat.getDrawable(context, resId);
    }

    public void setIconParams(LinearLayout.LayoutParams iconParams) {
        this.iconParams = iconParams;
    }

    public void setBackground(Drawable background) {
        this.background = background;
    }

    public void setBackground(Context context, @DrawableRes int resId) {
        this.background = ContextCompat.getDrawable(context, resId);
    }

    public void setTitleColor(@ColorInt int titleColor) {
        this.titleColor = titleColor;
    }

    public void setTitleColor(Context context, @ColorRes int resId) {
        this.titleColor = ContextCompat.getColor(context, resId);
    }

    public void setTitleSize(int titleSize) {
        this.titleSize = titleSize;
    }

    public void setTitleTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
