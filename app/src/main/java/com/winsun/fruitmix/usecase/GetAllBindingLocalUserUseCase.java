package com.winsun.fruitmix.usecase;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.logged.in.user.LoggedInUser;
import com.winsun.fruitmix.logged.in.user.LoggedInUserDataSource;
import com.winsun.fruitmix.logged.in.user.LoggedInWeChatUser;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.stations.Station;
import com.winsun.fruitmix.stations.StationsDataSource;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.user.datasource.UserDataRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/9/21.
 */

public class GetAllBindingLocalUserUseCase {

    private UserDataRepository userDataRepository;

    private StationsDataSource stationsDataSource;


    public GetAllBindingLocalUserUseCase(UserDataRepository userDataRepository, StationsDataSource stationsDataSource) {
        this.userDataRepository = userDataRepository;
        this.stationsDataSource = stationsDataSource;

    }

    public void getAllBindingLocalUser(final String guid, final String token, final BaseLoadDataCallback<LoggedInWeChatUser> callback) {

        final List<LoggedInWeChatUser> loggedInUsers = new ArrayList<>();

        if (guid.isEmpty()) {

            callback.onSucceed(loggedInUsers, new OperationSuccess());

            return;

        }

        stationsDataSource.getStationsByWechatGUID(guid, new BaseLoadDataCallback<Station>() {
            @Override
            public void onSucceed(List<Station> data, OperationResult operationResult) {

                getLoggedInUser(data, guid, token, loggedInUsers, callback);

            }

            @Override
            public void onFail(OperationResult operationResult) {

                callback.onSucceed(loggedInUsers, new OperationSuccess());

            }
        });

    }

    private void getLoggedInUser(final List<Station> data, final String guid, final String token, final List<LoggedInWeChatUser> loggedInUsers,
                                 final BaseLoadDataCallback<LoggedInWeChatUser> callback) {

        if (data.isEmpty()) {

            callback.onSucceed(loggedInUsers, new OperationSuccess());

            return;
        }

        final Station station = data.get(0);

        userDataRepository.getUsersByStationID(station.getId(), new BaseLoadDataCallback<User>() {
            @Override
            public void onSucceed(List<User> users, OperationResult operationResult) {

                User currentLocalUser = null;

                for (User user : users) {

                    if (user.getAssociatedWechatGUID().equals(guid)) {

                        currentLocalUser = user;
                        break;
                    }

                }

                if (currentLocalUser == null) {

                    continueGetLoggedInUser(data, station, guid, token, loggedInUsers, callback);

                } else {

                    LoggedInWeChatUser loggedInUser = new LoggedInWeChatUser("", token, "", station.getLabel(), currentLocalUser,station.getId());

                    loggedInUsers.add(loggedInUser);

                    continueGetLoggedInUser(data, station, guid, token, loggedInUsers, callback);

                }

            }

            @Override
            public void onFail(OperationResult operationResult) {

                continueGetLoggedInUser(data, station, guid, token, loggedInUsers, callback);

            }
        });
    }

    private void continueGetLoggedInUser(List<Station> data, Station station, String guid, String token,
                                         List<LoggedInWeChatUser> loggedInUsers, BaseLoadDataCallback<LoggedInWeChatUser> callback) {
        data.remove(station);

        getLoggedInUser(data, guid, token, loggedInUsers, callback);
    }


}
