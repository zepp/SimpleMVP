package com.simplemvp.view;

import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.widget.SearchView;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements {@link android.support.v7.widget.SearchView.OnQueryTextListener OnQueryTextListener}
 * interface to handle events from {@link android.support.v7.widget.SearchView SearchView} view.
 * It invokes {@link MvpPresenter#onTextChanged onTextChanged} method after a short delay. Delay
 * should prevent frequent method invocation if user pressed a button and holds it for period of time.
 *
 * @param <S> state type
 */
class MvpOnQueryTextListener<S extends MvpState> implements SearchView.OnQueryTextListener {
    private final static int DELAY = 200;
    private final Handler handler;
    private final MvpPresenter<S> presenter;
    @IdRes
    private final int viewId;
    // provides ability to set flag in handy manner
    private final AtomicBoolean isSendTextPosted = new AtomicBoolean();
    private String text = "";
    private long millis;

    MvpOnQueryTextListener(Handler handler, MvpPresenter<S> presenter, int viewId) {
        this.handler = handler;
        this.presenter = presenter;
        this.viewId = viewId;
    }

    private void sendText() {
        long delta = System.currentTimeMillis() - millis;
        if (delta > DELAY) {
            presenter.onTextChanged(viewId, text);
            isSendTextPosted.set(false);
        } else {
            handler.postDelayed(this::sendText, DELAY - delta);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        millis = System.currentTimeMillis();
        text = s;
        if (isSendTextPosted.compareAndSet(false, true)) {
            handler.postDelayed(this::sendText, DELAY);
        }
        // false if the SearchView should perform the default action of showing any suggestions
        return false;
    }
}
