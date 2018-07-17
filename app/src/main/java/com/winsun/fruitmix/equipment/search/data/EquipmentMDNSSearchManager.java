package com.winsun.fruitmix.equipment.search.data;

import android.content.Context;
import android.util.Log;

import com.github.druk.rxdnssd.BonjourService;
import com.github.druk.rxdnssd.RxDnssd;
import com.github.druk.rxdnssd.RxDnssdBindable;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by Administrator on 2017/5/16.
 */

public class EquipmentMDNSSearchManager implements EquipmentSearchManager {

    public static final String TAG = EquipmentMDNSSearchManager.class.getSimpleName();

    private RxDnssd mRxDnssd;
    private Subscription mSubscription;

    private static final String SERVICE_PORT = "_http._tcp";
    private static final String DEMAIN = "local.";

    public static EquipmentMDNSSearchManager instance;

    private EquipmentMDNSSearchManager(Context context) {

        initRxDnssd(context);

    }

    private RxDnssd createDnssd(Context context) {
        return new RxDnssdBindable(context);
    }

    static EquipmentMDNSSearchManager getInstance(Context context) {

        if (instance == null) {
            instance = new EquipmentMDNSSearchManager(context);
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private void initRxDnssd(Context context) {
        if (mRxDnssd == null)
            mRxDnssd = createDnssd(context);
    }

    private void destroyRxDnssd() {
        mRxDnssd = null;
    }

    public void startDiscovery(Context context,final EquipmentFoundedListener listener) {

        Log.d(TAG, "Equipment: startDiscovery");

        initRxDnssd(context);

        mSubscription = mRxDnssd.browse(SERVICE_PORT, DEMAIN)
                .compose(mRxDnssd.resolve())
                .compose(mRxDnssd.queryRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BonjourService>() {
                    @Override
                    public void call(BonjourService bonjourService) {

                        Log.d(TAG, "Equipment: " + bonjourService);

                        if (!Equipment.checkBonjourService(bonjourService)) return;

                        Equipment createdEquipment = Equipment.createEquipment(bonjourService);

                        if (createdEquipment == null) return;

                        Log.d(TAG, "Equipment: createEquipment: " + createdEquipment + " listener: " + listener);

                        listener.call(createdEquipment);

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "Equipment: " + throwable);

                        throwable.printStackTrace();
                    }
                });

    }

    public void stopDiscovery() {

        Log.d(TAG, "stopDiscovery");

        if (mSubscription != null) {
            mSubscription.unsubscribe();

            mSubscription = null;
        }

        destroyRxDnssd();
    }

}
