package com.winsun.fruitmix.file.view.interfaces;

/**
 * Created by Administrator on 2017/8/26.
 */

public interface FileListSelectModeListener {

    void onFileSelectItemClick(int selectItemCount);

    void onFileItemLongClick();

    void enterSelectMode();

    void quitSelectMode();

}
