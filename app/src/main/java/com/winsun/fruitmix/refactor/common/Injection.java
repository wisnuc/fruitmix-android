package com.winsun.fruitmix.refactor.common;

import android.content.Context;

import com.winsun.fruitmix.refactor.business.DataRepository;
import com.winsun.fruitmix.refactor.data.db.DBDataSource;
import com.winsun.fruitmix.refactor.data.memory.MemoryDataSource;
import com.winsun.fruitmix.refactor.data.server.ServerDataSource;

/**
 * Created by Administrator on 2017/2/6.
 */

public class Injection {

    public static DataRepository injectDataRepository(Context context) {

        return DataRepository.getInstance(MemoryDataSource.getInstance(), DBDataSource.getInstance(context), ServerDataSource.getInstance());

    }

}
