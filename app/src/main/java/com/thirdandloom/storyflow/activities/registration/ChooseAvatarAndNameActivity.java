package com.thirdandloom.storyflow.activities.registration;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.thirdandloom.storyflow.R;
import com.thirdandloom.storyflow.StoryflowApplication;
import com.thirdandloom.storyflow.activities.BaseActivity;
import com.thirdandloom.storyflow.activities.BrowseStoriesActivity;
import com.thirdandloom.storyflow.activities.SignUpActivity;
import com.thirdandloom.storyflow.utils.ActivityUtils;
import com.thirdandloom.storyflow.utils.SerializableRectF;
import com.thirdandloom.storyflow.utils.Validation;
import com.thirdandloom.storyflow.utils.glide.CropCircleTransformation;
import com.thirdandloom.storyflow.utils.glide.CropRectTransformation;
import com.thirdandloom.storyflow.utils.glide.LogCrashRequestListener;
import com.thirdandloom.storyflow.utils.image.PhotoFileUtils;
import com.thirdandloom.storyflow.utils.image.Size;
import com.thirdandloom.storyflow.views.dialog.ChooseActionDialog;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.Serializable;

public class ChooseAvatarAndNameActivity extends BaseActivity {
    private static final int CAPTURE_PHOTO = 1;
    private static final int SELECT_PHOTO = CAPTURE_PHOTO + 1;
    private static final int SET_PROFILE_PICTURE = SELECT_PHOTO + 1;

    private ImageView avatarImageView;
    private EditText nameEditText;
    private SavedState state;

    public static Intent newInstance(String userName, String email, String password) {
        Intent intent = new Intent(StoryflowApplication.getInstance(), ChooseAvatarAndNameActivity.class);
        SavedState state = new SavedState();
        state.userName = userName;
        state.email = email;
        state.password = password;
        putExtra(intent, state);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_avatar);
        findViews();
        initGui();
        state = (SavedState) getState();
        restoreState(savedInstanceState, restoredState -> {
            state = (SavedState) restoredState;
            if (state.imageTaken())
                loadImage();
        });
    }

    private void findViews() {
        nameEditText = (EditText) findViewById(R.id.activity_choose_avatar_name);
        avatarImageView = (ImageView) findViewById(R.id.activity_choose_avatar_image_view);
        findViewById(R.id.activity_choose_avatar_done).setOnClickListener(v -> {
            Validation.splitNameCredentials(getSplitName(), this::showWarning, this::registerUser);
        });
        findViewById(R.id.activity_choose_avatar_selector).setOnClickListener(v -> selectImage());
    }

    private void initGui() {
        setTitle(R.string.profile_picture_and_name);
    }

    private String getSplitName() {
        return nameEditText.getText().toString();
    }

    private void selectImage() {
        ChooseActionDialog dialog = ChooseActionDialog.newInstance(this::takePhoto, this::selectPhoto);
        dialog.show(getSupportFragmentManager(), "Test");
    }

    private void takePhoto() {
        PhotoFileUtils.checkStoragePermissionsAreGuaranteed(this, this::startCapturePhotoIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PhotoFileUtils.REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCapturePhotoIntent();
                } else {
                    showWarning(R.string.permissions_were_not_guaranteed);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_PHOTO:
                    startActivityForResult(ConfigureAvatarRectangleActivity.newInstance(state.capturedAbsolutePhotoPath, CAPTURE_PHOTO), SET_PROFILE_PICTURE);
                    break;
                case SELECT_PHOTO:
                    startActivityForResult(ConfigureAvatarRectangleActivity.newInstance(data.getData().toString(), SELECT_PHOTO), SET_PROFILE_PICTURE);
                    break;
                case SET_PROFILE_PICTURE:
                    ConfigureAvatarRectangleActivity.extractData(data, this::configureImageFinished);
                    break;
            }
        }
    }

    private void configureImageFinished(String imageUrl, Integer takenAction, RectF rect, Size cachedSize) {
        state.cachedSize = cachedSize;
        state.imageUrl = imageUrl;
        state.rect = new SerializableRectF(rect);
        state.takenAction = takenAction;
        loadImage();
    }

    private void loadImage() {
        createGlideRequest(this, state.takenAction, state.imageUrl)
                .bitmapTransform(new CropRectTransformation(this, state.rect.getRectF()), new CropCircleTransformation(this))
                .crossFade()
                .listener(new LogCrashRequestListener<>())
                .override(state.cachedSize.width(), state.cachedSize.height())
                .into(avatarImageView);
    }

    private void selectPhoto() {
        ActivityUtils.selectPhoto(this, SELECT_PHOTO);
    }

    private void startCapturePhotoIntent() {
        state.capturedAbsolutePhotoPath = ActivityUtils.capturePhoto(this, CAPTURE_PHOTO);
    }

    private void registerUser(String firstName, String lastName) {
        showProgress(Gravity.RIGHT);
        StoryflowApplication.restClient().signUp(state.email, state.userName, state.password, firstName, lastName, user -> {
            //StoryflowApplication.account().updateProfile(user);
            if (state.imageTaken()) {
                uploadFullImage();
            } else {
                hideProgress();
                //start browsing
            }

        }, this::showError);
    }

    private void uploadFullImage() {
        StoryflowApplication.restClient().createProfileImage(createGlideRequest(this, state.takenAction, state.imageUrl), responseBody -> {
            uploadCroppedAvatar();
        }, errorMessage -> {
            hideProgress();
            //start browsing
        });
    }

    private void uploadCroppedAvatar() {
        //hideProgress();

    }

    @Override
    protected int getStatusBarColor() {
        return R.color.greyLighter;
    }

    @Override
    protected boolean hasToolBar() {
        return true;
    }

    @Nullable
    @Override
    protected Serializable getSavedState() {
        return state;
    }

    private static class SavedState implements Serializable {
        int takenAction;
        SerializableRectF rect;
        Size cachedSize;
        String imageUrl;

        String capturedAbsolutePhotoPath;

        String userName;
        String email;
        String password;

        boolean imageTaken() {
            return !TextUtils.isEmpty(imageUrl);
        }
    }

    public static DrawableTypeRequest createGlideRequest(Activity activity, int takenAction, String imageUrl) {
        switch (takenAction) {
            case CAPTURE_PHOTO:
                return Glide
                        .with(activity)
                        .load(imageUrl);

            case SELECT_PHOTO:
                return Glide
                        .with(activity)
                        .load(Uri.parse(imageUrl));
            default:

                throw new UnsupportedOperationException("Selected takenAction is unsupported");

        }
    }
}