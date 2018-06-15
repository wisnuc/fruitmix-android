package com.winsun.fruitmix.retrieve.file.from.other.app;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.winsun.fruitmix.login.LoginUseCase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/15.
 */

public class RetrieveFileFromOtherAppUseCase {

    public static final String TAG = RetrieveFileFromOtherAppUseCase.class.getSimpleName();


    public String getUploadFilePath(Intent intent) {

        String action = intent.getAction();
        String type = intent.getType();

        if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) && type != null) {

/*            switch (type) {
                case "application/pdf":
                case "application/msword":
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                case "application/vnd.ms-excel":
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                case "application/vnd.ms-powerpoint":
                case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                case "text/plain":
                    return handleSendFile(intent);

                case "image/":
                    return handleSendFile(intent);

            }*/

            return handleSendFile(intent);

        }


        return null;

    }


    private String handleSendFile(Intent intent) {

        Uri uri = intent.getData();

        if (uri != null) {

            String path = uri.getPath();

            Log.d(TAG, "handleSendFile: file Uri: " + uri + " uri.path: " + path);

            return path;
        } else {

            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

            if (uri != null) {

                Log.d(TAG, "handleSendFile: file Uri: " + uri + " uri.path: " + uri.getPath());

                return uri.getPath();

            }

        }

        return null;
    }

    private String handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }

        return null;
    }

    private void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }


}
