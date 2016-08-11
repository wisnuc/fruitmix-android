package com.winsun.fruitmix.interfaces;

/**
 * Created by Administrator on 2016/8/4.
 */
public interface IPhotoListListener {

    void onPhotoItemClick(int selectedItemCount);

    void onPhotoItemLongClick(int selectedItemCount);

    void onNoPhotoItem(boolean noPhotoItem);
}
