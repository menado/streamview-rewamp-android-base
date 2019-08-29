package com.streamhash.streamview.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class DisplayUtils {


    private DisplayUtils() {

    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int getActionBarHeightInPx(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return mActionBarSize;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static float pxToDp(float px) {
        float densityDpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        return px / (densityDpi / 160f);
    }

    public static int dpToPx(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static void makeBottomSheetFullScreen(Context context, BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback, View contentView) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        //setting full screen
        View parent = (View) contentView.getParent();
        parent.setFitsSystemWindows(true);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(parent);
        contentView.measure(0, 0);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels- DisplayUtils.getStatusBarHeight(context);
        bottomSheetBehavior.setPeekHeight(screenHeight);
        if (params.getBehavior() instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) params.getBehavior()).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        params.height = screenHeight;
        parent.setLayoutParams(params);
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}
