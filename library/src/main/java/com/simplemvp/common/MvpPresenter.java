/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.content.Intent;
import android.support.annotation.IdRes;

import com.simplemvp.annotations.Handling;

/**
 * This interface describes generic MVP presenter
 *
 * @param <S> state type
 */
public interface MvpPresenter<S extends MvpState> {
    @Handling(offload = false)
    void attach(MvpView<S, ?> view);

    @Handling(offload = false)
    void detach(MvpView<S, ?> view);

    @Handling(offload = false)
    boolean isDetached();

    @Handling(offload = false)
    int getId();

    void commit();

    void finish();

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);

    void onViewClicked(@IdRes int viewId);

    void onOptionsItemSelected(@IdRes int itemId);

    void onItemSelected(@IdRes int viewId, Object item);

    void onTextChanged(@IdRes int viewId, String text);
}
