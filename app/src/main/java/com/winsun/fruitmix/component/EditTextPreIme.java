package com.winsun.fruitmix.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Administrator on 2016/9/28.
 */

public class EditTextPreIme extends EditText {

    public EditTextPreIme(Context context) {
        super(context);
    }

    public EditTextPreIme(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public EditTextPreIme(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            clearFocus();

        }
        return super.dispatchKeyEventPreIme(event);
    }

}
