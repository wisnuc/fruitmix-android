package com.winsun.fruitmix.fragment;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.winsun.fruitmix.NavPagerActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.NavPageBar;
import com.winsun.fruitmix.executor.ExecutorServiceInstance;
import com.winsun.fruitmix.interfaces.OnFragmentInteractionListener;
import com.winsun.fruitmix.mediaModule.fragment.AlbumList;
import com.winsun.fruitmix.mediaModule.fragment.MediaShareList;
import com.winsun.fruitmix.mediaModule.fragment.NewPhotoList;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaMainFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.right)
    TextView lbRight;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.btmenu)
    LinearLayout llBtMenu;
    @BindView(R.id.bt_album)
    ImageView ivBtAlbum;
    @BindView(R.id.bt_share)
    ImageView ivBtShare;
    @BindView(R.id.album_balloon)
    ImageView mAlbumBalloon;

    private List<NavPagerActivity.Page> pageList;
    private AlbumList albumList;
    private NewPhotoList photoList;
    private MediaShareList shareList;
    private NavPageBar mNavPageBar;

    private LocalBroadcastManager mManager;

    private Animator mAnimator;

    private boolean sMenuUnfolding = false;

    private Context mContext;

    private ProgressDialog mDialog;

    private static final int PAGE_SHARE = 0;
    private static final int PAGE_PHOTO = 1;
    private static final int PAGE_ALBUM = 2;

    private ExecutorServiceInstance instance;

    private boolean sInChooseMode = false;

    private boolean mLocalMediaLoaded = false;
    private boolean mRemoteMediaLoaded = false;

    private boolean mRemoteMediaShareLoaded = false;
    private boolean mLocalMediaShareLoaded = false;

    private boolean onCreate = false;

    private OnFragmentInteractionListener mListener;


    public MediaMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MediaMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaMainFragment newInstance() {
        MediaMainFragment fragment = new MediaMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nav_pager_main,container,false);

        ButterKnife.bind(this,view);

        mContext = getActivity();



        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
