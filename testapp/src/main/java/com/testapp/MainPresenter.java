package com.testapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.support.v4.content.ContextCompat;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpBasePresenter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.testapp.EventType.UI;

public class MainPresenter extends MvpBasePresenter<MainState> {
    private final AtomicInteger lastEventId = new AtomicInteger();
    private final ConnectivityManager connectivityManager;

    public MainPresenter(Context context, MainState state) {
        super(context, state);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onFirstViewConnected(MvpViewHandle<MainState> handle) throws Exception {
        super.onFirstViewConnected(handle);
        subscribeToBroadcast(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        subscribeToBroadcast(new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        state.addEvent(new Event(UI, lastEventId.incrementAndGet(), "onFirstViewConnected", handle.getLayoutId()));
        state.setWriteGranted(ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onViewConnected(MvpViewHandle<MainState> handle) throws Exception {
        super.onViewConnected(handle);
        state.addEvent(new Event(UI, lastEventId.incrementAndGet(), "onViewConnected", handle.getLayoutId()));
    }

    @Override
    @MvpHandler
    public void onTextChanged(MvpViewHandle<MainState> handle, int viewId, String text) {
        super.onTextChanged(handle, viewId, text);
        if (viewId == R.id.main_search) {
            state.setSearchPattern(text.toLowerCase());
        } else if (viewId == R.id.toast_text) {
            state.setText(text);
            state.addEvent(new Event(lastEventId.incrementAndGet(), "onTextChanged", viewId));
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onViewClicked(MvpViewHandle<MainState> handle, int viewId) {
        super.onViewClicked(handle, viewId);
        if (viewId == R.id.clear_all) {
            state.clearEvents();
        } else if (viewId == R.id.raise_error) {
            throw new RuntimeException("Runtime exception");
        } else {
            if (viewId == R.id.show_toast) {
                handle.showToast(state.text, state.duration.toastDuration);
            } else if (viewId == R.id.show_snackbar) {
                handle.showSnackBar(state.text, state.duration.snackBarDuration);
            }
            state.addEvent(new Event(lastEventId.incrementAndGet(), "onViewClicked", viewId));
        }
        commit(state.delay);
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
            state.addEvent(new Event(lastEventId.incrementAndGet(), "onItemSelected", viewId));
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onCheckedChanged(MvpViewHandle<MainState> handle, int viewId, boolean isChecked) {
        super.onCheckedChanged(handle, viewId, isChecked);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onCheckedChanged", viewId));
        if (viewId == R.id.settings_switch) {
            state.setSwitchChecked(isChecked);
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onRadioCheckedChanged(MvpViewHandle<MainState> handle, int radioViewId, int viewId) {
        super.onRadioCheckedChanged(handle, radioViewId, viewId);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onRadioCheckedChanged", radioViewId));
        state.setOption(viewId);
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onOptionsItemSelected(MvpViewHandle<MainState> handle, int itemId) {
        super.onOptionsItemSelected(handle, itemId);
        if (itemId == R.id.action_settings) {
            handle.showDialog(SettingsDialog.newInstance(getId()));
        }
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onOptionsItemSelected", itemId));
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onProgressChanged(MvpViewHandle<MainState> handle, int viewId, int progress) {
        super.onProgressChanged(handle, viewId, progress);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onProgressChanged", viewId));
        state.setDelay(progress * 100);
        commit(state.delay);
    }

    @Override
    public void onRequestPermissionsResult(MvpViewHandle<MainState> handle, int requestCode, Map<String, Integer> permissions) {
        super.onRequestPermissionsResult(handle, requestCode, permissions);
        state.addEvent(new Event(lastEventId.incrementAndGet(), "onRequestPermissionsResult", handle.getLayoutId()));
        state.setWriteGranted(permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        commit(state.delay);
    }

    @Override
    protected void onBroadcastReceived(Intent intent) {
        super.onBroadcastReceived(intent);
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info == null) {
                state.addEvent(new Event(lastEventId.incrementAndGet(),
                        intent.getAction(), "OFFLINE"));
            } else {
                state.addEvent(new Event(lastEventId.incrementAndGet(),
                        intent.getAction(), info.getTypeName()));
            }
            commit(state.delay);
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            switch (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                case BatteryManager.BATTERY_PLUGGED_USB:
                    state.addEvent(new Event(lastEventId.incrementAndGet(),
                            intent.getAction(), "USB power supply"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_AC:
                    state.addEvent(new Event(lastEventId.incrementAndGet(),
                            intent.getAction(), "AC power supply"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    state.addEvent(new Event(lastEventId.incrementAndGet(),
                            intent.getAction(), "Wireless power supply"));
                    break;
            }
            commit(state.delay);
        }
    }
}
