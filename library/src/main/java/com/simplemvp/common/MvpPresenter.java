/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import java.util.Map;

/**
 * This interface describes generic MVP presenter
 *
 * @param <S> state type
 */
public interface MvpPresenter<S extends MvpState> {
    /**
     * This method is called by any {@link MvpView MvpView} implementation to connect with presenter
     * instance. Typically this method is called internally by {@link com.simplemvp.view.MvpFragment},
     * {@link com.simplemvp.view.MvpDialogFragment} or {@link com.simplemvp.view.MvpActivity}
     *
     * @param handle {@link MvpViewHandle} reference
     */
    void connect(MvpViewHandle<S> handle);

    /**
     * This method is called when {@link MvpView MvpView} is about to be closed. Presenter is no more
     * needed in other words. Typically it is called internally by view instance.
     *
     * @param handle {@link MvpViewHandle} reference
     */
    void disconnect(MvpViewHandle<S> handle);

    /**
     * This method disconnects view from presenter by layout ID.
     *
     * @param layoutId layout id of the connected view
     */
    void disconnect(@LayoutRes int layoutId);

    /**
     * Method to be used by {@link com.simplemvp.presenter.MvpPresenterManager MvpPresenterManager}
     * to release presenter instance if one has no attached views.
     *
     * @return true if there is no {@link MvpView MvpView} attached to current presenter
     */
    boolean isDetached();

    /**
     * This method return presenter ID. Presenter ID is just a number that sequently generated by the
     * library.
     *
     * @return presenter id
     */
    int getId();

    /**
     * This method terminates presenter and all one's attached views
     */
    void finish();

    /**
     * This method has the same purpose as the {@link android.app.Activity#onActivityResult(int, int, Intent)}
     *
     * @param handle {@link MvpViewHandle} interface that hides real view
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached
     */
    void onActivityResult(MvpViewHandle<S> handle, int requestCode, int resultCode, Intent data);

    /**
     * This method has the same purpose as the {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     *  @param handle {@link MvpViewHandle} interface that hides real view
     * @param requestCode  request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  map that maps permission type to permission result
     */
    void onRequestPermissionsResult(MvpViewHandle<S> handle, int requestCode, Map<String, Integer> permissions);

    /**
     * This method handles {@link android.view.View.OnClickListener#onClick(View)} callback.
     *
     * @param handle {@link MvpViewHandle} interface that hides real view
     * @param viewId view's ID
     */
    void onViewClicked(MvpViewHandle<S> handle, @IdRes int viewId);

    /**
     * This method has the same purpose as {@link android.app.Activity#onOptionsItemSelected(MenuItem)}
     *
     * @param handle {@link MvpViewHandle} interface that hides real view
     * @param itemId menu item ID
     */
    void onOptionsItemSelected(MvpViewHandle<S> handle, @IdRes int itemId);

    /**
     * This method handles {@link android.widget.AdapterView.OnItemSelectedListener} callbacks and also
     * can be used to process similar events from other views such as
     * {@link androidx.recyclerview.widget.RecyclerView RecyclerView}
     *
     * @param handle {@link MvpViewHandle} interface that hides real view
     * @param viewId view's ID
     * @param item   object
     */
    void onItemSelected(MvpViewHandle<S> handle, @IdRes int viewId, Object item);

    /**
     * This method handles {@link android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(CompoundButton, boolean)}
     * callback
     *
     * @param handle    {@link MvpViewHandle} interface that hides real view
     * @param viewId    view's ID
     * @param isChecked compound button state
     */
    void onCheckedChanged(MvpViewHandle<S> handle, @IdRes int viewId, boolean isChecked);

    /**
     * This method handles {@link android.widget.RadioGroup.OnCheckedChangeListener#check(int)}
     * callback
     *
     * @param handle      {@link MvpViewHandle} interface that hides real view
     * @param radioViewId {@link android.widget.RadioGroup} view instance ID
     * @param viewId      checked button ID
     */
    void onRadioCheckedChanged(MvpViewHandle<S> handle, @IdRes int radioViewId, @IdRes int viewId);

    /**
     * This method handles text change callbacks from {@link android.widget.EditText} or
     * {@link android.widget.SearchView} views
     *
     * @param handle {@link MvpViewHandle} interface that hides real view
     * @param viewId view's ID
     * @param text   new text
     */
    void onTextChanged(MvpViewHandle<S> handle, @IdRes int viewId, String text);

    /**
     * This method handles {@link View.OnDragListener#onDrag(View, DragEvent)} callback
     *
     * @param handle {@link MvpViewHandle} interface that hides real view
     * @param viewId view's ID
     * @param event  {@link DragEvent} event
     */
    void onDrag(MvpViewHandle<S> handle, @IdRes int viewId, DragEvent event);

    /**
     * This method handles events from SeekBar
     *
     * @param handle   {@link MvpViewHandle} interface that hides real view
     * @param viewId   view's ID
     * @param progress The current progress level. This will be in the range min..max where min
     *                 and max were set by {@link ProgressBar#setMin(int)} and
     *                 {@link ProgressBar#setMax(int)}, respectively. (The default values for
     *                 min is 0 and max is 100.)
     */
    void onProgressChanged(MvpViewHandle<S> handle, @IdRes int viewId, int progress);
}
