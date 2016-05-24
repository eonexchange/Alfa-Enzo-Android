package com.thirdandloom.storyflow.activities;

import com.thirdandloom.storyflow.R;
import com.thirdandloom.storyflow.StoryflowApplication;
import com.thirdandloom.storyflow.utils.Timber;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class ProfileActivity extends BaseActivity {

    public static Intent newInstance() {
        return new Intent(StoryflowApplication.applicationContext, ProfileActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle(R.string.user_profile);
        findViewById(R.id.activity_logout_continue).setOnClickListener(v -> {
            StoryflowApplication.restClient().clearCookies();
            StoryflowApplication.account().resetAccount();
            StoryflowApplication.getPendingStoriesManager().clearAll();
            Intent intent = WelcomeActivity.newInstance();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public int getStatusBarColorResourceId() {
        return R.color.greyXLighter;
    }

    @Override
    protected boolean hasToolBar() {
        return true;
    }
}
