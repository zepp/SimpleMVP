package com.simplemvp.common;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

public interface MvpListener extends View.OnClickListener, MenuItem.OnMenuItemClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener, View.OnDragListener, SeekBar.OnSeekBarChangeListener {
}
