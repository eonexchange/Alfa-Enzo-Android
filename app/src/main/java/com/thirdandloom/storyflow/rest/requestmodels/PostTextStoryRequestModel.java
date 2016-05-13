package com.thirdandloom.storyflow.rest.requestmodels;

import com.google.gson.annotations.SerializedName;
import com.thirdandloom.storyflow.models.PendingStory;
import com.thirdandloom.storyflow.rest.RestClient;
import com.thirdandloom.storyflow.utils.DateUtils;

public class PostTextStoryRequestModel extends BaseRequestModel {
    @SerializedName("story")
    public Description description;
    @SerializedName("customStory")
    public Type storyType;

    public PostTextStoryRequestModel(PendingStory pendingStory) {
        this.description = new Description();
        this.description.text = pendingStory.getDescription();
        this.description.date = DateUtils.getDateString(RestClient.DATE_FORMAT, pendingStory.getDate());
        this.storyType = new Type();
        this.storyType.type = "text";
    }

    private static class Description {
        @SerializedName("description")
        public String text;
        @SerializedName("storyDate")
        public String date;
    }

    private static class Type {
        @SerializedName("storyType")
        public String type;
    }
}
