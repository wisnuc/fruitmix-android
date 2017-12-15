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

            String hash = running.optString("infoHash");

            Long downloaded = running.optLong("downloaded");

            Double downloadSpeed = running.optDouble("downloadSpeed");

            Double progress = running.optDouble("progress");

            String name = running.optString("name");

            boolean isPause = running.optBoolean("isPause");

            Long time = running.optLong("time");

            TorrentDownloadInfo torrentDownloadInfo = new TorrentDownloadInfo(hash, downloaded, downloadSpeed, progress, name, DownloadState.DOWNLOADING,
                    isPause, time);

            torrentDownloadInfos.add(torrentDownloadInfo);
        }

        for (int i = 0; i < finishs.length(); i++) {

            JSONObject finish = finishs.optJSONObject(i);

            String hash = finish.optString("infoHash");

            Long downloaded = finish.optLong("downloaded");

            Double downloadSpeed = finish.optDouble("downloadSpeed");

            Double progress = finish.optDouble("progress");

            String name = finish.optString("name");

            boolean isPause = finish.optBoolean("isPause");

            Long time = finish.optLong("time");

            TorrentDownloadInfo torrentDownloadInfo = new TorrentDownloadInfo(hash, downloaded, downloadSpeed, progress, name, DownloadState.DOWNLOADED,
                    isPause, time);

            torrentDownloadInfos.add(torrentDownloadInfo);
        }

        return torrentDownloadInfos;
    }
}
