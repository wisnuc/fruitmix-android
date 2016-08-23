package com.winsun.fruitmix.util;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.winsun.fruitmix.R;
import com.winsun.fruitmix.component.BigLittleImageView;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.model.Comment;
import com.winsun.fruitmix.services.LocalPhotoUploadService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2016/4/22.
 */
public class LocalCache {

    public static String CacheRootPath;
    static Application CurrentApp;

    public static int ScreenWidth, ScreenHeight;

    static Bitmap BigPic = null;
    static ImageView HotIV = null;
    static Map<String, Object> HotImage = null;

    public static ConcurrentMap<String, Map<String, String>> DocumentsMap = null;
    public static ConcurrentMap<String, Map<String, String>> AlbumsMap = null;
    public static ConcurrentMap<String, Map<String, String>> PhotoLinksMap = null;
    public static ConcurrentMap<String, Map<String, String>> UsersMap = null;
    public static ConcurrentMap<String, Map<String, String>> MediasMap = null;
    public static ConcurrentMap<String, Map<String, String>> LocalImagesMap = null; // 本地图片缓存(thumb为key)
    public static ConcurrentMap<String, Map<String, String>> LocalImagesMap2 = null;// 本地图片缓存副本（uuid为key）

    public static String DeviceID = null;

//    public static Map<String, Map<String, String>> Images=null;
//    public static Map<String, Map<String, String>> Shares=null;

    public static Map<String, Object> TransActivityContainer;

    public static boolean DeleteFile(File file) {
        File[] files;
        int i;

        if (file.isDirectory()) {
            files = file.listFiles();
            for (i = 0; i < files.length; i++) {
                //Log.d("winsun", "AA "+files[i]);
                if (file.isFile() || file.isDirectory()) DeleteFile(files[i]);
            }
        }

        return file.delete();
    }

    public static void CleanAll() {
        File file1;
        // reset cache
        file1 = new File(CacheRootPath + "/thumbCache");
        DeleteFile(file1);

        file1 = new File(CacheRootPath + "/thumbCache");
        if (!file1.exists()) file1.mkdirs();
        Log.d("winsun", "Cache: " + file1);

        file1 = new File(CacheRootPath + "/innerCache");
        if (!file1.exists()) file1.mkdirs();
        Log.d("winsun", "Cache: " + file1);

        // Upload Record Table
        // LocalCache.DropGlobalData("localHashMap");
        // Document Hash
        LocalCache.DropGlobalData("documentsMap");
        LocalCache.DropGlobalData("albumsMap");
        LocalCache.DropGlobalData("photosMap");
        LocalCache.DropGlobalData("usersMap");
        LocalCache.DropGlobalData("mediasMap");
        LocalCache.DropGlobalData("localHashMap");
        LocalCache.DropGlobalData("deviceID");
        LocalCache.DropGlobalData(Util.LOCAL_COMMENT_MAP);

        DocumentsMap = LocalCache.GetGlobalHashMap("documentsMap");
        UsersMap = LocalCache.GetGlobalHashMap("usersMap");
        AlbumsMap = LocalCache.GetGlobalHashMap("albumsMap");
        PhotoLinksMap = LocalCache.GetGlobalHashMap("photolinksMap");
        MediasMap = LocalCache.GetGlobalHashMap("mediasMap");
        LocalImagesMap = LocalCache.GetGlobalHashMap("localImagesMap");
        BuildLocalImagesMaps2();

        DeviceID = null;

        DBUtils dbUtils = DBUtils.SINGLE_INSTANCE;
        dbUtils.deleteAllLocalShare();
        dbUtils.deleteAllLocalComment();
        dbUtils.deleteAllRemoteComment();
        //
    }


    public static void BuildLocalImagesMaps2() {
        Map<String, String> itemRaw;

        LocalCache.LocalImagesMap2 = new ConcurrentHashMap<>();
        for (String key : LocalCache.LocalImagesMap.keySet()) {
            itemRaw = LocalCache.LocalImagesMap.get(key);
            LocalCache.LocalImagesMap2.put(itemRaw.get("uuid"), itemRaw);
        }
    }

