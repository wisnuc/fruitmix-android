package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.torrent.data.TorrentDownloadInfo;

import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 2017/12/13.
 */

public class RemoteTorrentDownloadInfoParser implements RemoteDatasParser<TorrentDownloadInfo> {

    @Override
    public List<TorrentDownloadInfo> parse(String json) throws JSONException {
        return null;
    }
}
