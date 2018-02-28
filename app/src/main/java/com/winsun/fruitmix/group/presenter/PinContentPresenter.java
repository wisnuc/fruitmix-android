package com.winsun.fruitmix.group.presenter;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.callback.BaseOperateDataCallback;
import com.winsun.fruitmix.databinding.PinContentItemBinding;
import com.winsun.fruitmix.databinding.SingleFileBinding;
import com.winsun.fruitmix.databinding.SinglePhotoBinding;
import com.winsun.fruitmix.file.data.model.AbstractFile;
import com.winsun.fruitmix.group.data.model.Pin;
import com.winsun.fruitmix.group.data.source.GroupRepository;
import com.winsun.fruitmix.group.data.viewmodel.PinContentItemViewModel;
import com.winsun.fruitmix.group.interfaces.OnPinContentItemClickListener;
import com.winsun.fruitmix.group.view.PinContentView;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.InjectHttp;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.util.MediaUtil;
import com.winsun.fruitmix.viewholder.BindingViewHolder;
import com.winsun.fruitmix.viewmodel.LoadingViewModel;
import com.winsun.fruitmix.viewmodel.NoContentViewModel;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Administrator on 2017/8/10.
 */

public class PinContentPresenter implements OnPinContentItemClickListener {

    private String groupUUID;

    private String pinUUID;

    private Pin pin;

    private GroupRepository groupRepository;

    private LoadingViewModel loadingViewModel;

    private NoContentViewModel noContentViewModel;

    private ToolbarViewModel toolbarViewModel;

    private PinContentView pinContentView;

    private ImageLoader imageLoader;

    private PinContentAdapter pinContentAdapter;

    private boolean modifyMode = false;

    private List<PinItemView> pinItemViews;

    public PinContentPresenter(String groupUUID, String pinUUID, GroupRepository groupRepository, LoadingViewModel loadingViewModel,
                               NoContentViewModel noContentViewModel, ToolbarViewModel toolbarViewModel, PinContentView pinContentView, ImageLoader imageLoader) {

        this.groupUUID = groupUUID;
        this.pinUUID = pinUUID;
        this.groupRepository = groupRepository;
        this.loadingViewModel = loadingViewModel;
        this.toolbarViewModel = toolbarViewModel;
        this.noContentViewModel = noContentViewModel;
        this.pinContentView = pinContentView;
        this.imageLoader = imageLoader;

        pinContentAdapter = new PinContentAdapter();

        pinItemViews = new ArrayList<>();

        initToolbar();
    }

    private void initToolbar() {

        toolbarViewModel.selectTextResID.set(R.string.finish_text);

        toolbarViewModel.setToolbarSelectBtnOnClickListener(new ToolbarViewModel.ToolbarSelectBtnOnClickListener() {
            @Override
            public void onClick() {

                deleteMediaOrFileInPin();

            }
        });
    }

