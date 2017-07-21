package com.winsun.fruitmix.group.view;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityGroupContentBinding;

public class GroupContentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityGroupContentBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_group_content);

        recyclerView = binding.chatRecyclerview;



    }



}
