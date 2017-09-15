package com.winsun.fruitmix.stations;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.callback.BaseLoadDataCallbackImpl;
import com.winsun.fruitmix.mock.MockThreadManager;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;
import com.winsun.fruitmix.thread.manage.ThreadManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void getStations_Succeed() {

        stationsRepository.getStationsByWechatGUID(testGUID, new BaseLoadDataCallbackImpl<Station>());

        ArgumentCaptor<BaseLoadDataCallback<Station>> captor = ArgumentCaptor.forClass(BaseLoadDataCallback.class);

        verify(stationsDataSource).getStationsByWechatGUID(anyString(), captor.capture());

        captor.getValue().onSucceed(Collections.singletonList(createStation()), new OperationSuccess());

        assertEquals(1, stationsRepository.stations.size());

        assertEquals(testStationID, stationsRepository.stations.get(0).getId());
        assertEquals(testStationLabel, stationsRepository.stations.get(0).getLabel());
        assertEquals(testGUID, stationsRepository.currentGUID);

    }

    @Test
    public void getStationsTwiceWithSameGUID() {

        getStations_Succeed();

        stationsRepository.getStationsByWechatGUID(testGUID, new BaseLoadDataCallbackImpl<Station>());

        verify(stationsDataSource, times(1)).getStationsByWechatGUID(anyString(), any(BaseLoadDataCallback.class));

    }

    @Test
    public void getStationsTwiceWithDifferentGUID() {

        getStations_Succeed();

        stationsRepository.getStationsByWechatGUID(testGUID2, new BaseLoadDataCallbackImpl<Station>());

        verify(stationsDataSource, times(2)).getStationsByWechatGUID(anyString(), any(BaseLoadDataCallback.class));

    }




}
