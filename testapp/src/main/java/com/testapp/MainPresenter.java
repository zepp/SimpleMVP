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
        state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onTextChanged"));
        commit();
    }

    @Override
    @MvpEventHandler
    public void onViewClicked(MvpViewHandle<MainState> handle, int viewId) {
        super.onViewClicked(handle, viewId);
        if (viewId == R.id.clear_all) {
            state.clearEvents();
        } else {
            if (viewId == R.id.show_toast) {
                handle.showToast(state.text, state.duration.duration);
            }
            state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onViewClicked"));
        }
        commit();
    }

    @Override
    @MvpEventHandler
    public void onItemSelected(MvpViewHandle<MainState> handle, int viewId, Object item) {
        super.onItemSelected(handle, viewId, item);
        if (viewId == R.id.events) {
            state.removeEvent((Event) item);
        } else {
            if (viewId == R.id.duration_spinner) {
                state.setDuration((ToastDuration) item);
            }
            state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onItemSelected"));
        }
        commit();
    }

    @Override
    @MvpEventHandler
    public void onCheckedChanged(MvpViewHandle<MainState> handle, int viewId, boolean isChecked) {
        super.onCheckedChanged(handle, viewId, isChecked);
        state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onCheckedChanged"));
        if (viewId == R.id.settings_switch) {
            state.setSwitchChecked(isChecked);
        }
        commit();
    }

    @Override
    @MvpEventHandler
    public void onRadioCheckedChanged(MvpViewHandle<MainState> handle, int radioViewId, int viewId) {
        super.onRadioCheckedChanged(handle, radioViewId, viewId);
        state.setOption(viewId);
    }

    @Override
    public void onOptionsItemSelected(MvpViewHandle<MainState> handle, int itemId) {
        super.onOptionsItemSelected(handle, itemId);
        if (itemId == R.id.action_settings) {
            handle.showDialog(SettingsDialog.newInstance(getId()));
        }
        state.addEvent(new Event(lastEventId.incrementAndGet(), itemId, "onOptionsItemSelected"));
        commit();
    }
}
