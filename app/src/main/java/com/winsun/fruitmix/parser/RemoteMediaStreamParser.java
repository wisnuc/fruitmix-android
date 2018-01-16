package com.winsun.fruitmix.parser;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.winsun.fruitmix.mediaModule.model.Media;
import com.winsun.fruitmix.mediaModule.model.Video;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/26.
 */

public class RemoteMediaStreamParser implements RemoteDataStreamParser<Media> {

    @Override
    public List<Media> parse(InputStream inputStream) throws JSONException {

        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream));

        List<Media> medias = new ArrayList<>();

        try {
            JsonToken jsonToken = jsonReader.peek();

            if (jsonToken == JsonToken.BEGIN_OBJECT) {

                jsonReader.beginObject();

                while (jsonReader.hasNext()) {

                    String name = jsonReader.nextName();
                    if (name.equals("data")) {

                        readMedia(jsonReader, medias);
                    } else
                        jsonReader.skipValue();

                }

                jsonReader.endObject();


            } else if (jsonToken == JsonToken.BEGIN_ARRAY) {

                readMedia(jsonReader, medias);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }


        return medias;
    }

    private void readMedia(JsonReader jsonReader, List<Media> medias) throws IOException {

        jsonReader.beginArray();

        while (jsonReader.hasNext()) {

            Media media;

            jsonReader.beginObject();

            String type = "";
            double durationSec = 0;
            long size = 0;
            String hash = "";
            String width = "";
            String height = "";
            String date = "";
            String dateTime = "";
            int orientationNumber = 0;

            while (jsonReader.hasNext()) {

                String name = jsonReader.nextName();

                switch (name) {
                    case "m":
                        type = jsonReader.nextString();
                        break;
                    case "dur":
                        durationSec = jsonReader.nextDouble();
                        break;
                    case "size":
                        size = jsonReader.nextLong();
                        break;
                    case "hash":
                        hash = jsonReader.nextString();
                        break;
                    case "w":
                        width = jsonReader.nextString();
                        break;
                    case "h":
                        height = jsonReader.nextString();
                        break;
                    case "date":
                        date = jsonReader.nextString();
                        break;
                    case "datetime":
                        dateTime = jsonReader.nextString();
                        break;
                    case "orient":
                        orientationNumber = jsonReader.nextInt();
                        break;
                    default:
                        jsonReader.skipValue();

                }

            }

            jsonReader.endObject();

            if (type.equals("MOV") || type.equals("MP4") || type.equals("3GP")) {

                media = new Video();

                long duration = (long) (durationSec * 1000);

                ((Video) media).setDuration(duration);

                ((Video) media).setSize(size);

            } else
                media = new Media();

            media.setUuid(hash);
            media.setWidth(width);
            media.setHeight(height);

            String time = date;
            if (time.isEmpty()) {
                time = dateTime;
            }

            if (time.equals("") || time.length() < 10) {
                media.setFormattedTime(Util.DEFAULT_DATE);
            } else {

                time = time.substring(0, 10).replace(":", "-") + time.substring(10);

                media.setFormattedTime(time);

            }

            if (orientationNumber == 0)
                orientationNumber = 1;

            media.setOrientationNumber(orientationNumber);

            media.setLocal(false);

            media.setType(type);

            medias.add(media);

        }

        jsonReader.endArray();

    }


}
