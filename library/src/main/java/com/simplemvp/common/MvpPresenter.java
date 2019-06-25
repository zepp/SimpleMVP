/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.content.Intent;
import android.support.annotation.IdRes;

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

    void onViewClicked(@IdRes int viewId);

    void onOptionsItemSelected(@IdRes int itemId);

    void onItemSelected(@IdRes int viewId, Object item);

    void onTextChanged(@IdRes int viewId, String text);
}
