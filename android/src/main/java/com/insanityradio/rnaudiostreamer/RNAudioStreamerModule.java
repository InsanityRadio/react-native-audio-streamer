package com.insanityradio.rnaudiostreamer;

import android.content.Context;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;

import java.lang.Math;
import java.lang.Exception;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.List;

public class RNAudioStreamerModule extends ReactContextBaseJavaModule {

    MusicStreamerService musicService = null;
    private ReactApplicationContext reactContext = null;
    private MusicStreamerService.StatusUpdateListener updateListener = null;
    private MusicStreamerService.MetadataUpdateListener metadataListener = null;

    private Thread artworkThread;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            MusicStreamerService.LocalBinder binder = (MusicStreamerService.LocalBinder) service;
            musicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            musicService = null;
        }
    };

    public RNAudioStreamerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        Intent intent = new Intent(this.reactContext, MusicStreamerService.class);
        this.reactContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        final ReactApplicationContext ctx = reactContext;
        this.updateListener = new MusicStreamerService.StatusUpdateListener() {
            @Override
            public void callback(String status) {
                ctx
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("RNAudioStreamerStatusChanged", status);
            }
        };
        
        this.metadataListener = new MusicStreamerService.MetadataUpdateListener() {
            @Override
            public void callback(String result) {
                ctx
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("RNAudioStreamerMetadataChanged", result);
            }
        };
    }

    @Override
    public String getName() {
        return "RNAudioStreamer";
    }

    @ReactMethod
    public void setUrl(String urlString) {
        
        if (musicService != null) {
            musicService.setStatusUpdateListener(this.updateListener);
            musicService.setMetadataUpdateListener(this.metadataListener);
            musicService.prepare(urlString);
            musicService.enableMetadataFromStream(false);
        }
        
    }

    @ReactMethod
    public void getCurrentUrl(Promise promise) {
        try {
            promise.resolve(musicService.getCurrentUrl());
        }
        catch (Exception e) {
            promise.reject("Error", e);
        }
    }

    @ReactMethod
    public void play() {
        if (musicService != null) {
            musicService.play();
        }
    }

    @ReactMethod
    public void pause() {
        if (musicService != null) musicService.pause();
    }

    @ReactMethod
    public void stop() {
        if (musicService != null) {
            musicService.stop();
        }
    }

    @ReactMethod
    public void seekToTime(double time) {
        if (musicService != null) musicService.seekToTime(time);
    }

    @ReactMethod
    public void currentTime(Promise promise) {
        try {
            promise.resolve(musicService.getCurrentTime());
        }
        catch (Exception e) {
            promise.reject("Error", e);
        }
    }

    @ReactMethod
    public void status(Promise promise) {
        try {
            promise.resolve(musicService.getStatus());
        }
        catch (Exception e) {
            promise.reject("Error", e);
        }
    }

    @ReactMethod
    public void duration(Promise promise) {
        try {
            promise.resolve(musicService.getDuration());
        }
        catch (Exception e) {
            promise.reject("Error", e);
        }
    }


    private Bitmap loadArtwork(String url, boolean local) {
        Bitmap bitmap = null;

        try {
            if(local) {

                // Gets the drawable from the RN's helper for local resources
                ResourceDrawableIdHelper helper = ResourceDrawableIdHelper.getInstance();
                Drawable image = helper.getResourceDrawable(getReactApplicationContext(), url);

                if(image instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable)image).getBitmap();
                } else {
                    bitmap = BitmapFactory.decodeFile(url);
                }

            } else {

                // Open connection to the URL and decodes the image
                URLConnection con = new URL(url).openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(10000);
                con.connect();
                InputStream input = con.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
                input.close();

            }
        } catch(IOException ex) {
            Log.w("MusicStreamer", "Could not load the artwork", ex);
        }
        
        return bitmap;
    }
}
