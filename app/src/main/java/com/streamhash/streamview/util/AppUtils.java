package com.streamhash.streamview.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;

import com.sensorberg.permissionbitte.BitteBitte;
import com.sensorberg.permissionbitte.PermissionBitte;
import com.streamhash.streamview.R;
import com.streamhash.streamview.ui.activity.BaseActivity;
import com.streamhash.streamview.ui.activity.SplashActivity;
import com.streamhash.streamview.util.UiUtils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtils {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private AppUtils() {

    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = new Date().getTime();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidCard(long number) {
        return (getSize(number) >= 13 &&
                getSize(number) <= 16) &&
                (prefixMatched(number, 4) ||
                        prefixMatched(number, 5) ||
                        prefixMatched(number, 37) ||
                        prefixMatched(number, 6)) &&
                ((sumOfDoubleEvenPlace(number) +
                        sumOfOddPlace(number)) % 10 == 0);
    }

    // Get the result from Step 2
    private static int sumOfDoubleEvenPlace(long number) {
        int sum = 0;
        String num = number + "";
        for (int i = getSize(number) - 2; i >= 0; i -= 2)
            sum += getDigit(Integer.parseInt(num.charAt(i) + "") * 2);

        return sum;
    }

    // Return this number if it is a single digit, otherwise,
    // return the sum of the two digits
    private static int getDigit(int number) {
        if (number < 9)
            return number;
        return number / 10 + number % 10;
    }

    // Return sum of odd-place digits in number
    private static int sumOfOddPlace(long number) {
        int sum = 0;
        String num = number + "";
        for (int i = getSize(number) - 1; i >= 0; i -= 2)
            sum += Integer.parseInt(num.charAt(i) + "");
        return sum;
    }

    // Return true if the digit d is a prefix for number
    private static boolean prefixMatched(long number, int d) {
        return getPrefix(number, getSize(d)) == d;
    }

    // Return the number of digits in d
    private static int getSize(long d) {
        String num = d + "";
        return num.length();
    }

    // Return the first k number of digits from
    // number. If the number of digits in number
    // is less than k, return number.
    private static long getPrefix(long number, int k) {
        if (getSize(number) > k) {
            String num = number + "";
            return Long.parseLong(num.substring(0, k));
        }
        return number;
    }

    public static String getYouTubeIdFromUrl(String youTubeUrl) {
        if (null == youTubeUrl)
            return "error";
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }

    public static void permissionCheck(Context context) {
        PermissionBitte.ask((BaseActivity) context, new BitteBitte() {
            @Override
            public void yesYouCan() {
            }

            @Override
            public void noYouCant() {
                new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Permissions were denied.")
                        .setMessage("Please grant them manually by going to Settings for the app to work")
                        .setPositiveButton("Ok", (dialog, which) -> PermissionBitte.goToSettings((BaseActivity) context))
                        .create().show();
            }

            @Override
            public void askNicer() {
                new AlertDialog.Builder(context)
                        .setTitle("Permissions")
                        .setMessage("You should grant us permissions for storage and camera for the app to function properly.")
                        .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                            //Ask permissions
                            dialog.dismiss();
                            permissionCheck(context);
                        })
                        .setNegativeButton(context.getString(R.string.no), ((dialog, which) -> {

                        }))
                        .setIcon(R.mipmap.ic_launcher)
                        .create().show();
            }
        });

    }
}
