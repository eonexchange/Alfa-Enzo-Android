package com.thirdandloom.storyflow.utils.animations;

import com.thirdandloom.storyflow.utils.AndroidUtils;
import com.thirdandloom.storyflow.utils.AnimationUtils;
import com.thirdandloom.storyflow.utils.ViewUtils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class FooterHiderScrollListener extends RecyclerView.OnScrollListener {

    public static FooterHiderScrollListener init(RecyclerView recyclerView, View footerView) {
        return new FooterHiderScrollListener(recyclerView, footerView);
    }

    private final View footer;
    private int currentScroll;
    private boolean footerIsVisible;

    public FooterHiderScrollListener (RecyclerView recyclerView, View view) {
        this.footer = view;
        this.footerIsVisible = ViewUtils.isVisible(view);
        recyclerView.addOnScrollListener(this);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        currentScroll += dy;
        if (currentScroll >= AndroidUtils.minScrollPx()) {
            currentScroll = AndroidUtils.minScrollPx();
            if (footerIsVisible) {
                footerIsVisible = false;
                AnimationUtils.hideFooter(footer);
            }
        } else if (currentScroll < 0) {
            currentScroll = 0;
            startShowFooterAnimated();
        }
    }

    public void showFooterOnDismiss() {
        if (!ViewUtils.isVisible(footer)) AnimationUtils.showFooter(footer);
    }

    private void startShowFooterAnimated() {
        if (!footerIsVisible) {
            footerIsVisible = true;
            AnimationUtils.showFooter(footer);
        }
    }
}
