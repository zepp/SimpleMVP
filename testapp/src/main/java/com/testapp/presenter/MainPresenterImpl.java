package com.testapp.presenter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpBasePresenter;
import com.testapp.AppState;
import com.testapp.R;
import com.testapp.common.ActionDuration;
import com.testapp.common.Event;
import com.testapp.view.EventInfoDialog;
import com.testapp.view.SettingsDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.testapp.common.EventType.UI;

public class MainPresenterImpl extends MvpBasePresenter<MainState> implements MainPresenter {
    private final AppState appState;
    private final AtomicInteger lastEventId = new AtomicInteger();
    private final ConnectivityManager connectivityManager;
    private SimpleDateFormat format;
    private ScheduledFuture<?> timer;

    public MainPresenterImpl(Context context, MainState state) {
        super(context, state);
        appState = AppState.getInstance(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private int getEventId() {
        return lastEventId.incrementAndGet();
    }

    private ScheduledFuture<?> startTimer() {
        state.setStarted(true);
        return schedulePeriodic(() -> {
            state.incProgress();
            commit();
        }, 1, TimeUnit.SECONDS);
    }

    private void stopTimer(ScheduledFuture<?> timer) {
        state.setStarted(false);
        timer.cancel(false);
    }

    @Override
    protected void onFirstViewConnected(MvpViewHandle<MainState> handle) throws Exception {
        super.onFirstViewConnected(handle);
        state.addEvent(new Event(UI, getEventId(), "onFirstViewConnected", handle.getLayoutId()));
        state.setWriteGranted(ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onViewConnected(MvpViewHandle<MainState> handle) throws Exception {
        super.onViewConnected(handle);
        if (handle.getLayoutId() != R.layout.dialog_event_info) {
            state.addEvent(new Event(UI, getEventId(), "onViewConnected", handle.getLayoutId()));
            commit();
        }
    }

    @Override
    protected void onViewsActive() throws Exception {
        super.onViewsActive();
        state.addEvent(new Event(UI, getEventId(), "onViewsActive"));
        if (appState.isTimerStarted()) {
            state.setProgress((int) ((System.currentTimeMillis() - appState.getTimerStartedTime()) / 1000));
            if (timer == null) {
                timer = startTimer();
            }
        }
        format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        commit(state.delay);
    }

    @Override
    protected void onViewsInactive() throws Exception {
        super.onViewsInactive();
        state.addEvent(new Event(UI, getEventId(), "onViewsInactive"));
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onTextChanged(MvpViewHandle<MainState> handle, int viewId, String text) {
        super.onTextChanged(handle, viewId, text);
        if (viewId == R.id.main_search) {
            state.setSearchPattern(text.toLowerCase());
        } else {
            state.addEvent(new Event(getEventId(), "onTextChanged", viewId));
            if (viewId == R.id.toast_text) {
                state.setText(text);
            } else if (viewId == R.id.expression) {
                state.setExpression(text.trim(), false);
            }
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onViewClicked(MvpViewHandle<MainState> handle, int viewId) {
        super.onViewClicked(handle, viewId);
        if (viewId == R.id.clear_all) {
            state.clearEvents();
        } else {
            state.addEvent(new Event(getEventId(), "onViewClicked", viewId));
            if (viewId == R.id.show_toast) {
                handle.showToast(state.text, state.duration.getToastDuration());
            } else if (viewId == R.id.show_snackbar) {
                handle.showSnackBar(state.text, state.duration.getSnackBarDuration(),
                        getString(R.string.main_snackbar_action));
            } else if (viewId == R.id.eval) {
                state.setExpression(String.valueOf(new MathExpression(state.expression).evaluate()), true);
            } else if (viewId == R.id.action_settings) {
                handle.showDialog(SettingsDialog.newInstance(getId()));
            } else if (viewId == R.id.timer_start_stop) {
                if (timer == null || timer.isDone()) {
                    appState.setTimerStarted(true);
                    appState.setTimerStartedTime(System.currentTimeMillis());
                    state.setProgress(0);
                    timer = startTimer();
                } else {
                    appState.setTimerStarted(false);
                    stopTimer(timer);
                    handle.showSnackBar(getString(R.string.timer_started_at) +
                                    " " + format.format(new Date(appState.getTimerStartedTime())) +
                                    " " + getString(R.string.timer_duration) + " " + state.getTextProgress(),
                            Snackbar.LENGTH_LONG, getString(R.string.main_snackbar_action));
                }
            }
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onItemSelected(MvpViewHandle<MainState> handle, int viewId, Object item) {
        super.onItemSelected(handle, viewId, item);
        if (viewId == R.id.event_delete) {
            state.removeEvent((Event) item);
        } else if (viewId == R.id.event_layout) {
            handle.showDialog(EventInfoDialog.newInstance(getId(), ((Event) item).id));
        } else if (viewId == R.id.view_pager) {
            state.setCurrentPage((int) item);
        } else {
            state.addEvent(new Event(getEventId(), "onItemSelected", viewId));
            if (viewId == R.id.duration_spinner) {
                state.setDuration((ActionDuration) item);
            }
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onCheckedChanged(MvpViewHandle<MainState> handle, int viewId, boolean isChecked) {
        super.onCheckedChanged(handle, viewId, isChecked);
        state.addEvent(new Event(getEventId(), "onCheckedChanged", viewId));
        if (viewId == R.id.settings_connectivity) {
            if (!state.isSubscribedToConnectivity) {
                subscribeToBroadcast(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            }
            state.setSubscribedToConnectivity(isChecked);
        } else if (viewId == R.id.settings_power_supply) {
            if (!state.isSubscribedToPowerSupply) {
                subscribeToBroadcast(new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
            state.setSubscribedToPowerSupply(isChecked);
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onProgressChanged(MvpViewHandle<MainState> handle, int viewId, int progress) {
        super.onProgressChanged(handle, viewId, progress);
        state.addEvent(new Event(getEventId(), "onProgressChanged", viewId));
        state.setDelay(progress * 100);
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onRequestPermissionsResult(MvpViewHandle<MainState> handle, int requestCode, Map<String, Integer> permissions) {
        super.onRequestPermissionsResult(handle, requestCode, permissions);
        state.addEvent(new Event(getEventId(), "onRequestPermissionsResult", handle.getLayoutId()));
        state.setWriteGranted(permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        commit(state.delay);
    }

    @Override
    @MvpHandler(executor = false)
    public void customHandler(MvpViewHandle<MainState> handle, int viewId) {
        state.addEvent(new Event(getEventId(), "customHandler", viewId));
        handle.showToast(R.string.main_invoked, Toast.LENGTH_SHORT);
        commit(state.delay);
    }

    @Override
    protected void onBroadcastReceived(Intent intent, BroadcastReceiver.PendingResult result) throws Exception {
        super.onBroadcastReceived(intent, result);
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info == null) {
                state.addEvent(new Event(getEventId(), intent.getAction(), "OFFLINE"));
            } else {
                state.addEvent(new Event(getEventId(), intent.getAction(), info.getTypeName()));
            }
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            switch (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                case BatteryManager.BATTERY_PLUGGED_USB:
                    state.addEvent(new Event(getEventId(), intent.getAction(), "USB"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_AC:
                    state.addEvent(new Event(getEventId(), intent.getAction(), "AC"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    state.addEvent(new Event(getEventId(), intent.getAction(), "WIRELESS"));
                    break;
                default:
                    state.addEvent(new Event(getEventId(), intent.getAction(), "N/A"));
                    break;
            }
        }
        commit(state.delay);
    }

    @Override
    protected void afterCommit() {
        state.isEventAdded = false;
        state.isEvaluated = false;
    }
}
