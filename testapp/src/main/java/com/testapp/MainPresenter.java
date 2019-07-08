package com.testapp;

import android.content.Context;

import com.simplemvp.annotations.MvpEventHandler;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpBasePresenter;

import java.util.concurrent.atomic.AtomicInteger;

public class MainPresenter extends MvpBasePresenter<MainState> {
    private final AtomicInteger lastEventId = new AtomicInteger();

    public MainPresenter(Context context, MainState state) {
        super(context, state);
    }

    @Override
    @MvpEventHandler
    public void onTextChanged(MvpViewHandle<MainState> handle, int viewId, String text) {
        super.onTextChanged(handle, viewId, text);
        state.setText(text);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onTextChanged (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    @MvpEventHandler
    public void onViewClicked(MvpViewHandle<MainState> handle, int viewId) {
        super.onViewClicked(handle, viewId);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onViewClicked (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    @MvpEventHandler
    public void onItemSelected(MvpViewHandle<MainState> handle, int viewId, Object item) {
        super.onItemSelected(handle, viewId, item);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onItemSelected (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    @MvpEventHandler
    public void onCheckedChanged(MvpViewHandle<MainState> handle, int viewId, boolean isChecked) {
        super.onCheckedChanged(handle, viewId, isChecked);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onCheckedChanged (" + resources.getResourceName(viewId) + ")"));
        commit();
    }

    @Override
    @MvpEventHandler
    public void onRadioCheckedChanged(MvpViewHandle<MainState> handle, int radioViewId, int viewId) {
        super.onRadioCheckedChanged(handle, radioViewId, viewId);
        state.setOption(viewId);
    }
}
