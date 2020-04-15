/*
 * Copyright (c) 2020 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This interface extends a lot of standard Android listeners to define universal listener to be
 * accepted by different {@link View} implementations.
 */
public interface MvpListener extends View.OnClickListener, MenuItem.OnMenuItemClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener, View.OnDragListener, SeekBar.OnSeekBarChangeListener,
        TextView.OnEditorActionListener {
}
