package com.nxt.nxtvault.util;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 *  on 4/19/2015.
 */
public class FontUtils {
    private static Typeface normal;

    private static Typeface bold;

    private static Typeface condensed;

    private static Typeface light;

    private static void processsViewGroup(ViewGroup v, final int len) {

        for (int i = 0; i < len; i++) {
            final View c = v.getChildAt(i);
            if (c instanceof TextView) {
                setCustomFont((TextView) c);
            } else if (c instanceof ViewGroup) {
                setCustomFont((ViewGroup) c);
            }
        }
    }

    private static void setCustomFont(TextView c) {
        Object tag = c.getTag();
        if (tag instanceof String) {
            if (((String) tag).contains("bold")) {
                c.setTypeface(bold);
                return;
            }
            if (((String) tag).contains("condensed")) {
                c.setTypeface(condensed);
                return;
            }
            if (((String) tag).contains("light")) {
                c.setTypeface(light);
                return;
            }
        }
        c.setTypeface(normal);
    }

    public static void setCustomFont(View topView, AssetManager assetsManager) {
        if (normal == null || bold == null || condensed == null || light == null) {
            normal = Typeface.createFromAsset(assetsManager, "fonts/segoeui.ttf");
            bold = Typeface.createFromAsset(assetsManager, "fonts/segouib.ttf");
            condensed = Typeface.createFromAsset(assetsManager, "fonts/segoeui.ttf");
            light = Typeface.createFromAsset(assetsManager, "fonts/segoeui;.ttf");
        }

        if (topView instanceof ViewGroup) {
            setCustomFont((ViewGroup) topView);
        } else if (topView instanceof TextView) {
            setCustomFont((TextView) topView);
        }
    }

    private static void setCustomFont(ViewGroup v) {
        final int len = v.getChildCount();
        processsViewGroup(v, len);
    }
}