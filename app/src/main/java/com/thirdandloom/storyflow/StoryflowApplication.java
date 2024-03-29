package com.thirdandloom.storyflow;

import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;
import com.crashlytics.android.Crashlytics;
import com.thirdandloom.storyflow.config.Config;
import com.thirdandloom.storyflow.managers.AccountManager;
import com.thirdandloom.storyflow.managers.PendingStoriesManager;
import com.thirdandloom.storyflow.preferences.ApplicationPreferences;
import com.thirdandloom.storyflow.preferences.UserPreferences;
import com.thirdandloom.storyflow.rest.IRestClient;
import com.thirdandloom.storyflow.rest.RestClient;
import com.thirdandloom.storyflow.service.UploadStoriesService;
import com.thirdandloom.storyflow.utils.Timber;
import com.thirdandloom.storyflow.utils.concurrent.SimpleExecutor;
import io.fabric.sdk.android.Fabric;
import rx.functions.Action1;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import java.util.concurrent.Future;

public class StoryflowApplication extends MultiDexApplication implements IAviaryClientCredentials {
    private static volatile Handler applicationHandler;
    private static volatile SimpleExecutor<Runnable> backgroundThreadExecutor;

    public static volatile UserPreferences userPreferences;
    public static volatile ApplicationPreferences applicationPreferences;
    public static volatile Context applicationContext;

    private static StoryflowApplication instance;
    private IRestClient restClient;
    private AccountManager accountManager;
    private PendingStoriesManager pendingStoriesManager;

    private static final String CREATIVE_SDK_CLIENT_ID = "b4bdc6c45e2846229fb8eb247a6664e2";
    private static final String CREATIVE_SDK_CLIENT_SECRET = "e9917989-e5fb-4fe7-9d04-288dead3e5bf";

    public static AccountManager account() {
        return instance.accountManager;
    }

    public static IRestClient restClient() {
        return instance.restClient;
    }

    public static Resources resources() {
        return applicationContext.getResources();
    }

    public static PendingStoriesManager getPendingStoriesManager() {
        return instance.pendingStoriesManager;
    }

    public static void runBackground(Runnable runnable) {
        runBackground(runnable, null);
    }

    public static void runBackground(Runnable runnable, Action1<Future<?>> computation) {
        backgroundThreadExecutor.execute(runnable, computation);
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            applicationHandler.post(runnable);
        } else {
            applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        applicationHandler.removeCallbacks(runnable);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        backgroundThreadExecutor = new SimpleExecutor<>("backgroundThreadExecutor");
        userPreferences = new UserPreferences();
        applicationPreferences = new ApplicationPreferences();

        AdobeCSDKFoundation.initializeCSDKFoundation(applicationContext);
        initTimber();
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
        this.restClient = new RestClient(this);
        this.accountManager = new AccountManager();
        this.pendingStoriesManager = new PendingStoriesManager();
        UploadStoriesService.notifyService();
    }

    private void initTimber() {
        if (Config.USE_DEBUG_LOGGINING) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    private class CrashReportingTree extends Timber.Tree {
        @Override public void i(String message, Object... args) {
            Crashlytics.log(String.format(message, args));
        }

        @Override public void i(Throwable t, String message, Object... args) {
            i(message, args);
        }

        @Override public void e(String message, Object... args) {
            i("ERROR: " + message, args);
        }

        @Override public void e(Throwable t, String message, Object... args) {
            e(message, args);
            Crashlytics.logException(t);
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {

        }
    }

    // Creative SDK: Image Editor
    @Override
    public String getClientID() {
        return CREATIVE_SDK_CLIENT_ID;
    }

    @Override
    public String getClientSecret() {
        return CREATIVE_SDK_CLIENT_SECRET;
    }

    @Override
    public String getBillingKey() {
        return "";
    }
}
