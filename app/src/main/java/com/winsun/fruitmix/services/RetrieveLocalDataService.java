package com.winsun.fruitmix.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class RetrieveLocalDataService extends IntentService {
    private static final String ACTION_GET_LOCAL_DATA = "com.winsun.fruitmix.services.action.retrieve.local.data";

    private static final String EXTRA_REQUEST = "com.winsun.fruitmix.services.extra.request";

    public RetrieveLocalDataService() {
        super("GetLocalDataService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionGetLocalData(Context context, String request) {
        Intent intent = new Intent(context, RetrieveLocalDataService.class);
        intent.setAction(ACTION_GET_LOCAL_DATA);
        intent.putExtra(EXTRA_REQUEST, request);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_LOCAL_DATA.equals(action)) {
                final String request = intent.getStringExtra(EXTRA_REQUEST);
                handleActionGetLocalData(request);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionGetLocalData(String request) {
    }

}

