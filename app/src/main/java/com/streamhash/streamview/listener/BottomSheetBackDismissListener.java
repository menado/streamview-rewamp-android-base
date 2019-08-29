package com.streamhash.streamview.listener;

import android.content.DialogInterface;
import android.os.Handler;
import android.view.KeyEvent;


public class BottomSheetBackDismissListener implements DialogInterface.OnKeyListener {

    private static boolean justTouched = false;

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!justTouched) {
                dialog.dismiss();
                justTouched = true;
                new Handler().postDelayed(() -> justTouched = false, 500);
            }
            return true;
        }
        return false;
    }
}
