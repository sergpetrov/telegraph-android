package com.telex.base.utils;

import android.os.Parcel;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpannableStringReplacer {
    private final CharSequence mSource;
    private final CharSequence mReplacement;
    private final Matcher mMatcher;
    private int mAppendPosition;
    private final boolean mIsSpannable;

    public static CharSequence replace(CharSequence source, String regex,
                                       CharSequence replacement) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        return new SpannableStringReplacer(source, matcher, replacement).doReplace();
    }

    private SpannableStringReplacer(CharSequence source, Matcher matcher,
                                    CharSequence replacement) {
        mSource = source;
        mReplacement = replacement;
        mMatcher = matcher;
        mAppendPosition = 0;
        mIsSpannable = replacement instanceof Spannable;
    }

    private CharSequence doReplace() {
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        while (mMatcher.find()) {
            appendReplacement(buffer);
        }
        return appendTail(buffer);
    }

    private void appendReplacement(SpannableStringBuilder buffer) {
        buffer.append(mSource.subSequence(mAppendPosition, mMatcher.start()));
        CharSequence replacement = mIsSpannable
                ? copyCharSequenceWithSpans(mReplacement)
                : mReplacement;
        buffer.append(replacement);

        mAppendPosition = mMatcher.end();
    }

    public SpannableStringBuilder appendTail(SpannableStringBuilder buffer) {
        buffer.append(mSource.subSequence(mAppendPosition, mSource.length()));
        return buffer;
    }

    // This is a weird way of copying spans, but I don't know any better way.
    private CharSequence copyCharSequenceWithSpans(CharSequence string) {
        Parcel parcel = Parcel.obtain();
        try {
            TextUtils.writeToParcel(string, parcel, 0);
            parcel.setDataPosition(0);
            return TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        } finally {
            parcel.recycle();
        }
    }
}
