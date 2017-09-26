package com.winsun.fruitmix.usecase;

import android.content.Context;

import com.winsun.fruitmix.stations.InjectStation;
import com.winsun.fruitmix.user.datasource.InjectUser;

/**
 * Created by Administrator on 2017/9/21.
 */

public class InjectGetAllBindingLocalUserUseCase {

    public static GetAllBindingLocalUserUseCase provideInstance(Context context){

        return new GetAllBindingLocalUserUseCase(InjectUser.provideRepository(context), InjectStation.provideStationDataSource(context));

    }

}
