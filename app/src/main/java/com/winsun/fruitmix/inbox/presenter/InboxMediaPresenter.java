package com.winsun.fruitmix.inbox.presenter;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.InboxMediaItemBinding;
import com.winsun.fruitmix.databinding.InboxPhotoItemBinding;
import com.winsun.fruitmix.inbox.data.model.GroupMediaComment;
import com.winsun.fruitmix.inbox.data.model.GroupUserComment;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.util.MediaUtil;

import java.util.List;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by Administrator on 2018/1/11.
 */

public class InboxMediaPresenter {

    private InboxMediaItemBinding mInboxMediaItemBinding;

    private List<Media> mMedias;

    private TextView mediaCountTV;

    private ImageLoader mImageLoader;

    public InboxMediaPresenter(InboxMediaItemBinding inboxMediaItemBinding, GroupMediaComment groupMediaComment, ImageLoader imageLoader) {
        mInboxMediaItemBinding = inboxMediaItemBinding;

        mMedias = groupMediaComment.getMedias();

        mImageLoader = imageLoader;

        if (mMedias.size() > 6) {
            mMedias = mMedias.subList(0, 6);
        }

        InboxMediaPagerAdapter mediaPagerAdapter = new InboxMediaPagerAdapter();

        ViewPager viewPager = mInboxMediaItemBinding.mediaViewpager;

        viewPager.setAdapter(mediaPagerAdapter);

        CircleIndicator circleIndicator = mInboxMediaItemBinding.circleIndicator;
        circleIndicator.setViewPager(viewPager);

        mediaPagerAdapter.registerDataSetObserver(circleIndicator.getDataSetObserver());

        mediaCountTV = mInboxMediaItemBinding.mediaCount;

        setCount(1);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                setCount(position);

            }
        });

    }

    private void setCount(int position) {
        mediaCountTV.setText(getString(R.string.slash, position, mMedias.size()));
    }

    private String getString(int resID) {
        return mInboxMediaItemBinding.getRoot().getContext().getString(resID);
    }

    private String getString(int resID, Object... formatArgs) {
        return mInboxMediaItemBinding.getRoot().getContext().getString(resID, formatArgs);
    }


    private class InboxMediaPagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            InboxPhotoItemBinding binding = InboxPhotoItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

            View view = binding.getRoot();

            container.addView(view);

            Media media = mMedias.get(position);

            MediaUtil.setMediaImageUrl(media, binding.photoNetworkImageview, media.getImageThumbUrl(view.getContext()), mImageLoader);

            return view;

        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

            container.removeView((View) object);
        }

        /**
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            return mMedias.size();
        }


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }


}