    public static boolean Init(Activity activity) {
        File file, file1;
        boolean b;
        Map<String, String> item;

        CurrentApp = activity.getApplication();
        file = activity.getApplication().getExternalCacheDir();
        if (file == null) file = activity.getApplication().getCacheDir();
        if (!file.exists() && !file.mkdirs()) {
            file = activity.getApplication().getFilesDir();
            if (!file.exists()) file.mkdirs();
        }

        CacheRootPath = "" + file;

        file1 = new File(CacheRootPath + "/innerCache");
        DeleteFile(file1);

        file1 = new File(CacheRootPath + "/thumbCache");
        if (!file1.exists()) file1.mkdirs();
        Log.d("winsun", "Cache: " + file1);

        file1 = new File(CacheRootPath + "/innerCache");
        if (!file1.exists()) file1.mkdirs();
        Log.d("winsun", "Cache: " + file1);

        TransActivityContainer = new HashMap<String, Object>();

        DocumentsMap = LocalCache.GetGlobalHashMap("documentsMap");
        UsersMap = LocalCache.GetGlobalHashMap("usersMap");
        AlbumsMap = LocalCache.GetGlobalHashMap("albumsMap");
        PhotoLinksMap = LocalCache.GetGlobalHashMap("photolinksMap");
        MediasMap = LocalCache.GetGlobalHashMap("mediasMap");
        LocalImagesMap = LocalCache.GetGlobalHashMap("localImagesMap");
        BuildLocalImagesMaps2();
        DeviceID = LocalCache.GetGlobalData("deviceID");

        Log.i("LocalCache", DocumentsMap.toString());
        Log.i("LocalCache", UsersMap.toString());
        Log.i("LocalCache", AlbumsMap.toString());
        Log.i("LocalCache", MediasMap.toString());
        Log.i("LocalCache", LocalImagesMap.toString());


//        Images=new HashMap<String, Map<String, String>>();
//        Shares=new HashMap<String, Map<String, String>>();
//        item=new HashMap<String, String>();
//        Shares.put("ss", item);
//        item=new HashMap<String, String>();
//        Shares.put("ss1", item);

        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        ScreenWidth = display.getWidth();
        ScreenHeight = display.getHeight();

        return true;
    }

    public static String GetInnerTempFile() {
        return CacheRootPath + "/innerCache/" + ("" + Math.random()).replace(".", "");
    }

    public static boolean MoveTempFileToThumbCache(String tempFile, String key) {
        new File(tempFile).renameTo(new File(CacheRootPath + "/thumbCache/" + key));

        return true;
    }

    public static void LoadLocalData() {

        List<Map<String, String>> localPhotoList;
        int i;
        Map<String, String> itemRaw, item;

        localPhotoList = LocalCache.PhotoList("Camera");

        for (i = 0; i < localPhotoList.size(); i++) {
            itemRaw = localPhotoList.get(i);
            if (LocalImagesMap.containsKey(itemRaw.get("thumb"))) continue;

            String uuid = Util.CalcSHA256OfFile(itemRaw.get("thumb"));

            item = new HashMap<String, String>();
            item.put("thumb", itemRaw.get("thumb"));
            item.put("width", itemRaw.get("width"));
            item.put("height", itemRaw.get("height"));
            item.put("mtime", itemRaw.get("lastModified"));
            item.put(Util.KEY_LOCAL_PHOTO_UPLOAD_SUCCESS,"false");
            item.put("uuid", uuid);
            LocalImagesMap.put(itemRaw.get("thumb"), item);
            LocalImagesMap2.put(uuid, item);
        }

        LocalCache.SetGlobalHashMap("localImagesMap", LocalCache.LocalImagesMap);
        Log.d("winsun", "LocalImagesMap " + LocalCache.LocalImagesMap);

        LocalPhotoUploadService.startActionUploadLocalPhoto(Util.APPLICATION_CONTEXT);
    }

    /*
    public static void LoadLocalBitmap(final String key, final ImageView iv) {

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object...params) {
                String path;
                Bitmap bmp;

                if(key.startsWith("/")) path=key;
                else path=CacheRootPath+"/thumbCache/"+key;
                bmp= BitmapFactory.decodeFile(path);

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                try {
                    iv.setImageBitmap(bmp);
                } catch (NullPointerException e) { e.printStackTrace(); }
            }

        }.execute();

    }*/