    private void deleteMediaOrFileInPin() {

        groupRepository.updatePinInGroup(pin, groupUUID, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                handleUpdatePinInGroup();

            }

            @Override
            public void onFail(OperationResult result) {

                handleUpdatePinInGroup();

            }
        });


    }

    private void handleUpdatePinInGroup() {
        toolbarViewModel.showSelect.set(false);

        pinContentView.showMenu();

        quitModifyMode();

        refreshPinContent();
    }


    public void onDestroy() {

        pinContentView = null;
    }

    public PinContentAdapter getPinContentAdapter() {
        return pinContentAdapter;
    }

    public ArrayList<String> getPinMediaKeys() {

        List<Media> medias = pin.getMedias();

        ArrayList<String> pinMediaKeys = new ArrayList<>(medias.size());

        for (Media media : medias) {
            pinMediaKeys.add(media.getKey());
        }

        return pinMediaKeys;
    }

    public ArrayList<String> getPinFileNames() {

        List<AbstractFile> files = pin.getFiles();

        ArrayList<String> pinFileNames = new ArrayList<>(files.size());

        for (AbstractFile file : files) {
            pinFileNames.add(file.getName());
        }

        return pinFileNames;
    }


    public void refreshPinName() {
        pin = groupRepository.getPinInGroup(pinUUID, groupUUID);

        toolbarViewModel.titleText.set(pin.getName());
    }

    public void refreshPinContent() {

        refreshPinName();

        pinItemViews.clear();

        List<Media> medias = pin.getMedias();

        for (Media media : medias) {

            PinItemView pinItemView = new MediaPinItemView(media);

            pinItemViews.add(pinItemView);
        }

        List<AbstractFile> files = pin.getFiles();

        for (AbstractFile file : files) {

            PinItemView pinItemView = new FilePinItemView(file);

            pinItemViews.add(pinItemView);

        }

        if (pinItemViews.isEmpty()) {

            loadingViewModel.showLoading.set(false);
            noContentViewModel.showNoContent.set(true);

        } else {

            loadingViewModel.showLoading.set(false);
            noContentViewModel.showNoContent.set(false);

            pinContentAdapter.setPinItemViews(pinItemViews);
            pinContentAdapter.notifyDataSetChanged();

        }

    }

    private void enterModifyMode() {
        modifyMode = true;

    }

    private void quitModifyMode() {
        modifyMode = false;

    }

    public void modifyPinContent() {

        enterModifyMode();
        pinContentAdapter.notifyDataSetChanged();

        pinContentView.dismissMenu();

        toolbarViewModel.showSelect.set(true);


    }

    public void deletePin() {

        groupRepository.deletePin(groupUUID, pinUUID, new BaseOperateDataCallback<Boolean>() {
            @Override
            public void onSucceed(Boolean data, OperationResult result) {

                pinContentView.setResult(RESULT_OK);

                pinContentView.finishView();
            }

            @Override
            public void onFail(OperationResult result) {

                pinContentView.showToast("删除失败");

            }
        });

    }

    @Override
    public void onClick(PinItemView pinItemView) {

        if (modifyMode) {

            int position = pinItemViews.indexOf(pinItemView);

            if (position == -1)
                return;

            pinItemViews.remove(pinItemView);

            pinContentAdapter.setPinItemViews(pinItemViews);

            pinContentAdapter.notifyItemRemoved(position);

            if (pinItemView instanceof MediaPinItemView)
                pin.removeMedia(((MediaPinItemView) pinItemView).getMedia());
            else if (pinItemView instanceof FilePinItemView)
                pin.removeFile(((FilePinItemView) pinItemView).getAbstractFile());

        } else {


        }

    }


    class PinContentAdapter extends RecyclerView.Adapter<BindingViewHolder> {

        private List<PinItemView> mPinItemViews;

        public PinContentAdapter() {
            mPinItemViews = new ArrayList<>();
        }

        public void setPinItemViews(List<PinItemView> pinItemViews) {
            mPinItemViews.clear();
            mPinItemViews.addAll(pinItemViews);
        }

        @Override
        public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            BindingViewHolder bindingViewHolder;

            PinContentItemBinding pinContentItemBinding = PinContentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            ViewDataBinding binding;

            if (viewType == PinItemView.TYPE_MEDIA) {

                binding = SinglePhotoBinding.inflate(LayoutInflater.from(parent.getContext()), pinContentItemBinding.pinContentContainer, false);

            } else {

                binding = SingleFileBinding.inflate(LayoutInflater.from(parent.getContext()), pinContentItemBinding.pinContentContainer, false);

            }

            pinContentItemBinding.pinContentContainer.addView(binding.getRoot());

            bindingViewHolder = new BindingViewHolder(pinContentItemBinding);

            return bindingViewHolder;
        }

        @Override
        public void onBindViewHolder(BindingViewHolder holder, int position) {

            PinContentItemBinding pinContentItemBinding = (PinContentItemBinding) holder.getViewDataBinding();

            PinContentItemViewModel pinContentItemViewModel = pinContentItemBinding.getPinContentItemViewModel();

            if (pinContentItemViewModel == null) {
                pinContentItemViewModel = new PinContentItemViewModel();
                pinContentItemBinding.setPinContentItemViewModel(pinContentItemViewModel);
            }

            pinContentItemViewModel.showDelPhoto.set(modifyMode);

            PinItemView pinItemView = mPinItemViews.get(position);

            pinContentItemBinding.setPinItemView(pinItemView);

            pinContentItemBinding.setOnPinContentItemClickListener(PinContentPresenter.this);

            ViewDataBinding binding = DataBindingUtil.getBinding(pinContentItemBinding.pinContentContainer.getChildAt(0));

            if (pinItemView.getType() == PinItemView.TYPE_MEDIA) {

                SinglePhotoBinding singlePhotoBinding = (SinglePhotoBinding) binding;

                NetworkImageView networkImageView = singlePhotoBinding.coverImg;

                Media media = ((MediaPinItemView) pinItemView).getMedia();

                HttpRequest httpRequest = media.getImageThumbUrl(InjectHttp.provideHttpRequestFactory(pinContentView.getContext()));

                MediaUtil.setMediaImageUrl(media,networkImageView, httpRequest, imageLoader);

            } else {

                SingleFileBinding singleFileBinding = (SingleFileBinding) binding;

                singleFileBinding.setFile(((FilePinItemView) pinItemView).getAbstractFile());

            }

        }

        @Override
        public int getItemCount() {
            return mPinItemViews.size();
        }

        @Override
        public int getItemViewType(int position) {
            return mPinItemViews.get(position).getType();
        }
    }

    public interface PinItemView {

        int TYPE_MEDIA = 0;
        int TYPE_FILE = 1;

        int getType();

    }

    private class MediaPinItemView implements PinItemView {

        private Media media;

        public MediaPinItemView(Media media) {
            this.media = media;
        }

        @Override
        public int getType() {
            return TYPE_MEDIA;
        }

        public Media getMedia() {
            return media;
        }
    }

    private class FilePinItemView implements PinItemView {

        private AbstractFile abstractFile;

        public FilePinItemView(AbstractFile abstractFile) {
            this.abstractFile = abstractFile;
        }

        @Override
        public int getType() {
            return TYPE_FILE;
        }

        public AbstractFile getAbstractFile() {
            return abstractFile;
        }
    }


}
