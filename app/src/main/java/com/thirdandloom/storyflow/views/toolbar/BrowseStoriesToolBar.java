package com.thirdandloom.storyflow.views.toolbar;

import com.bumptech.glide.Glide;
import com.thirdandloom.storyflow.R;
import com.thirdandloom.storyflow.StoryflowApplication;
import com.thirdandloom.storyflow.adapters.PeriodsAdapter;
import com.thirdandloom.storyflow.models.User;
import com.thirdandloom.storyflow.utils.glide.CropCircleTransformation;
import rx.functions.Action0;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BrowseStoriesToolBar extends Toolbar {
    public BrowseStoriesToolBar(Context context) {
        this(context, null);
    }

    public BrowseStoriesToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.toolbarStyle);
    }

    public BrowseStoriesToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private TextView userNameTextView;
    private TextView fullUserNameTextView;
    private ImageView avatarImageView;
    private ImageView changeSizeImageView;
    private ImageView changePeriodImageView;
    private Action0 onChangePeriod;
    private Action0 onChangeSize;

    private void init() {
        inflate(getContext(), R.layout.view_toolbar_activity_browsing_stories, this);

        userNameTextView = (TextView) findViewById(R.id.toolbar_activity_browsing_stories_user_name);
        fullUserNameTextView = (TextView) findViewById(R.id.toolbar_activity_browsing_stories_full_name);
        avatarImageView = (ImageView) findViewById(R.id.toolbar_activity_browsing_stories_avatar);
        changeSizeImageView = (ImageView) findViewById(R.id.toolbar_activity_browsing_stories_increase_size);
        changePeriodImageView = (ImageView) findViewById(R.id.toolbar_activity_browsing_stories_change_period);

        User user = StoryflowApplication.account().getUser();
        userNameTextView.setText(user.getUsername());
        fullUserNameTextView.setText(user.getFullUserName());
        Glide
                .with(getContext())
                .load(user.getProfileImage().getImageUrl())
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .dontAnimate()
                .into(avatarImageView);
        changePeriodImageView.setOnClickListener(this::onChangePeriodClicked);
        changeSizeImageView.setOnClickListener(this::onChangeSizeClicked);
    }

    private void onChangePeriodClicked(View view) {
        if (onChangePeriod != null) onChangePeriod.call();
    }

    private void onChangeSizeClicked(View view) {
        if (onChangeSize != null) onChangeSize.call();
    }

    public void setOnChangePeriod(Action0 onChangePeriod) {
        this.onChangePeriod = onChangePeriod;
    }

    public void setOnChangeSize(Action0 onChangeSize) {
        this.onChangeSize = onChangeSize;
    }

    public void onNewItemWidthSelected(PeriodsAdapter.ItemWidth itemWidth) {
        changeSizeImageView.setImageResource(toolbarSizeImage.get(itemWidth.ordinal()));
    }

    private static final Map<Integer, Integer> toolbarSizeImage;
    static {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(PeriodsAdapter.ItemWidth.Small.ordinal(), R.drawable.plus_icon);
        map.put(PeriodsAdapter.ItemWidth.Large.ordinal(), R.drawable.minus_icon);
        toolbarSizeImage = Collections.unmodifiableMap(map);
    }
}