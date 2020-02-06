package com.simplemvp.view;

import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

/**
 * This class implements {@link android.text.TextWatcher TextWatcher} interface to handle events from
 * {@link android.widget.EditText EditText} view and so on.
 * It invokes {@link MvpPresenter#onTextChanged onTextChanged} method after a short delay. Delay
 * should prevent frequent method invocation if user pressed backspace to remove all input for example.
 *
 * @param <S> state type
 */
class MvpTextWatcher<S extends MvpState> implements TextWatcher {
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    @IdRes
    private final int viewId;

    MvpTextWatcher(MvpViewHandle<S> handle, MvpPresenter<S> presenter, int viewId) {
        this.handle = handle;
        this.presenter = presenter;
        this.viewId = viewId;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        presenter.onTextChanged(handle, viewId, s.toString());
    }
}
