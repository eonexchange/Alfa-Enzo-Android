package com.thirdandloom.storyflow.adapters;

import com.thirdandloom.storyflow.R;
import com.thirdandloom.storyflow.StoryflowApplication;
import com.thirdandloom.storyflow.adapters.holder.BrowsePeriodBaseHolder;
import com.thirdandloom.storyflow.adapters.holder.BrowsePeriodEmptyHolder;
import com.thirdandloom.storyflow.adapters.holder.BrowsePeriodLargePopulatedHolder;
import com.thirdandloom.storyflow.adapters.holder.BrowsePeriodPendingHolder;
import com.thirdandloom.storyflow.adapters.holder.BrowsePeriodSmallPopulatedHolder;
import com.thirdandloom.storyflow.adapters.holder.BrowsePeriodSmallestPopulatedHolder;
import com.thirdandloom.storyflow.managers.StoriesManager;
import com.thirdandloom.storyflow.models.Story;
import com.thirdandloom.storyflow.utils.AndroidUtils;
import com.thirdandloom.storyflow.utils.DateUtils;
import com.thirdandloom.storyflow.utils.DeviceUtils;
import com.thirdandloom.storyflow.utils.ViewUtils;
import rx.functions.Func0;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BrowsePeriodsAdapter extends RecyclerView.Adapter<BrowsePeriodEmptyHolder> {

    public enum ItemType {
        Large, Small, Smallest
    }

    public enum PeriodType {
        Daily, Monthly, Yearly
    }

    enum DataType {
        EmptyStories, PendingStories, PopulatedStories
    }

    private static final int EMPTY = 1;
    private static final int PENDING = EMPTY + 1;
    private static final int POPULATED_LARGE = PENDING + 1;
    private static final int POPULATED_SMALL = POPULATED_LARGE + 1;
    private static final int POPULATED_SMALLEST = POPULATED_SMALL + 1;

    private final Handler postponeHandler = new Handler();
    private final StoriesManager storiesManager;
    private final Context context;
    private int centerPosition;

    private ItemType itemType = ItemType.Large;
    private PeriodType periodType = PeriodType.Daily;
    private BrowsePeriodBaseHolder.Actions storyPreviewActions;
    private Func0<Integer> getParentHeightAction;

    public BrowsePeriodsAdapter(Context context, @Nullable LinkedHashMap<Calendar, Story.WrapList> store,
                                @Nullable List<Integer> fetchedPositions, @Nullable StoriesManager.RequestData requestData) {
        this.context = context;
        this.storiesManager = new StoriesManager(store, fetchedPositions, requestData);
    }

    public void setGetParentHeightAction(Func0<Integer> getParentHeightAction) {
        this.getParentHeightAction = getParentHeightAction;
    }

    public void setStoryPreviewActions(BrowsePeriodBaseHolder.Actions storyPreviewActions) {
        this.storyPreviewActions = storyPreviewActions;
    }

    public StoriesManager getStoriesManager() {
        return storiesManager;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setCenterPosition(int centerPosition) {
        this.centerPosition = centerPosition;
    }

    public void onNewStoriesFetched(Story.WrapList list, Calendar calendar) {
        onNewStoriesFetched(list, calendar, getPosition(calendar));
    }

    public void onNewStoriesFetched(Story.WrapList list, Calendar calendar, int position) {
        storiesManager.storeData(calendar, list);
        updateDataFromLocalStore(position);
    }

    public void onNewStoriesFetchFailed(int position) {
        storiesManager.removeFromFetchedPositions(position);
        updateDataFromLocalStore(position);
    }

    public void updatePeriodType() {
        switch (storiesManager.getRequestData().getPeriodType()) {
            case Yearly:
                periodType = PeriodType.Yearly;
                break;
            case Monthly:
                periodType = PeriodType.Monthly;
                break;
            case Daily:
                periodType = PeriodType.Daily;
                break;
        }
    }

    public void setPeriodType(PeriodType periodType) {
        this.periodType = periodType;
    }

    public void clearData() {
        this.storiesManager.clearStore();
    }

    public PeriodType getPeriodType() {
        return periodType;
    }

    public void changeItemWidth() {
        switch (itemType) {
            case Large:
                itemType = ItemType.Small;
                break;
            case Small:
                itemType = ItemType.Smallest;
                break;
            case Smallest:
                itemType = ItemType.Large;
                break;
        }
    }

    public int getCenterPosition() {
        return centerPosition;
    }

    public int getItemWidthPixel() {
        switch (itemType) {
            case Large:
                return DeviceUtils.getDisplayWidth() - getItemPadding() * 2;
            case Small:
                return DeviceUtils.getDisplayWidth() / 2;
            case Smallest:
                return DeviceUtils.getDisplayWidth() / 3 - getItemPadding() * 2;
        }
        throw new UnsupportedOperationException("unsupported itemWidth is using");
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public void updateDataFromLocalStore(int position) {
        postponeHandler.post(() -> notifyItemChanged(position));
    }

    public int getPosition(Calendar storyDateCalendar) {
        Calendar calendar = DateUtils.todayCalendar();

        switch (periodType) {
            case Daily:
                long todayTime = calendar.getTime().getTime();
                long storyDateTime = storyDateCalendar.getTime().getTime();
                long diffDays = TimeUnit.MILLISECONDS.toDays(todayTime - storyDateTime);
                return (int) (centerPosition - diffDays);

            case Monthly:
                int diffYear = calendar.get(Calendar.YEAR) - storyDateCalendar.get(Calendar.YEAR);
                int diffMonth = diffYear * 12 + calendar.get(Calendar.MONTH) - storyDateCalendar.get(Calendar.MONTH);
                return (centerPosition - diffMonth);

            case Yearly:
                return (centerPosition - (calendar.get(Calendar.YEAR) - storyDateCalendar.get(Calendar.YEAR)));

            default:
                throw new UnsupportedOperationException("unsupported itemType is using");
        }
    }

    @NonNull
    private static Calendar updateDate(BrowsePeriodEmptyHolder holder, int position, int centerPosition, PeriodType periodType) {
        Calendar periodDate = getDateCalendar(position, centerPosition, periodType);
        switch (periodType) {
            case Daily:
                DateUtils.getDailyRepresentation(periodDate, holder::setDateRepresentation);
                break;
            case Monthly:
                DateUtils.getMonthlyRepresentation(periodDate, holder::setDateRepresentation);
                break;
            case Yearly:
                DateUtils.getYearlyRepresentation(periodDate, holder::setDateRepresentation);
                break;
            default:
                throw new UnsupportedOperationException("unsupported itemType is using");
        }
        return periodDate;
    }

    public static Calendar getDateCalendar(int position, int centerPosition, PeriodType periodType) {
        Calendar calendar = DateUtils.todayCalendar();
        int offset = position - centerPosition;
        switch (periodType) {
            case Daily:
                calendar.add(Calendar.DAY_OF_YEAR, offset);
                break;
            case Monthly:
                calendar.add(Calendar.MONTH, offset);
                break;
            case Yearly:
                calendar.add(Calendar.YEAR, offset);
                break;
            default:
                throw new UnsupportedOperationException("unsupported itemType is using");
        }
        return calendar;
    }

    public static int getItemPadding() {
        return AndroidUtils.dp(14);
    }

    public static int getItemMargin() {
        return StoryflowApplication.resources().getDimensionPixelOffset(R.dimen.sizeTiniest);
    }

    @Override
    public BrowsePeriodEmptyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case EMPTY:
                return BrowsePeriodEmptyHolder.newInstance(parent, storyPreviewActions);
            case PENDING:
                return BrowsePeriodPendingHolder.newInstance(parent, storyPreviewActions);
            case POPULATED_LARGE:
                return BrowsePeriodLargePopulatedHolder.newInstance(parent, storyPreviewActions);
            case POPULATED_SMALL:
                return BrowsePeriodSmallPopulatedHolder.newInstance(parent, storyPreviewActions);
            case POPULATED_SMALLEST:
                return BrowsePeriodSmallestPopulatedHolder.newInstance(parent, storyPreviewActions);

            default:
                throw new UnsupportedOperationException("Your are using unsupported item view type");
        }
    }

    @Override
    public void onBindViewHolder(BrowsePeriodEmptyHolder holder, int position) {
        ViewUtils.applyWidth(holder.itemView, getItemWidthPixel());
        Calendar periodDate = updateDate(holder, position, centerPosition, periodType);
        holder.setPeriodDate(periodDate);
        switch (getItemViewType(position)) {
            case POPULATED_LARGE:
            case POPULATED_SMALL:
            case POPULATED_SMALLEST:
                Story.WrapList wrapList = storiesManager.getDisplayingStories(periodDate);
                BrowsePeriodSmallestPopulatedHolder populatedHolder = (BrowsePeriodSmallestPopulatedHolder) holder;
                populatedHolder.setStories(wrapList.getStories(), context, getItemWidthPixel(), getItemType(), getParentHeightAction.call());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        Calendar calendar = getDateCalendar(position, centerPosition, periodType);
        Story.WrapList wrapList = storiesManager.getDisplayingStories(calendar);
        switch (getDataType(wrapList)) {
            case EmptyStories:
                return EMPTY;
            case PendingStories:
                return PENDING;
            case PopulatedStories:
                switch (itemType) {
                    case Smallest:
                        return POPULATED_SMALLEST;
                    case Small:
                        return POPULATED_SMALL;
                    case Large:
                        return POPULATED_LARGE;
                    default:
                        throw new UnsupportedOperationException("Your are using unsupported item type");
                }
            default:
                throw new UnsupportedOperationException("Your are using unsupported item view type");
        }
    }

    private DataType getDataType(Story.WrapList wrapList) {
        if (wrapList == null) {
            return DataType.PendingStories;
        } else if (wrapList.getStories() == null
                || wrapList.getStories().size() == 0) {
            return DataType.EmptyStories;
        } else {
            return DataType.PopulatedStories;
        }
    }
}
