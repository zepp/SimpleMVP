package com.testapp.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpBasePresenter;
import com.testapp.AppState;
import com.testapp.R;
import com.testapp.common.ActionDuration;
import com.testapp.common.Event;
import com.testapp.common.MyObjectBox;
import com.testapp.view.EventInfoDialog;
import com.testapp.view.SettingsDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.objectbox.Box;
import io.objectbox.BoxStore;

import static com.testapp.common.EventType.UI;

public class MainPresenterImpl extends MvpBasePresenter<MainState> implements MainPresenter {
    private final static int SELECT_FILE_CODE = 1;
    private final AppState appState;
    private final ConnectivityManager connectivityManager;
    private final BoxStore store;
    private final Box<Event> eventBox;
    private SimpleDateFormat format;
    private ScheduledFuture<?> timer;

    public MainPresenterImpl(Context context, MainState state) {
        super(context, state);
        appState = AppState.getInstance(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        store = MyObjectBox.builder().androidContext(context).build();
        eventBox = store.boxFor(Event.class);
    }

    private void recordEvent(Event event) {
        state.addEvent(event);
        eventBox.put(event);
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
    protected void onFirstViewConnected(@NonNull MvpViewHandle<MainState> handle, @NonNull Bundle arguments) throws Exception {
        super.onFirstViewConnected(handle, arguments);
        state.setEvents(eventBox.getAll());
        recordEvent(new Event(UI, "onFirstViewConnected", handle.getLayoutId()));
        state.setWriteGranted(ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onViewConnected(@NonNull MvpViewHandle<MainState> handle, @NonNull Bundle arguments) throws Exception {
        super.onViewConnected(handle, arguments);
        if (handle.getLayoutId() != R.layout.dialog_event_info) {
            recordEvent(new Event(UI, "onViewConnected", handle.getLayoutId()));
            commit();
        }
    }

    @Override
    protected void onViewsActive() throws Exception {
        super.onViewsActive();
        recordEvent(new Event(UI, "onViewsActive"));
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
        recordEvent(new Event(UI, "onViewsInactive"));
        commit(state.delay);
    }

    @Override
    protected void onLastViewDisconnected() throws Exception {
        super.onLastViewDisconnected();
        recordEvent(new Event(UI, "onLastViewDisconnected"));
        store.close();
    }

    @Override
    @MvpHandler
    public void onTextChanged(@NonNull MvpViewHandle<MainState> handle, int viewId, String text) {
        super.onTextChanged(handle, viewId, text);
        if (viewId == R.id.main_search) {
            state.setSearchPattern(text.toLowerCase());
        } else {
            recordEvent(new Event("onTextChanged", viewId));
            if (viewId == R.id.main_toast_text) {
                state.setText(text);
            } else if (viewId == R.id.main_expression) {
                state.setExpression(text.trim(), false);
            }
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onViewClicked(@NonNull MvpViewHandle<MainState> handle, int viewId) {
        super.onViewClicked(handle, viewId);
        if (viewId == R.id.clear_all) {
            state.clearEvents();
            eventBox.removeAll();
        } else {
            recordEvent(new Event("onViewClicked", viewId));
            if (viewId == R.id.main_show_toast) {
                handle.showToast(state.text, state.duration.getToastDuration());
            } else if (viewId == R.id.main_show_snackbar) {
                handle.showSnackBar(state.text, state.duration.getSnackBarDuration(),
                        getString(R.string.main_snackbar_action));
            } else if (viewId == R.id.main_eval) {
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
            } else if (viewId == R.id.main_request_permissions) {
                handle.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            } else if (viewId == R.id.main_select) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                handle.startActivityForResult(Intent.createChooser(intent, getString(R.string.main_selector_dialog_title)),
                        SELECT_FILE_CODE);
            }
        }
        commit(state.delay);
    }

    @MvpHandler
    @Override
    public void onActivityResult(@NonNull MvpViewHandle<MainState> handle, int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(handle, requestCode, resultCode, data);
        recordEvent(new Event("onActivityResult", handle.getLayoutId()));
        if (requestCode == SELECT_FILE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            state.setFileName(data.getData().getLastPathSegment());
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onItemSelected(@NonNull MvpViewHandle<MainState> handle, int viewId, @Nullable Object item) {
        super.onItemSelected(handle, viewId, item);
        if (viewId == R.id.event_delete) {
            if (item instanceof Event) {
                Event event = (Event) item;
                eventBox.remove(event.id);
                state.removeEvent(event);
            }
        } else if (viewId == R.id.event_layout) {
            if (item instanceof Event) {
                handle.showDialog(EventInfoDialog.newInstance(getId(), ((Event) item).id));
            }
        } else {
            recordEvent(new Event("onItemSelected", viewId));
            if (viewId == R.id.main_duration_spinner) {
                state.setDuration((ActionDuration) item);
            }
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onPositionChanged(@NonNull MvpViewHandle<MainState> handle, int viewId, int position) {
        super.onPositionChanged(handle, viewId, position);
        recordEvent(new Event("onPositionChanged", viewId));
        if (viewId == R.id.view_pager) {
            state.setCurrentPage(position);
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onCheckedChanged(@NonNull MvpViewHandle<MainState> handle, int viewId, boolean isChecked) {
        super.onCheckedChanged(handle, viewId, isChecked);
        recordEvent(new Event("onCheckedChanged", viewId));
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
    public void onProgressChanged(@NonNull MvpViewHandle<MainState> handle, int viewId, int progress) {
        super.onProgressChanged(handle, viewId, progress);
        recordEvent(new Event("onProgressChanged", viewId));
        if (viewId == R.id.settings_delay) {
            state.setDelay(progress * 100);
        }
        commit(state.delay);
    }

    @Override
    @MvpHandler
    public void onRequestPermissionsResult(@NonNull MvpViewHandle<MainState> handle, int requestCode, @NonNull Map<String, Integer> permissions) {
        super.onRequestPermissionsResult(handle, requestCode, permissions);
        recordEvent(new Event("onRequestPermissionsResult", handle.getLayoutId()));
        state.setWriteGranted(permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        commit(state.delay);
    }

    @Override
    @MvpHandler(executor = false)
    public void customHandler(MvpViewHandle<MainState> handle, int viewId) {
        recordEvent(new Event("customHandler", viewId));
        handle.showToast(R.string.main_invoked, Toast.LENGTH_SHORT);
        commit(state.delay);
    }

    @Override
    protected void onBroadcastReceived(@NonNull Intent intent, BroadcastReceiver.PendingResult result) throws Exception {
        super.onBroadcastReceived(intent, result);
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info == null) {
                recordEvent(new Event(intent.getAction(), "OFFLINE"));
            } else {
                recordEvent(new Event(intent.getAction(), info.getTypeName()));
            }
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            switch (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                case BatteryManager.BATTERY_PLUGGED_USB:
                    recordEvent(new Event(intent.getAction(), "USB"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_AC:
                    recordEvent(new Event(intent.getAction(), "AC"));
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    recordEvent(new Event(intent.getAction(), "WIRELESS"));
                    break;
                default:
                    recordEvent(new Event(intent.getAction(), "N/A"));
                    break;
            }
        }
        commit(state.delay);
    }
}
