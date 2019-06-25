package com.testapp;

import com.simplemvp.common.MvpState;

class MainState extends MvpState {
    String text = "";

    public void setText(String text) {
        setChanged(!this.text.equals(text));
        this.text = text;
    }
}
