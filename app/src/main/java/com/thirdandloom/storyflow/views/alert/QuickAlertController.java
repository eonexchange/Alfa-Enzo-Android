package com.thirdandloom.storyflow.views.alert;

import com.thirdandloom.storyflow.StoryflowApplication;
import com.thirdandloom.storyflow.utils.AnimationUtils;
import com.thirdandloom.storyflow.utils.CancelableRunnable;
import com.thirdandloom.storyflow.utils.ColorUtils;
import com.thirdandloom.storyflow.utils.DeviceUtils;
import com.thirdandloom.storyflow.utils.ViewUtils;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.annotation.ColorRes;
import android.support.annotation.UiThread;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class QuickAlertController {
    private static final int ALERT_DISPLAYING_TIME = 4000;

    private Window window;
    private QuickAlertView quickAlertView;
    private CancelableRunnable hideAlertRunnable;
    private int statusBarColorResourceId;

    public QuickAlertController(Window window) {
        this.window = window;
    }

    public QuickAlertController(Window window, @ColorRes int statusBarColorResourceId) {
        this(window);
        this.statusBarColorResourceId = statusBarColorResourceId;
    }

    @UiThread
    public void show(String message, QuickAlertView.Type type) {
        hidePreviousAlert();
        showQuickAlert(message, type);
        hideAlertRunnable = createHideAlertRunnable();
        quickAlertView.postDelayed(hideAlertRunnable, ALERT_DISPLAYING_TIME);
        quickAlertView.setOnClickListener(v -> hideAlertAnimate());
    }

    public void hide() {
        if (quickAlertView != null && quickAlertView.isOnScreen()) {
            DeviceUtils.updateStatusBarColor(window, ColorUtils.color(statusBarColorResourceId));
            ViewUtils.removeFromParent(quickAlertView);
        }
    }

    private void showQuickAlert(String message, QuickAlertView.Type type) {
        quickAlertView = new QuickAlertView(StoryflowApplication.applicationContext);
        quickAlertView.setText(message, type);
        window.addContentView(quickAlertView, createLayoutParams());
        quickAlertView.measure(0, 0);
        AnimationUtils.showHeader(quickAlertView, 200);
        quickAlertView.setOnClickListener(v -> hidePreviousAlert());

        int alertColorRes = QuickAlertView.getColorResourceId(type);
        int alertColor = ColorUtils.color(alertColorRes);
        DeviceUtils.updateStatusBarColor(window, ColorUtils.calculateStatusBarColor(alertColor));
    }

    private void hidePreviousAlert() {
        if (quickAlertView != null) {
            cancelPreviousRunnable();
            hideAlertAnimate();
        }
    }

    private void cancelPreviousRunnable() {
        hideAlertRunnable.cancel();
    }

    private void hideAlertAnimate() {
        AnimationUtils.hideHeader(quickAlertView, 200);
        hide();
    }

    private CancelableRunnable createHideAlertRunnable() {
        return new CancelableRunnable() {
            @Override
            public void execute() {
                hideAlertAnimate();
            }
        };
    }

    private static WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        layoutParams.format = PixelFormat.OPAQUE;
        layoutParams.windowAnimations = 0;
        return layoutParams;
    }

}
