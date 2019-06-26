/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.view.DragEvent;

/**
 * This interface describes generic MVP presenter
 *
 * @param <S> state type
 */
public interface MvpPresenter<S extends MvpState> {
    void attach(MvpView<S, ?> view);

    void detach(MvpView<S, ?> view);

    boolean isDetached();

    int getId();

    void commit();

    void finish();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    void onViewClicked(@IdRes int viewId);

    void onOptionsItemSelected(@IdRes int itemId);

    void onItemSelected(@IdRes int viewId, Object item);

    void onCheckedChanged(@IdRes int viewId, boolean isChecked);

    void onRadioCheckedChanged(@IdRes int radioViewId, @IdRes int viewId);

    void onTextChanged(@IdRes int viewId, String text);

    void onDrag(@IdRes int viewId, DragEvent event);
}
