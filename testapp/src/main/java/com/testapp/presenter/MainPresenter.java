/*
 * Copyright (c) 2020 Pavel A. Sokolov
 */

package com.testapp.presenter;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpViewHandle;

public interface MainPresenter extends MvpPresenter<MainState> {
    void customHandler(MvpViewHandle<MainState> handle, int viewId);
}
