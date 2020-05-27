/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.CallSuper;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * This class uses reflections to avoid watchers invocation when text is changed
 */
public class MvpEditText extends AppCompatEditText {
    private TextWatcher mvpTextWatcher;

    public MvpEditText(Context context) {
        super(context);
    }

    public MvpEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MvpEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @CallSuper
    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        super.addTextChangedListener(watcher);
        if (watcher instanceof MvpTextWatcher && !watcher.equals(mvpTextWatcher)) {
            mvpTextWatcher = watcher;
        }
    }

    public void setTextAvoidMvpWatcher(String text) {
        if (mvpTextWatcher != null) {
            removeTextChangedListener(mvpTextWatcher);
        }
        setText(text);
        setSelection(text == null || text.isEmpty() ? 0 : text.length());
        if (mvpTextWatcher != null) {
            addTextChangedListener(mvpTextWatcher);
        }
    }
}
