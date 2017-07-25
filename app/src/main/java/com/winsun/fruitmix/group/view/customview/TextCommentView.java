package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.winsun.fruitmix.databinding.TextCommentBinding;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.viewmodel.TextCommentViewModel;
import com.winsun.fruitmix.user.User;

/**
 * Created by Administrator on 2017/7/20.
 */

public class TextCommentView extends UserCommentView {

    private View view;

    @Override
    protected View generateContentView(Context context) {

        TextCommentBinding binding = TextCommentBinding.inflate(LayoutInflater.from(context), null, false);

        view = binding.getRoot();

        return view;
    }

    @Override
    protected void refreshContent(UserComment userComment, boolean isLeftMode) {

        TextComment textComment = (TextComment) userComment;

        TextCommentBinding binding = DataBindingUtil.findBinding(view);

        TextCommentViewModel viewModel = binding.getTextCommentViewModel();

        if (viewModel == null) {
            viewModel = new TextCommentViewModel();
            binding.setTextCommentViewModel(viewModel);
        }

        viewModel.isLeftMode.set(isLeftMode);
        viewModel.text.set(textComment.getText());

        binding.executePendingBindings();

    }
}
