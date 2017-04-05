package com.winsun.fruitmix.common;

import android.content.Context;

import com.winsun.fruitmix.business.DataRepository;
import com.winsun.fruitmix.data.db.DBDataSource;
import com.winsun.fruitmix.data.memory.MemoryDataSource;
import com.winsun.fruitmix.data.server.ServerDataSource;

/**
 * Created by Administrator on 2017/2/6.
 */

public class Injection {

    public static DataRepository injectDataRepository(Context context) {

        return DataRepository.getInstance(context,MemoryDataSource.getInstance(), DBDataSource.getInstance(context), ServerDataSource.getInstance());

    }

}
