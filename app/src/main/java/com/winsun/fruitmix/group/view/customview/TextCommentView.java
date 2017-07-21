package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.winsun.fruitmix.group.data.model.TextComment;

/**
 * Created by Administrator on 2017/7/20.
 */

public class TextCommentView extends UserCommentView<TextComment> {

    private TextView textView;

    @Override
    public View generateContentView(Context context) {

        textView = new TextView(context);

        return textView;
    }

    @Override
    public void refreshContent(TextComment data) {

        textView.setText(data.getText());
    }
}
