/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.content.Intent;
import android.view.DragEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.simplemvp.presenter.MvpBasePresenter;

import java.util.Map;

/**
 * This interface describes generic MVP presenter
 *
 * @param <S> state type
 */
public interface MvpPresenter<S extends MvpState> {
    /**
     * This method is called by a {@link MvpView} implementation to connect to a presenter instance.
     * {@link MvpBasePresenter#onFirstViewConnected(MvpViewHandle, android.os.Bundle)} and
     * {@link MvpBasePresenter#onViewConnected(MvpViewHandle, android.os.Bundle)}
     * are submitted for execution after this method call by {@link MvpBasePresenter}.
     * {@link com.simplemvp.view.MvpFragment}, {@link com.simplemvp.view.MvpDialogFragment} and
     * {@link com.simplemvp.view.MvpActivity} call this method internally.
     *
     * @param view {@link MvpView} instance
     */
    void connect(@NonNull MvpView<S, ?> view);

    /**
     * This method is called by a {@link MvpView} implementation when one is about to be destroyed.
     * {@link MvpBasePresenter#onLastViewDisconnected()} method is submitted for execution in case
     * of it is a last connected view.
     * {@link com.simplemvp.view.MvpFragment}, {@link com.simplemvp.view.MvpDialogFragment} and
     * {@link com.simplemvp.view.MvpActivity} call this method internally.
     *
     * @param view {@link MvpView} instance
     */
    void disconnect(@NonNull MvpView<S, ?> view);

    /**
     * This method disconnects view from presenter by ID. See {@link MvpView#getMvpId()} for details.
     * It is used by library internals in case of view does not disconnect itself properly.
     *
     * @param id view's ID
     */
    void disconnect(int id);

    /**
     * This method checks that presenter has no connected views. It is used internally by
     * {@link com.simplemvp.presenter.MvpBasePresenter} and by {@link com.simplemvp.presenter.MvpPresenterManager}.
     *
     * @return true if there is no {@link MvpView MvpView} connected to the current presenter
     */
    boolean isDisconnected();

    /**
     * This method return presenter ID. Presenter ID is just a number that is assigned to presenter
     * during construction.
     *
     * @return presenter id
     */
    int getId();

    /**
     * This method tries to finish all connected views to release presenter. Actually it should
     * implicitly call {@link MvpView#finish()} of all connected views.
     */
    void finish();

    /**
     * This method has the same purpose as the {@link com.simplemvp.view.MvpActivity#onActivityResult(int, int, Intent)}
     *
     * @param handle {@link MvpViewHandle} instance
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached
     */
    void onActivityResult(@NonNull MvpViewHandle<S> handle, int requestCode, int resultCode, Intent data);

    /**
     * This method has the same purpose as the {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     * @param handle {@link MvpViewHandle} instance
     * @param requestCode request code passed in {@link com.simplemvp.view.MvpFragment#requestPermissions(String[], int)} or
     *                    {@link com.simplemvp.view.MvpActivity#requestPermissions(String[], int)}
     * @param permissions map that represents result for the certain permission
     */
    void onRequestPermissionsResult(@NonNull MvpViewHandle<S> handle, int requestCode, @NonNull Map<String, Integer> permissions);

    /**
     * This method handles {@link View} clicks and {@link android.view.MenuItem} selection.
     * See {@link com.simplemvp.view.MvpClickListener} and {@link MvpListener} for details
     * Use {@link MvpView#getMvpListener()} or {@link MvpView#newMvpClickListener(boolean)} to
     * connect certain view to the handler.
     *
     * @param handle {@link MvpViewHandle} instance
     * @param viewId view's ID
     */
    void onViewClicked(@NonNull MvpViewHandle<S> handle, @IdRes int viewId);

    /**
     * This method handles item selection of {@link android.widget.AdapterView} or any other suitable
     * view like a {@link androidx.recyclerview.widget.RecyclerView} but in this case you have to invoke
     * {@link MvpPresenter#onItemSelected(MvpViewHandle, int, Object)} manually.
     * See {@link MvpListener} for details. Use {@link MvpView#getMvpListener()} to connect certain
     * view to the handler.
     *
     * @param handle {@link MvpViewHandle} instance
     * @param viewId view's ID
     * @param item selected item
     */
    void onItemSelected(MvpViewHandle<S> handle, @IdRes int viewId, Object item);

    /**
     * This method handles check selection of {@link android.widget.CompoundButton}.
     * See {@link MvpListener} for details. Use {@link MvpView#getMvpListener()} to connect certain
     * view to the handler.
     *
     * @param handle    {@link MvpViewHandle} interface
     * @param viewId    view's ID
     * @param isChecked check state
     */
    void onCheckedChanged(@NonNull MvpViewHandle<S> handle, @IdRes int viewId, boolean isChecked);

    /**
     * This method handles item selection of {@link android.widget.RadioGroup}.
     * See {@link MvpListener} for details. Use {@link MvpView#getMvpListener()} to connect certain
     * view to the handler.
     *
     * @param handle      {@link MvpViewHandle} interface that hides real view
     * @param radioViewId {@link android.widget.RadioGroup} view instance ID
     * @param viewId      checked button ID
     */
    void onRadioCheckedChanged(@NonNull MvpViewHandle<S> handle, @IdRes int radioViewId, @IdRes int viewId);

    /**
     * This method handles text changes of {@link android.widget.EditText} or
     * {@link android.widget.SearchView} views.
     * Use {@link MvpView#newTextWatcher(EditText)}} or {@link MvpView#newQueryTextListener(SearchView)}
     * to connect certain view to the handler.
     *
     * @param handle {@link MvpViewHandle} instance
     * @param viewId view's ID
     * @param text   new text
     */
    void onTextChanged(@NonNull MvpViewHandle<S> handle, @IdRes int viewId, String text);

    /**
     * This method handles {@link View.OnDragListener#onDrag(View, DragEvent)} callback.
     * See {@link MvpListener} for details. Use {@link MvpView#getMvpListener()} to connect certain
     * view to the handler.
     *
     * @param handle {@link MvpViewHandle} instance
     * @param viewId view's ID
     * @param event  {@link DragEvent} instance
     */
    void onDrag(@NonNull MvpViewHandle<S> handle, @IdRes int viewId, DragEvent event);

    /**
     * This method handles events of {@link android.widget.SeekBar}
     * See {@link MvpListener} for details. Use {@link MvpView#getMvpListener()} to connect certain
     * view to the handler.
     *
     * @param handle   {@link MvpViewHandle} instance
     * @param viewId   view's ID
     * @param progress The current progress level. This will be in the range min..max where min
     *                 and max were set by {@link ProgressBar#setMin(int)} and
     *                 {@link ProgressBar#setMax(int)}, respectively. (The default values for
     *                 min is 0 and max is 100.)
     */
    void onProgressChanged(@NonNull MvpViewHandle<S> handle, @IdRes int viewId, int progress);
}
