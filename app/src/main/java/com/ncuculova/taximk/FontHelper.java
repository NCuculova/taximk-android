package com.ncuculova.taximk;

import android.content.Context;
import android.graphics.Typeface;

public class FontHelper {
    private static Typeface mSansCondensed;
    private static Typeface mSansBoldCondensed;
    private static Typeface mSansRegular;
    private static final Object lock = new Object();

    public static Typeface getSansCondensed(Context context, boolean bold) {
        synchronized (lock) {
            if (!bold && mSansCondensed == null)
                mSansCondensed = Typeface.createFromAsset(
                        context.getAssets(), "OpenSans-CondLight.ttf");
            else if (bold && mSansBoldCondensed == null)
                mSansBoldCondensed = Typeface.createFromAsset(
                        context.getAssets(), "OpenSans-CondBold.ttf");
        }
        return bold ? mSansBoldCondensed : mSansCondensed;
    }

    public static Typeface getSansRegular (Context context){
        synchronized (lock) {
            if (mSansRegular == null)
                mSansRegular = Typeface.createFromAsset(
                        context.getAssets(), "OpenSans-Regular.ttf");
        }
        return mSansRegular;
    }
}
