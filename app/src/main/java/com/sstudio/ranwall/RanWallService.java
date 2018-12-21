package com.sstudio.ranwall;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.sstudio.Wallpaper;

import org.json.JSONObject;

import java.io.IOException;

public class RanWallService extends Service {
    MyReceiver mScreenStateReceiver;
    boolean ready = true;
    Wallpaper wallpaper;
    RequestQueue queue;
    Gson gson;
    Bitmap bitM;

    public RanWallService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mScreenStateReceiver = new MyReceiver();
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);


        gson = new Gson();
        queue = Volley.newRequestQueue(this);
        loadImage();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Constants.ACTION.DOWNLOAD_IMAGE) && ready) {
                    queue = Volley.newRequestQueue(this);
                    loadImage();
                }

                if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
                    if (MainActivity.instance != null) {
                        MainActivity.instance.walObjUpdateFromService(wallpaper, bitM);
                    }
                    startInForeground();
                } else if (intent.getAction().equals(
                        Constants.ACTION.STOPFOREGROUND_ACTION)) {
                    //Log.i(LOG_TAG, "Received Stop Foreground Intent");
                    stopForeground(true);
                    //stopSelf();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    private void startInForeground() {
        String title = " ", name = " ";
        try {
            title = wallpaper.getLocation().getTitle();
            name = "By " + wallpaper.getUser().getName();
            Log.i("Myservice", "startInForeground: " + wallpaper.toString());
        } catch (Exception e) {
            Log.e("Myservice", "startInForeground: " + (new Gson()).toJson(wallpaper), e);
            title = "RanWall";
            name = "Photo details missing";
            e.printStackTrace();
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Wall channel")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("" + title)
                .setContentText("" + name)
                .setTicker("Ready to roll")
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("Wall channel", "sstudio", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("RanWall uses a background service to download wallpapers." +
                    " To keep the service alive it is recommended to not disable the notification.");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        (RanWallService.this).startForeground(12, notification);
        notification = null;
        title = null;
        name = null;
    }

    @Override
    public void onDestroy() {
        //wakeLock.release();
        unregisterReceiver(mScreenStateReceiver);
        queue.stop();
        queue.getCache().clear();
        super.onDestroy();
    }


    public void loadImage() {
        ready = false;
        //queue = Volley.newRequestQueue(this);

        JsonObjectRequest imageRequest = new JsonObjectRequest(Request.Method.GET,
                "https://api.unsplash.com/photos/random?client_id=" +
                        "a76e445b56fa6e5d8e0b1946abf8e167af6d078d046a32f34450ebb93bd6cf7f",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String url = null;
                wallpaper = gson.fromJson(String.valueOf(response), Wallpaper.class);
                url = wallpaper.getUrls().getRegular();
                Log.d("Log", "onResponse: " + url);
                //startInForeground();
                setWall(RanWallService.this, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ready = true;
            }
        });
        queue.add(imageRequest);
    }

    public void setWall(final Context context, String url) {

        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {

                        WallpaperManager wallpaperManager =
                                WallpaperManager.getInstance(context);
                        try {
                            wallpaperManager.setBitmap(centerCropWallpaper(context, bitmap,
                                    Math.min(wallpaperManager.getDesiredMinimumWidth(),
                                            wallpaperManager.getDesiredMinimumHeight())));
                            ready = true;
                            startInForeground();
                            bitM = bitmap;
                            if (MainActivity.instance != null) {
                                MainActivity.instance.walObjUpdateFromService(wallpaper, bitmap);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            ready = true;
                        }
                        queue.stop();
                        queue.getCache().clear();
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        ready = true;
                        queue.stop();
                        queue.getCache().clear();
                    }
                });
        queue.add(request);
    }

    private Bitmap centerCropWallpaper(Context context, Bitmap wallp, int desiredHeight) {
        float scale = (float) desiredHeight / wallp.getHeight();
        int scaledWidth = (int) (scale * wallp.getWidth());
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int deviceWidth = metrics.widthPixels;
        int imageCenterWidth = scaledWidth / 2;
        int widthToCut = imageCenterWidth - deviceWidth / 2;
        int leftWidth = scaledWidth - widthToCut;
        Bitmap scaledWallpaper = Bitmap.createScaledBitmap(wallp, scaledWidth, desiredHeight, false);
        return Bitmap.createBitmap(scaledWallpaper, widthToCut, 0, leftWidth, desiredHeight);
    }
}