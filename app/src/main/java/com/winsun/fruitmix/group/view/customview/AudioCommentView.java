package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.AudioCommentItemBinding;
import com.winsun.fruitmix.group.data.model.AudioComment;
import com.winsun.fruitmix.group.data.model.RetryFailUserCommentStrategy;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.group.usecase.PlayAudioUseCase;
import com.winsun.fruitmix.util.Util;

/**
 * Created by Administrator on 2017/8/14.
 */

public class AudioCommentView extends UserCommentView {

    private AudioCommentItemBinding binding;

    private TextView audioTimeTextView;
    private ImageView audioImg;

    private PlayAudioUseCase playAudioUseCase;

    private boolean isPlaying = false;

    public AudioCommentView(RetryFailUserCommentStrategy retryFailUserCommentStrategy,PlayAudioUseCase playAudioUseCase) {
        super(retryFailUserCommentStrategy);
        this.playAudioUseCase = playAudioUseCase;
    }

    @Override
    protected View generateContentView(Context context, ViewGroup parent) {

        binding = AudioCommentItemBinding.inflate(LayoutInflater.from(context), parent, false);

        audioTimeTextView = new TextView(context);
        audioTimeTextView.setTextColor(ContextCompat.getColor(context, R.color.thirty_eight_percent_black));
        audioTimeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,Util.dip2px(context, 12));

        audioImg = new ImageView(context);

        audioImg.setImageResource(R.drawable.ic_volume_up_black_24dp);

        return binding.getRoot();
    }

    @Override
    protected void refreshContent(Context context, View toolbar,UserComment data, boolean isLeftModel) {

        LinearLayout audioCommentContainer = binding.autoCommentContainer;

        if (isLeftModel) {

            audioCommentContainer.addView(audioImg);
            audioCommentContainer.addView(audioTimeTextView);

            Util.setRightMargin(audioImg,Util.dip2px(context,4));


        } else {

            audioCommentContainer.addView(audioTimeTextView);
            audioCommentContainer.addView(audioImg);

            Util.setLeftMargin(audioImg,Util.dip2px(context,4));

        }

        final AudioComment audioComment = (AudioComment) data;

        audioTimeTextView.setText(String.valueOf(audioComment.getAudioRecordTime()));

        audioCommentContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPlaying)
                    playAudioUseCase.stopPlayAudio();
                else
                    playAudioUseCase.startPlayAudio(audioComment.getAudioRecordFilePath());

            }
        });


    }
}
