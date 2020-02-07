package com.testapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpBasePresenter;

import java.util.concurrent.atomic.AtomicInteger;

public class MainPresenter extends MvpBasePresenter<MainState> {
    private final AtomicInteger lastEventId = new AtomicInteger();
    private final ConnectivityManager connectivityManager;

    public MainPresenter(Context context, MainState state) {
        super(context, state);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onFirstViewConnected(MvpViewHandle<MainState> handle) {
        super.onFirstViewConnected(handle);
        subscribeToBroadcast(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        subscribeToBroadcast(new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onViewConnected(MvpViewHandle<MainState> handle) {
        super.onViewConnected(handle);
        MvpView<MainState, ?> view = handle.getMvpView();
        state.addEvent(new Event(lastEventId.incrementAndGet(), view == null ? 0 : view.getLayoutId(),
                "onViewConnected"));
        commit();
    }

    @Override
    @MvpHandler
    public void onTextChanged(MvpViewHandle<MainState> handle, int viewId, String text) {
        super.onTextChanged(handle, viewId, text);
        state.setText(text);
        state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onTextChanged"));
        commit();
    }

    @Override
    @MvpHandler
    public void onViewClicked(MvpViewHandle<MainState> handle, int viewId) {
        super.onViewClicked(handle, viewId);
        if (viewId == R.id.clear_all) {
            state.clearEvents();
        } else {
            if (viewId == R.id.show_toast) {
                handle.showToast(state.text, state.duration.toastDuration);
            } else if (viewId == R.id.show_snackbar) {
                handle.showSnackBar(state.text, state.duration.snackBarDuration);
            }
            state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onViewClicked"));
        }
        commit();
    }

    @Override
    @MvpHandler
    public void onItemSelected(MvpViewHandle<MainState> handle, int viewId, Object item) {
        super.onItemSelected(handle, viewId, item);
        if (viewId == R.id.events) {
            state.removeEvent((Event) item);
        } else {
            if (viewId == R.id.duration_spinner) {
                state.setDuration((ActionDuration) item);
            }
            state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onItemSelected"));
        }
        commit();
    }

    @Override
    @MvpHandler
    public void onCheckedChanged(MvpViewHandle<MainState> handle, int viewId, boolean isChecked) {
        super.onCheckedChanged(handle, viewId, isChecked);
        state.addEvent(new Event(lastEventId.incrementAndGet(), viewId, "onCheckedChanged"));
        if (viewId == R.id.settings_switch) {
            state.setSwitchChecked(isChecked);
        }
        commit();
    }

    @Override
    @MvpHandler
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

    @Override
    protected void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info == null) {
                state.addEvent(new Event(lastEventId.incrementAndGet(), 0, "network is unavailable"));
            } else {
                state.addEvent(new Event(lastEventId.incrementAndGet(), 0, "network: " + info.getTypeName()));
            }
            commit();
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            switch (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                case BatteryManager.BATTERY_PLUGGED_USB:
                    state.addEvent(new Event(lastEventId.incrementAndGet(), 0, "USB is plugged"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_AC:
                    state.addEvent(new Event(lastEventId.incrementAndGet(), 0, "AC power supply is plugged"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    state.addEvent(new Event(lastEventId.incrementAndGet(), 0, "Wireless power supply is plugged"));
                    break;
            }
            commit();
        }
    }
}
