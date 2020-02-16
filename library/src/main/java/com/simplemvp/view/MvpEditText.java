/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class uses reflections to avoid watchers invocation when text is changed
 */
public class MvpEditText extends AppCompatEditText {
    private Field mListenersField;
    private List<TextWatcher> mListeners = Collections.emptyList();

    public MvpEditText(Context context) {
        super(context);
        initListenersField();
    }

    public MvpEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initListenersField();
    }

    public MvpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initListenersField();
    }

    private void initListenersField() {
        try {
            mListenersField = TextView.class.getDeclaredField("mListeners");
            mListenersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
    }

    public void setTextNoWatchers(String text) {
        try {
            mListeners = (List<TextWatcher>) mListenersField.get(this);
            mListenersField.set(this, new ArrayList<>());
            setText(text);
            setSelection(text == null || text.isEmpty() ? 0 : text.length());
            mListenersField.set(this, mListeners);
        } catch (IllegalAccessException e) {
        }
    }
}
