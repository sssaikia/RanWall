package com.sstudio.ranwall;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.sstudio.Wallpaper;
import com.sstudio.ranwall.pojo.Updt;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Button button;
    private RequestQueue queue;
    Context context;
    String url, title, name, location, user, json, camera, modelNo, downloadLink, morePhotoessByPhotographer, instaUsername,
            heihgt, width, date, rawPhoto;
    Wallpaper wallpaper;
    static MainActivity instance;
    TextView normal, hd;
    BottomSheetBehavior bottomSheetBehavior;
    SwitchCompat switchCompat;
    Bitmap img;
    //private AdView mAdView;
    //InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        instance = this;
        queue = Volley.newRequestQueue(MainActivity.this);
        refresh();
        checkForUpdate();
        LinearLayout bottomSheetViewgroup = (LinearLayout) findViewById(R.id.linearLay);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetViewgroup);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setPeekHeight(56);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                normal = bottomSheet.findViewById(R.id.download);
                hd = bottomSheet.findViewById(R.id.downloadHD);
                ((TextView) bottomSheet.findViewById(R.id.feedback)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(context, "working", Toast.LENGTH_SHORT).show();
                       /* Uri uri = Uri.parse("https://t.me/joinchat/IWeioxJM0vRvGhO5rOgCEw"); // missing 'http://' will cause crashed
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);*/
                    }
                });
                //((SwitchCompat) bottomSheet.findViewById(R.id.toggle)).setChecked(true);
                switchCompat = ((SwitchCompat) bottomSheet.findViewById(R.id.toggle));
                switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        //Toast.makeText(context, "working", Toast.LENGTH_SHORT).show();
                        if (b) {
                            Intent startIntent = new Intent(context, RanWallService.class);
                            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                            context.startService(startIntent);
                        } else {
                            Toast.makeText(context, "May not work properly", Toast.LENGTH_SHORT).show();
                            Intent stopIntent = new Intent(context, RanWallService.class);
                            stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
                            context.startService(stopIntent);
                        }
                    }
                });
                normal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                        fadeIn.setDuration(1000);
                        fadeIn.setRepeatCount(Animation.INFINITE);
                        AnimationSet animation = new AnimationSet(false); //change to false
                        animation.addAnimation(fadeIn);
                        animation.setRepeatMode(Animation.REVERSE);
                        normal.setAnimation(animation);
                        normal.startAnimation(animation);

                        if (img==null){
                        ImageRequest request = new ImageRequest(url,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        normal.clearAnimation();
                                        //Toast.makeText(context, "Downloading", Toast.LENGTH_SHORT).show();
                                        SaveImage(title, bitmap, normal);
                                    }
                                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                        normal.clearAnimation();
                                        Toast.makeText(MainActivity.this, "Error! swipe down to refresh", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        queue.add(request);
                        }else{
                            normal.clearAnimation();
                            SaveImage(title, img, normal);
                        }
                    }
                });
                hd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                        fadeIn.setDuration(1000);
                        fadeIn.setRepeatCount(Animation.INFINITE);
                        AnimationSet animation = new AnimationSet(false); //change to false
                        animation.addAnimation(fadeIn);
                        animation.setRepeatMode(Animation.REVERSE);
                        hd.setAnimation(animation);
                        hd.startAnimation(animation);
                        //Toast.makeText(context, "Working", Toast.LENGTH_SHORT).show();
                        ImageRequest request = new ImageRequest(rawPhoto,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        hd.clearAnimation();
                                        SaveImage(title, bitmap, hd);
                                        //Toast.makeText(context, "Downloading", Toast.LENGTH_SHORT).show();
                                    }
                                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                        hd.clearAnimation();
                                        Toast.makeText(MainActivity.this, "Error! swipe down to refresh", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        queue.add(request);
                    }
                });
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    ((TextView) findViewById(R.id.swipeUp)).setText("Collapse");
                } else if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)) {
                    ((TextView) findViewById(R.id.swipeUp)).setText("Expand");
                } else {
                    ((TextView) findViewById(R.id.swipeUp)).setText("•••");
                }
                ((TextView) findViewById(R.id.swipeUp)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        } else {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                    }
                });
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        ((RelativeLayout) findViewById(R.id.ww)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        ((SwipeRefreshLayout) findViewById(R.id.refresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        //registering alarm
        Intent ishintent = new Intent(getApplicationContext(), RanWallService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, ishintent, 0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        //alarm.cancel(pintent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 600000, pintent);

        //new BottomClass().show(getSupportFragmentManager(),"Bottom");
       /* MobileAds.initialize(this, getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.banner_ad_unit_id_2));

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mInterstitialAd.loadAd(adRequest2);*/
    }

    int refreshCount = 0;

    public void refresh() {
        Intent startIntent = new Intent(MainActivity.this, RanWallService.class);
        if (refreshCount > 0) {
            startIntent.setAction(Constants.ACTION.DOWNLOAD_IMAGE);
        } else {
            startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        }
        refreshCount++;
        startService(startIntent);
        if (normal != null) {
            normal.setBackgroundResource(R.drawable.detailsback);
            normal.setText("Download jpg");
            hd.setText("Download HD");
            hd.setBackgroundResource(R.drawable.detailsback);
            switchCompat.setChecked(true);
        }
    }

    public void checkForUpdate() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "https://sssaikia.github.io/update.json",
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new Gson();
                final Updt update = gson.fromJson(String.valueOf(response), Updt.class);
                try {

                    Log.d("Mainactivity", "Version Code: " + (getApplicationContext().getPackageManager()
                            .getPackageInfo(getApplicationContext()
                                    .getPackageName(), 0).versionName));

                    if ((Float.valueOf(update.getVersion()) > Float.valueOf(getApplicationContext().getPackageManager()
                            .getPackageInfo(getApplicationContext()
                                    .getPackageName(), 0).versionName))) {
                        //Toast.makeText(context, "Upddate available.", Toast.LENGTH_SHORT).show();
                        (findViewById(R.id.download)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.download)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri = Uri.parse(update.getUrl()); // missing 'http://' will cause crashed
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }


    private void SaveImage(String title, Bitmap finalBitmap, TextView textView) {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myDir = new File(root + "/RanWall");
            myDir.mkdirs();

            Calendar calendar = Calendar.getInstance();
            calendar.getTimeInMillis();
            String fname = title + "_" + ((System.currentTimeMillis() / 1000)) + ".jpg";
            File file = new File(myDir, fname);
            if (file.exists()) file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                //Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                textView.setText("Downloaded");
                textView.setBackgroundResource(R.drawable.downloaded);
                //todo ad adds
               /* if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 012);
        }
    }

    public void walObjUpdateFromService(Wallpaper obj, Bitmap wall) {
        wallpaper = obj;
        img=wall;
        //Toast.makeText(context, "object updated", Toast.LENGTH_SHORT).show();
        try {
            //wallpaper = gson.fromJson(json, Wallpaper.class);
            url = wallpaper.getUrls().getRegular();
            title = wallpaper.getLocation().getTitle();
            name = wallpaper.getLocation().getName();
            location = wallpaper.getLocation().getCountry();
            user = wallpaper.getUser().getName();
            camera = wallpaper.getExif().getMake() + "";
            modelNo = wallpaper.getExif().getModel() + "";
            downloadLink = wallpaper.getLinks().getDownload();
            instaUsername = wallpaper.getUser().getInstagramUsername() + "";
            heihgt = wallpaper.getHeight() + "";
            width = wallpaper.getWidth() + "";
            date = wallpaper.getUpdatedAt();
            rawPhoto = wallpaper.getUrls().getFull();
            ((TextView) findViewById(R.id.data)).setText("Title: " + title + "\nName: " + name
                    + "\nLocation: " + location + "\nPhoto by: " + user + "\nCamera: " + camera + "\nModel no: " + modelNo +
                    "\nResulution: " + heihgt + "x" + width + "\nDate: " + date);
        } catch (Exception e) {
            try {
                url = wallpaper.getUrls().getRegular();
                title = "Not available";
                name = "Not available";
                location = "Not available";
                user = wallpaper.getUser().getName();
                camera = wallpaper.getExif().getMake() + "";
                modelNo = wallpaper.getExif().getModel() + "";
                downloadLink = wallpaper.getLinks().getDownload();
                instaUsername = wallpaper.getUser().getInstagramUsername() + "";
                heihgt = wallpaper.getHeight() + "";
                width = wallpaper.getWidth() + "";
                date = wallpaper.getUpdatedAt();
                rawPhoto = wallpaper.getUrls().getFull();
                ((TextView) findViewById(R.id.data)).setText("Title: " + title + "\nName: " + name
                        + "\nLocation: " + location + "\nPhoto by: " + user + "\nCamera: " + camera + "\nModel no: " + modelNo +
                        "\nResulution: " + heihgt + "x" + width + "\nDate: " + date);
            } catch (Exception e1) {
                url = "https://images.unsplash.com/photo-1463438081263-6852d273b5f2?ixlib=rb-0.3" +
                        ".5&q=80&fm=jpg&crop=entropy&cs=tinysrgb&w=1080&fit=max&ixid=eyJhcHBfaWQi" +
                        "OjI2NjY2fQ&s=f347b7de33f97ad0166ea630185ef9e3";
                e1.printStackTrace();
            }
        }
        ((ImageView) findViewById(R.id.wall)).setImageBitmap(wall);
        ((SwipeRefreshLayout) findViewById(R.id.refresh)).setRefreshing(false);
        ((TextView) findViewById(R.id.title)).setText("Title: " + title);
        ((TextView) findViewById(R.id.name)).setText("Name: " + name);
        ((TextView) findViewById(R.id.location)).setText("Country: " + location);
        ((TextView) findViewById(R.id.user)).setText("Captured by: " + user);
        /*ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        //((ImageView) findViewById(R.id.wall)).setImageBitmap(bitmap);
                        ((SwipeRefreshLayout) findViewById(R.id.refresh)).setRefreshing(false);
                        ((TextView) findViewById(R.id.title)).setText("Title: " + title);
                        ((TextView) findViewById(R.id.name)).setText("Name: " + name);
                        ((TextView) findViewById(R.id.location)).setText("Country: " + location);
                        ((TextView) findViewById(R.id.user)).setText("Captured by: " + user);
                    }
                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        ((SwipeRefreshLayout) findViewById(R.id.refresh)).setRefreshing(false);
                    }
                });
        queue.add(request);*/
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            context = null;
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        queue.stop();
        queue.getCache().clear();
        instance=null;
        context=null;
        refreshCount = 0;
        super.onDestroy();
    }
}
