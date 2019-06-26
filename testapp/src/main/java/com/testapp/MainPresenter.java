package com.testapp;

import android.content.Context;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpBasePresenter;

import java.util.concurrent.atomic.AtomicInteger;

public class MainPresenter extends MvpBasePresenter<MainState> implements MvpPresenter<MainState> {
    private final AtomicInteger lastEventId = new AtomicInteger();

    public MainPresenter(Context context, MainState state) {
        super(context, state);
    }

    @Override
    public void onTextChanged(int viewId, String text) {
        super.onTextChanged(viewId, text);
        state.setText(text);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onTextChanged (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    public void onViewClicked(int viewId) {
        super.onViewClicked(viewId);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onViewClicked (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    public void onItemSelected(int viewId, Object item) {
        super.onItemSelected(viewId, item);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onItemSelected (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    public void onCheckedChanged(int viewId, boolean isChecked) {
        super.onCheckedChanged(viewId, isChecked);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onCheckedChanged (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    public void onRadioCheckedChanged(int radioViewId, int viewId) {
        super.onRadioCheckedChanged(radioViewId, viewId);
        state.setOption(viewId);
    }
}
