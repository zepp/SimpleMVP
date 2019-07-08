package com.simplemvp.view;

import android.os.Handler;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class implements {@link android.text.TextWatcher TextWatcher} interface to handle events from
 * {@link android.widget.EditText EditText} view and so on.
 * It invokes {@link MvpPresenter#onTextChanged onTextChanged} method after a short delay. Delay
 * should prevent frequent method invocation if user pressed backspace to remove all input for example.
 *
 * @param <S> state type
 */
class MvpTextWatcher<S extends MvpState> implements TextWatcher {
    private final static int DELAY = 200;
    private final Handler handler;
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    @IdRes
    private final int viewId;
    // provides ability to set flag in handy manner
    private final AtomicBoolean isSendTextPosted = new AtomicBoolean();
    private String text = "";
    private long millis;

    MvpTextWatcher(Handler handler, MvpViewHandle<S> handle, MvpPresenter<S> presenter, int viewId) {
        this.handler = handler;
        this.handle = handle;
        this.presenter = presenter;
        this.viewId = viewId;
    }

    private void sendText() {
        long delta = System.currentTimeMillis() - millis;
        if (delta > DELAY) {
            presenter.onTextChanged(handle, viewId, text);
            isSendTextPosted.set(false);
        } else {
            handler.postDelayed(this::sendText, DELAY - delta);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        millis = System.currentTimeMillis();
        text = s.toString();
        if (isSendTextPosted.compareAndSet(false, true)) {
            handler.postDelayed(this::sendText, DELAY);
        }
    }
}
