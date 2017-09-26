package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/9/14.
 */

public class StationsRepositoryTest {

    private StationsRepository stationsRepository;

    @Mock
    private StationsDataSource stationsDataSource;

    private String testStationID = "testStationID";
    private String testStationLabel = "testStationLabel";

    private String testGUID = "testGUID";
    private String testGUID2 = "testGUID2";

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);

        ThreadManager threadManager = new MockThreadManager();

        stationsRepository = StationsRepository.getInstance(threadManager, stationsDataSource);

    }

    @After
    public void teardown() {

        StationsRepository.destroyInstance();

    }

    private Station createStation() {

        Station station = new Station();
        station.setLabel(testStationLabel);
        station.setId(testStationID);

        return station;
    }

    private void getStationsSucceed() {

        stationsRepository.getStationsByWechatGUID(testGUID, new BaseLoadDataCallbackImpl<Station>());

        verify(stationsDataSource).getStationsByWechatGUID(anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void getStationsTwiceWithSameGUID() {

        getStationsSucceed();

        stationsRepository.getStationsByWechatGUID(testGUID, new BaseLoadDataCallbackImpl<Station>());

        verify(stationsDataSource, times(2)).getStationsByWechatGUID(anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void getStationsTwiceWithDifferentGUID() {

        getStationsSucceed();

        stationsRepository.getStationsByWechatGUID(testGUID2, new BaseLoadDataCallbackImpl<Station>());

        verify(stationsDataSource, times(2)).getStationsByWechatGUID(anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void getStationsWhichOneStationIsOffLine() {

        stationsRepository.getStationsByWechatGUID(testGUID, new BaseLoadDataCallbackImpl<Station>() {

            @Override
            public void onSucceed(List<Station> data, OperationResult operationResult) {
                super.onSucceed(data, operationResult);

                assertEquals(1, data.size());

            }
        });

        ArgumentCaptor<BaseLoadDataCallback<Station>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationsDataSource).getStationsByWechatGUID(eq(testGUID), captor.capture());

        Station station1 = createStation();
        station1.setOnline(true);

        Station station2 = createStation();
        station2.setOnline(false);

        List<Station> stations = new ArrayList<>();
        stations.add(station1);
        stations.add(station2);

        captor.getValue().onSucceed(stations, new OperationSuccess());

    }


}
