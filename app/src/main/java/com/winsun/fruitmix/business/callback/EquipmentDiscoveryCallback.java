package com.winsun.fruitmix.business.callback;

import com.github.druk.rxdnssd.BonjourService;

/**
 * Created by Administrator on 2017/3/20.
 */

public interface EquipmentDiscoveryCallback {

    void onEquipmentDiscovery(BonjourService bonjourService);

}
