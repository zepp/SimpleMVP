package com.testapp;

import android.content.Context;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpBasePresenter;

public class MainPresenter extends MvpBasePresenter<MainState> implements MvpPresenter<MainState> {
    public MainPresenter(Context context, MainState state) {
        super(context, state);
    }

    @Override
    public void onTextChanged(int viewId, String text) {
        super.onTextChanged(viewId, text);
        state.setText(text);
    }
}
