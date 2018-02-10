package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.TextCommentBinding;
import com.winsun.fruitmix.group.data.model.TextComment;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.data.model.UserCommentShowStrategy;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.source.InjectGroupDataSource;
import com.winsun.fruitmix.group.data.viewmodel.TextCommentViewModel;
import com.winsun.fruitmix.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/7/20.
 */

public class TextCommentView extends UserCommentView {

    private TextCommentBinding binding;

    @Override
    protected View generateContentView(Context context, ViewGroup parent) {

        binding = TextCommentBinding.inflate(LayoutInflater.from(context), parent, false);

        return binding.getRoot();

    }

    @Override
    protected void refreshContent(Context context, View toolbar, UserComment userComment, boolean isLeftModel) {

        TextComment textComment = (TextComment) userComment;

        TextCommentViewModel viewModel = binding.getTextCommentViewModel();

        if (viewModel == null) {
            viewModel = new TextCommentViewModel();
            binding.setTextCommentViewModel(viewModel);
        }

        viewModel.isLeftMode.set(isLeftModel);
        viewModel.text.set(textComment.getText());

        binding.executePendingBindings();

    }




}
