package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.torrent.data.DownloadState;
import com.winsun.fruitmix.torrent.data.TorrentDownloadInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/13.
 */

public class RemoteTorrentDownloadInfoParser extends BaseRemoteDataParser implements RemoteDatasParser<TorrentDownloadInfo> {

    @Override
    public List<TorrentDownloadInfo> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        JSONObject jsonObject = new JSONObject(root);

        JSONArray runnings = jsonObject.optJSONArray("running");
        JSONArray finishs = jsonObject.optJSONArray("finish");

        List<TorrentDownloadInfo> torrentDownloadInfos = new ArrayList<>();

        for (int i = 0; i < runnings.length(); i++) {

            JSONObject running = runnings.optJSONObject(i);

            TorrentDownloadInfo torrentDownloadInfo = createInstance(running);

            torrentDownloadInfo.setState(DownloadState.DOWNLOADING);

            torrentDownloadInfos.add(torrentDownloadInfo);
        }

        for (int i = 0; i < finishs.length(); i++) {

            JSONObject finish = finishs.optJSONObject(i);

            TorrentDownloadInfo torrentDownloadInfo = createInstance(finish);

            torrentDownloadInfo.setState(DownloadState.DOWNLOADED);

            torrentDownloadInfos.add(torrentDownloadInfo);
        }

        return torrentDownloadInfos;
    }

    private TorrentDownloadInfo createInstance(JSONObject jsonObject) {

        String hash = jsonObject.optString("infoHash");

        Long downloaded = jsonObject.optLong("downloaded");

        Double downloadSpeed = jsonObject.optDouble("downloadSpeed");

        Double progress = jsonObject.optDouble("progress");

        String name = jsonObject.optString("name");

        boolean isPause = jsonObject.optBoolean("isPause");

        Long time = jsonObject.optLong("finishTime");

        String torrentPath = jsonObject.optString("torrentPath");

        if (torrentPath.equals("null"))
            torrentPath = "";

        String magnetUrl = jsonObject.optString("magnetUrl");

        if (magnetUrl.equals("null"))
            magnetUrl = "";

        return new TorrentDownloadInfo(hash, downloaded, downloadSpeed, progress, name, DownloadState.UNDEFINED,
                isPause, time, torrentPath, magnetUrl);

    }

}
