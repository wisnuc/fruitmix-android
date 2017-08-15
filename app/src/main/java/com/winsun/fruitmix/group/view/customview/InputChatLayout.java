package com.winsun.fruitmix.group.view.customview;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.InputChatLayoutBinding;
import com.winsun.fruitmix.group.data.viewmodel.InputChatMenuViewModel;
import com.winsun.fruitmix.group.data.viewmodel.InputChatViewModel;
import com.winsun.fruitmix.group.usecase.RecordAudioUseCase;
import com.winsun.fruitmix.group.usecase.RecordAudioUseCaseImpl;
import com.winsun.fruitmix.group.usecase.InputChatMenuUseCase;

/**
 * Created by Administrator on 2017/8/7.
 */

public class InputChatLayout extends FrameLayout implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener {

    private EditText editText;

    private String inputText = "";

    private InputChatViewModel inputChatViewModel;

    private SendTextChatListener sendTextChatListener;
    private EditTextFocusChangeListener editTextFocusChangeListener;
    private ChatLayoutOnClickListener chatLayoutOnClickListener;

    private SendAudioChatListener sendAudioChatListener;

    private InputChatMenuViewModel inputChatMenuViewModel;

    private InputChatLayoutBinding binding;

    private AddBtnOnClickListener addBtnOnClickListener;

    private RecordAudioUseCase recordAudioUseCase;

    private boolean isRecordingAudio = false;

    public InputChatLayout(Context context) {
        super(context);

        init(context);
    }

    public InputChatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public InputChatLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {

        binding = InputChatLayoutBinding.inflate(LayoutInflater.from(context), null, false);

        addView(binding.getRoot());

        inputChatViewModel = new InputChatViewModel();

        binding.setInputChatViewModel(inputChatViewModel);

        inputChatMenuViewModel = new InputChatMenuViewModel();

        binding.setInputChatMenuViewModel(inputChatMenuViewModel);

        editText = binding.editText;
        editText.clearFocus();

        editText.addTextChangedListener(this);

        editText.setOnFocusChangeListener(this);

        editText.setOnClickListener(this);

        Button button = binding.sendButton;

        button.setOnClickListener(this);

        binding.addVoice.setOnClickListener(this);

        binding.addButton.setOnClickListener(this);

        binding.addEmoticon.setOnClickListener(this);

        binding.addVoiceBtn.setOnClickListener(this);

        recordAudioUseCase = RecordAudioUseCaseImpl.getInstance();

    }

    public void setInputChatMenuUseCase(final InputChatMenuUseCase inputChatMenuUseCase) {

        InputChatMenuUseCase useCase = new InputChatMenuUseCase() {
            @Override
            public void sendPhotoChat() {

                inputChatMenuUseCase.sendPhotoChat();

                inputChatMenuViewModel.showChatMenu.set(false);

            }

            @Override
            public void sendFileChat() {

                inputChatMenuUseCase.sendFileChat();

                inputChatMenuViewModel.showChatMenu.set(false);
            }
        };

        binding.setInputChatMenuUseCase(useCase);

    }

    public void setAddBtnOnClickListener(AddBtnOnClickListener addBtnOnClickListener) {
        this.addBtnOnClickListener = addBtnOnClickListener;
    }

    public void setEditTextFocusChangeListener(EditTextFocusChangeListener editTextFocusChangeListener) {
        this.editTextFocusChangeListener = editTextFocusChangeListener;
    }

    public void setSendTextChatListener(SendTextChatListener sendTextChatListener) {
        this.sendTextChatListener = sendTextChatListener;
    }

    public void setSendAudioChatListener(SendAudioChatListener sendAudioChatListener) {
        this.sendAudioChatListener = sendAudioChatListener;
    }

    public void setChatLayoutOnClickListener(ChatLayoutOnClickListener chatLayoutOnClickListener) {
        this.chatLayoutOnClickListener = chatLayoutOnClickListener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }


    @Override
    public void afterTextChanged(Editable s) {

        inputText = editText.getText().toString();

        if (inputText.isEmpty()) {
            inputChatViewModel.showSendBtn.set(false);
        } else
            inputChatViewModel.showSendBtn.set(true);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.add_voice_btn:

                if (isRecordingAudio) {

                    isRecordingAudio = false;

                    recordAudioUseCase.stopAudioRecord();

                    if (onSendAudioChat(recordAudioUseCase.getAudioRecordFilePath(), recordAudioUseCase.getAudioRecordTime()))
                        inputChatViewModel.showAddVoiceLayout.set(false);


                } else {

                    isRecordingAudio = true;

                    recordAudioUseCase.startAudioRecord();

                    binding.addVoiceBtn.setText("结束录音");
                }

                break;
            case R.id.edit_text:
            case R.id.add_voice:

                if (chatLayoutOnClickListener != null)
                    chatLayoutOnClickListener.onChatLayoutClick();

                inputChatMenuViewModel.showChatMenu.set(false);

                inputChatViewModel.showAddVoiceLayout.set(true);

                break;

            case R.id.add_button:

                if (chatLayoutOnClickListener != null)
                    chatLayoutOnClickListener.onChatLayoutClick();

                if (addBtnOnClickListener != null)
                    addBtnOnClickListener.onAddBtnClick();

                inputChatViewModel.showAddVoiceLayout.set(false);

                inputChatMenuViewModel.showChatMenu.set(true);

                break;

            case R.id.add_emoticon:

                if (chatLayoutOnClickListener != null)
                    chatLayoutOnClickListener.onChatLayoutClick();

                inputChatViewModel.showAddVoiceLayout.set(false);

                inputChatMenuViewModel.showChatMenu.set(false);

                break;

            case R.id.send_button:

                boolean result = onSendTextChat();
                if (result)
                    editText.getText().clear();

                break;
        }

    }

    private boolean onSendTextChat() {

        return sendTextChatListener != null && sendTextChatListener.onSendTextChat(inputText);
    }

    private boolean onSendAudioChat(String filePath, long audioRecordTime) {
        return sendAudioChatListener != null && sendAudioChatListener.onSendAudioChat(filePath, audioRecordTime);
    }

    /**
     * Called when the focus state of a view has changed.
     *
     * @param v        The view whose state has changed.
     * @param hasFocus The new focus state of v.
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        if (editTextFocusChangeListener != null)
            editTextFocusChangeListener.onFocusChanged(hasFocus);

    }

    public interface EditTextFocusChangeListener {

        void onFocusChanged(boolean hasFocus);

    }

    public interface SendTextChatListener {

        boolean onSendTextChat(String text);

    }

    public interface ChatLayoutOnClickListener {

        void onChatLayoutClick();

    }

    public interface AddBtnOnClickListener {

        void onAddBtnClick();

    }

    public interface SendAudioChatListener {

        boolean onSendAudioChat(String filePath, long audioRecordTime);

    }

    public boolean handleOnBackPressed() {

        if (inputChatMenuViewModel.showChatMenu.get()) {

            inputChatMenuViewModel.showChatMenu.set(false);

            return true;
        } else
            return false;

    }

}