    // get thumb bitmap
    public static void LoadRemoteBitmapThumb(final String key, final int width, final int height, final ImageView iv) {

        if (key == null) return;

        try {
            new AsyncTask<Object, Object, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Object... params) {
                    Bitmap bmp;
                    String key1, path1;
                    int sFind;


                    key1 = CalcDegist(key + "?" + width + "X" + height);
                    path1 = CacheRootPath + "/thumbCache/" + key1;
                    if (!new File(path1).exists()) {
                        sFind = FNAS.RetrieveFNASFile("/media/" + key + "?type=thumb&width=" + width + "&height=" + height, key1);
                        if (sFind == 0) return null;
                    }
                    bmp = BitmapFactory.decodeFile(path1);

                    return bmp;
                }

                @Override
                protected void onPostExecute(Bitmap bmp) {
                    try {
                        if (bmp != null) {
                            iv.setImageBitmap(bmp);
                            if (iv instanceof BigLittleImageView)
                                ((BigLittleImageView) iv).bigPic = bmp;
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (java.util.concurrent.RejectedExecutionException e) {
            e.printStackTrace();
        }
    }


    //get full size bitmap
    public static void LoadRemoteBitmap(final String key, final BigLittleImageView iv) {

        if (key == null) return;

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bmp;
                String path;
                BitmapFactory.Options bmpOptions;
//Log.d("winsun", "NAS: "+key);
                BigLittleImageView.HotView2 = iv;
                path = CacheRootPath + "/thumbCache/" + key;
                if (!new File(path).exists()) {
                    FNAS.RetrieveFNASFile("/media/" + key + "?type=original", key);
                }

                if (BigLittleImageView.HotView2 == iv) {
                    bmpOptions = new BitmapFactory.Options();
                    bmpOptions.inJustDecodeBounds = true;
                    bmp = BitmapFactory.decodeFile(path, bmpOptions);
                    if (bmpOptions.outWidth > 4096 || bmpOptions.outHeight > 4096)
                        bmpOptions.inSampleSize = 2;
                    bmpOptions.inJustDecodeBounds = false;
                    bmp = BitmapFactory.decodeFile(path, bmpOptions);
                } else
                    bmp = null;

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                if (bmp == null) return;
                if (BigLittleImageView.HotView2 != iv) {
                    //bmp.recycle();
                    bmp = null;
                    return;
                }
                try {
                    if (BigLittleImageView.HotView != null) {
                        if (BigLittleImageView.HotView.bigPic != null) {
                       /*     BigLittleImageView.HotView.bigPic.recycle();
                            BigLittleImageView.HotView.bigPic=null;
                            */
                        }
                        //BigLittleImageView.HotView.loadSmallPic();

                        //BigLittleImageView.HotView.setImageResource(R.drawable.yesshou);
                        System.gc();
                        Log.d("winsun", "Recycled");
                    }
                    iv.setImageBitmap(bmp);

                    iv.bigPic = bmp;
                    BigLittleImageView.HotView = iv;

                    if (iv.handler != null) iv.handler.sendEmptyMessage(100);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public static String CalcDegist(String str) {
        MessageDigest mdInst;
        byte[] digest;
        String result;
        int i;
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(str.getBytes());
            digest = mdInst.digest();
            result = "";
            for (i = 0; i < 8; i++) {
                result += hexDigits[(digest[i] >> 4) & 0xf];
                result += hexDigits[digest[i] & 0xf];
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void LoadLocalBitmapThumb(final String path, final int width, final int height, final ImageView iv) {

//        // use cachethreadpool instead of asynctask
//        DBUtils.SINGLE_INSTANCE.doOneTaskInCachedThread(new Runnable() {
//            @Override
//            public void run() {
//                String key, path1, pathTemp;
//                Bitmap bmp;
//                BitmapFactory.Options bmpOptions;
//                int inSampleSize1, inSampleSize2;
//                FileOutputStream fout;
//
//                try {
//
//                    key = CalcDegist(path + "?" + width + "X" + height);
//                    path1 = CacheRootPath + "/thumbCache/" + key;
//                    Log.i("LocalCache",p1)
//                    if (!new File(path1).exists()) {
//                        bmpOptions = new BitmapFactory.Options();
//                        bmpOptions.inJustDecodeBounds = true;
//                        bmp = BitmapFactory.decodeFile(path, bmpOptions);
//                        inSampleSize1 = bmpOptions.outWidth / width / 2;
//                        inSampleSize2 = bmpOptions.outHeight / height / 2;
//                        bmpOptions.inSampleSize = inSampleSize1 > inSampleSize2 ? inSampleSize2 : inSampleSize1;
//                        bmpOptions.inJustDecodeBounds = false;
//                        bmp = BitmapFactory.decodeFile(path, bmpOptions);
//                        pathTemp = GetInnerTempFile();
//                        fout = new FileOutputStream(new File(pathTemp));
//                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fout);
//                        fout.flush();
//                        fout.close();
//                        MoveTempFileToThumbCache(pathTemp, key);
//
//                        final Bitmap bitmap = bmp;
//
//                        iv.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    if (iv.getTag() != null && iv.getTag().equals(path)) {
//                                        iv.setImageBitmap(bitmap);
//                                    }
//                                } catch (NullPointerException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//
//                    } else {
////                        Log.d("winsun", "Cached!");
//                        final Bitmap bitmap = BitmapFactory.decodeFile(path1);
//
//                        iv.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    if (bitmap != null && iv.getTag() != null && iv.getTag().equals(path)) {
//                                        iv.setImageBitmap(bitmap);
//                                    }
//                                } catch (NullPointerException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                String key, path1, pathTemp;
                Bitmap bmp;
                BitmapFactory.Options bmpOptions;
                int inSampleSize1, inSampleSize2;
                FileOutputStream fout;

                try {

                    key = CalcDegist(path + "?" + width + "X" + height);
                    path1 = CacheRootPath + "/thumbCache/" + key;
                    if (!new File(path1).exists()) {
                        bmpOptions = new BitmapFactory.Options();
                        bmpOptions.inJustDecodeBounds = true;
                        bmp = BitmapFactory.decodeFile(path, bmpOptions);
                        inSampleSize1 = bmpOptions.outWidth / width / 2;
                        inSampleSize2 = bmpOptions.outHeight / height / 2;
                        bmpOptions.inSampleSize = inSampleSize1 > inSampleSize2 ? inSampleSize2 : inSampleSize1;
                        bmpOptions.inJustDecodeBounds = false;
                        bmp = BitmapFactory.decodeFile(path, bmpOptions);
                        pathTemp = GetInnerTempFile();
                        fout = new FileOutputStream(new File(pathTemp));
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, fout);
                        fout.flush();
                        fout.close();
                        MoveTempFileToThumbCache(pathTemp, key);
                        return bmp;
                    } else {
//                        Log.d("winsun", "Cached!");
                        return BitmapFactory.decodeFile(path1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            protected void onPostExecute(Bitmap bmp) {
                try {
                    if (bmp != null) {
                        iv.setImageBitmap(bmp);
                        if (iv instanceof BigLittleImageView)
                            ((BigLittleImageView) iv).bigPic = bmp;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public static void LoadLocalBitmap(final String path, final BigLittleImageView iv) {

        new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... params) {
                Bitmap bmp;

                try {
                    bmp = BitmapFactory.decodeFile(path);
                    return bmp;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                try {
                    /*
                    if(BigLittleImageView.HotView!=null) {
                        BigLittleImageView.HotView.bigPic.recycle();
                        BigLittleImageView.HotView.bigPic=null;
                        BigLittleImageView.HotView.loadSmallPic();
                        //BigLittleImageView.HotView.setImageResource(R.drawable.yesshou);
                        System.gc();
                        Log.d("winsun", "Recycled");
                    }*/
                    iv.setImageBitmap(bmp);
                    /*
                    Matrix matrix;
                    matrix=new Matrix();
                    matrix.postRotate(30.0f);
                    iv.setScaleType(ImageView.ScaleType.MATRIX);
                    iv.setImageMatrix(matrix);
                    */
                    iv.bigPic = bmp;
                    BigLittleImageView.HotView = iv;

                    if (iv.handler != null) iv.handler.sendEmptyMessage(100);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    //get bucket photo list
    public static List<Map<String, String>> PhotoBucketList() {
        ContentResolver cr;
        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor;
        Map<String, Map<String, String>> bucketMap;
        List<Map<String, String>> bucketList;
        Map<String, String> bucket;
        File f;
        SimpleDateFormat df;
        Calendar date;

        cr = CurrentApp.getContentResolver();
        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, null, null, null);

        bucketList = new ArrayList<Map<String, String>>();
        if (!cursor.moveToFirst()) return bucketList;

        bucketMap = new HashMap<String, Map<String, String>>();

        do {
            bucket = bucketMap.get(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
            if (bucket == null) {
                bucket = new HashMap<String, String>();
                bucket.put("name", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
                bucket.put("thumb", "");
                bucket.put("lastModified", "0");
                bucketMap.put(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)), bucket);
            }
            f = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            if (f.lastModified() > Long.parseLong(bucket.get("lastModified"))) {
                bucket.put("lastModified", f.lastModified() + "");
                bucket.put("thumb", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            }
        }
        while (cursor.moveToNext());

        cursor.close();

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();
        for (Map.Entry<String, Map<String, String>> entry : bucketMap.entrySet()) {
            date.setTimeInMillis(Long.parseLong(entry.getValue().get("lastModified")));
            entry.getValue().put("lastModified", df.format(date.getTime()));
            bucketList.add(entry.getValue());
        }

        return bucketList;
    }


    public static List<Map<String, String>> PhotoList(String bucketName) {
        ContentResolver cr;
        String[] fields = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.HEIGHT, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor;
        List<Map<String, String>> imageList;
        Map<String, String> image;
        File f;
        SimpleDateFormat df;
        Calendar date;


        cr = CurrentApp.getContentResolver();
        cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fields, MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "='" + bucketName + "'", null, null);

        imageList = new ArrayList<Map<String, String>>();
        if (!cursor.moveToFirst()) return imageList;

        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Calendar.getInstance();

        do {
            image = new HashMap<String, String>();
            image.put("title", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)));
            image.put("bucket", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
            image.put("thumb", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            image.put("width", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
            image.put("height", cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));
            f = new File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            date.setTimeInMillis(f.lastModified());
            image.put("lastModified", df.format(date.getTime()));
            imageList.add(image);
        }
        while (cursor.moveToNext());

        cursor.close();

        return imageList;
    }

    public static String GetGlobalData(String name) {
        SharedPreferences sp;
        sp = CurrentApp.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        return sp.getString(name, null);
    }

    public static void SetGlobalData(String name, String data) {
        SharedPreferences sp;
        SharedPreferences.Editor mEditor;

        sp = CurrentApp.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putString(name, data);
        mEditor.commit();
    }

    public static void DropGlobalData(String name) {
        SharedPreferences sp;
        SharedPreferences.Editor mEditor;

        sp = CurrentApp.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        mEditor = sp.edit();
        mEditor.putString(name, null);
        mEditor.commit();
    }

    public static ConcurrentMap<String, Map<String, String>> GetGlobalHashMap(String name) {
        String strData;
        ObjectInputStream ois;
        ConcurrentHashMap<String, Map<String, String>> dataList;

        try {
            strData = GetGlobalData(name);
            if (strData.equals("")) return new ConcurrentHashMap<>();
            ois = new ObjectInputStream(new ByteArrayInputStream(Base64.decode(strData, Base64.DEFAULT)));
            dataList = (ConcurrentHashMap<String, Map<String, String>>) ois.readObject();
            ois.close();
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ConcurrentHashMap<>();
        }
    }

    public static void SetGlobalHashMap(String name, Map<String, Map<String, String>> data) {
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.reset();
            oos.writeObject(data);
            oos.close();
            SetGlobalData(name, new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearGatewayUuidPasswordToken(Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.GATEWAY, null);
        editor.putString(Util.USER_UUID, null);
        editor.putString(Util.PASSWORD, null);
        editor.putString(Util.JWT, null);
        editor.apply();
    }

    public static void clearToken(Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.JWT, null);
        editor.apply();
    }


    public static void saveGateway(String gateway, Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.GATEWAY, gateway);
        editor.apply();
    }

    public static String getGateway(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);

        return sp.getString(Util.GATEWAY, null);
    }

    public static void saveJwt(String jwt, Context context) {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putString(Util.JWT, jwt);
        editor.apply();
    }

    public static String getJWT(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);

        return sp.getString(Util.JWT, null);
    }

    public static String getUuidValue(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        return sp.getString(Util.USER_UUID, null);
    }

    public static String getPasswordValue(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        return sp.getString(Util.PASSWORD, null);
    }

    public static String getUserNameValue(Context context) {
        SharedPreferences sp;
        sp = context.getSharedPreferences("fruitMix", Context.MODE_PRIVATE);
        return sp.getString(Util.EQUIPMENT_CHILD_NAME, null);
    }
}
