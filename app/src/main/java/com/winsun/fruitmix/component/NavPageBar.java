package com.winsun.fruitmix.component;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.winsun.fruitmix.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/19.
 */
public class NavPageBar {

    TabLayout tabLayout;
    ViewPager viewPager;

    ArrayList<Map<String, String>> itemList;

    OnTabChangedListener onTabChangedListener;

    List<OnTabChangedListener> onTabChangedListenerList;

    public void init() {
        Map<String, String> itemMap;

        itemList=new ArrayList<Map<String, String>>();

        itemMap=new HashMap<String, String>();
        itemMap.put("name", "share");
        itemMap.put("img", ""+R.drawable.ic_share);
        itemMap.put("active_img", "" + R.drawable.ic_share);
        itemList.add(itemMap);

        itemMap=new HashMap<String, String>();
        itemMap.put("name", "photo");
        itemMap.put("img", ""+R.drawable.ic_photo);
        itemMap.put("active_img", ""+R.drawable.ic_photo);
        itemList.add(itemMap);

        itemMap=new HashMap<String, String>();
        itemMap.put("name", "album");
        itemMap.put("img", "" + R.drawable.ic_photo_album);
        itemMap.put("active_img", "" + R.drawable.ic_photo_album);
        itemList.add(itemMap);


    }

    public NavPageBar(TabLayout tabLayout_, ViewPager viewPager_) {

        init();

        tabLayout=tabLayout_;
        viewPager=viewPager_;

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(getTabView(i));
            }
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ImageView iv;
                int currentPos;

                currentPos = tabLayout.getSelectedTabPosition();
                Log.d("winsun", "aaa " + currentPos);
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    iv = (ImageView) (tabLayout.getTabAt(i).getCustomView().findViewById(R.id.imageView));
                    if (i == currentPos)
                        iv.setImageResource(Integer.parseInt(itemList.get(i).get("active_img")));
                    else iv.setImageResource(Integer.parseInt(itemList.get(i).get("img")));
                }

                viewPager.setCurrentItem(currentPos);

                if(onTabChangedListenerList != null){
                    Iterator<OnTabChangedListener> iterator = onTabChangedListenerList.iterator();
                    while (iterator.hasNext()){
                        onTabChangedListener  = iterator.next();
                        onTabChangedListener.onTabChanged(currentPos);
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    public View getTabView(int position) {
        View v = LayoutInflater.from(tabLayout.getContext()).inflate(R.layout.nav_tab_item, null);
        ImageView img = (ImageView) v.findViewById(R.id.imageView);
        //Log.d("winsun", img);
        img.setImageResource(Integer.parseInt(itemList.get(position).get("img")));
        return v;
    }

    public interface OnPageChangedListener {
        public abstract void onPageChanged(String pageName);
    }

    public static final int TAB_ALBUM = 2;

    public interface OnTabChangedListener {
        void onTabChanged(int tabNum);
    }

    public void registerOnTabChangedListener(OnTabChangedListener listener){
        if(onTabChangedListenerList == null){
            onTabChangedListenerList = new ArrayList<>();
        }
        onTabChangedListenerList.add(listener);
    }

    public void unregisterOnTabChangedListener(OnTabChangedListener listener){
        if(onTabChangedListenerList != null){
            onTabChangedListenerList.remove(listener);
        }
    }
}

