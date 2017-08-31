package com.winsun.fruitmix.equipment.data;

import android.content.Context;
import android.util.Log;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.winsun.fruitmix.CustomApplication;
import com.winsun.fruitmix.model.Equipment;
import com.winsun.fruitmix.util.Util;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2017/5/16.
 */

public class EquipmentSearchManager {

    public static final String TAG = EquipmentSearchManager.class.getSimpleName();

    private RxDnssd mRxDnssd;
    private Subscription mSubscription;

    private static final String SERVICE_PORT = "_http._tcp";
    private static final String DEMAIN = "local.";

    public static EquipmentSearchManager instance;

    private EquipmentSearchManager(Context context) {

        mRxDnssd = CustomApplication.getRxDnssd(context);

    }

    static EquipmentSearchManager getInstance(Context context) {

        if (instance == null) {
            instance = new EquipmentSearchManager(context);
        }

        return instance;
    }

    public void startDiscovery(final IEquipmentDiscoveryListener listener) {

        mSubscription = mRxDnssd.browse(SERVICE_PORT, DEMAIN)
                .compose(mRxDnssd.resolve())
                .compose(mRxDnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {

                        if (!Util.checkBonjourService(bonjourService)) return;

                        Equipment createdEquipment = Equipment.createEquipment(bonjourService);

                        if (createdEquipment == null) return;

                        listener.call(createdEquipment);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "call: " + throwable);
                    }
                });

    }

    public void stopDiscovery() {

        if (mSubscription != null) {
            mSubscription.unsubscribe();

            mSubscription = null;
        }

    }

    public interface IEquipmentDiscoveryListener {

        void call(Equipment equipment);

    }


}
