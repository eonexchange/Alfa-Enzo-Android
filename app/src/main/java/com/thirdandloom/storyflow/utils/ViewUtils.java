package com.thirdandloom.storyflow.utils;

import rx.functions.Action1;
import rx.functions.Action2;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ScrollView;

public class ViewUtils extends BaseUtils{

    public static void removeFromParent(View view) {
        ViewGroup parent = (ViewGroup)view.getParent();
        parent.removeView(view);
    }

    public static void setShown(View view, boolean shown) {
        view.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    public static void setHidden(View view, boolean hidden) {
        view.setVisibility(hidden ? View.GONE : View.VISIBLE);
    }

    public static void hide(View view) {
        setHidden(view, true);
    }

    public static void show(View view) {
        setHidden(view, false);
    }

    public static void applyWidth(View itemView, int widthPixel) {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.width = widthPixel;
        itemView.setLayoutParams(params);
    }

    public static void applyHeight(View itemView, int heightPixel) {
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = heightPixel;
        itemView.setLayoutParams(params);
    }

    public static WindowManager.LayoutParams getFullScreenLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        return layoutParams;
    }

    public static void getMeasuredSize(View view, Action2<Integer, Integer> action) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        action.call(view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    public static void setViewFrame(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    public static void applyWrapContentHeight(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);
    }

    public static void applyMatchParent(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(params);
    }

    public static void callOnPreDraw(View view, Action1<View> action) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                action.call(view);
                return true;
            }
        });
    }

    public static void getLocationInWindow(View view, Action2<Integer, Integer> action) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        action.call(location[0], location[1]);
    }

    public static int getScrollViewContentHeight(ScrollView view) {
        return view.getChildAt(0).getHeight();
    }

}
