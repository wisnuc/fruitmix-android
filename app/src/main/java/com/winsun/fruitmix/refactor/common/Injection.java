package com.winsun.fruitmix.refactor.common;

import com.winsun.fruitmix.refactor.business.DataBusiness;
import com.winsun.fruitmix.refactor.data.cache.CacheDataSource;
import com.winsun.fruitmix.refactor.data.db.DBDataSource;
import com.winsun.fruitmix.refactor.data.server.ServerDataSource;

/**
 * Created by Administrator on 2017/2/6.
 */

public class Injection {

    public static DataBusiness injectDataBusiness() {

        return DataBusiness.getInstance(new CacheDataSource(), new DBDataSource(), new ServerDataSource());

    }

}
