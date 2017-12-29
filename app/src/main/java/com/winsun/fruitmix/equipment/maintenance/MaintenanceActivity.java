package com.winsun.fruitmix.equipment.maintenance;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.winsun.fruitmix.BR;
import com.winsun.fruitmix.BaseActivity;
import com.winsun.fruitmix.R;
import com.winsun.fruitmix.databinding.ActivityMaintenanceBinding;
import com.winsun.fruitmix.databinding.ActivityTorrentDownloadManageBinding;
import com.winsun.fruitmix.databinding.ToolbarLayoutBinding;
import com.winsun.fruitmix.util.Util;
import com.winsun.fruitmix.viewmodel.ToolbarViewModel;

public class MaintenanceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMaintenanceBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_maintenance);

        initToolBar(binding,binding.toolbarLayout,getString(R.string.maintenance));

    }


}
