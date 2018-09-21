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
    private String tempUrl = null;
    private Boolean playWhenReady = false;
    private ReactApplicationContext reactContext = null;
    private MusicStreamerService.StatusUpdateListener updateListener = null;
    private MusicStreamerService.MetadataUpdateListener metadataListener = null;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            MusicStreamerService.LocalBinder binder = (MusicStreamerService.LocalBinder) service;
            musicService = binder.getService();
            musicServiceReady();
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
            if (playWhenReady) {
                playWhenReady = false;
                play();
            }
        } else {
            tempUrl = urlString;
        }
        
    }
   
    // If we've set the URL before 
    public void musicServiceReady () {
        if (musicService != null && tempUrl != null) {
            setUrl(tempUrl);
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
        } else {
            playWhenReady = true;
        }
    }

    @ReactMethod
    public void pause() {
        if (musicService != null) musicService.pause();
        playWhenReady = false;
    }

    @ReactMethod
    public void stop() {
        if (musicService != null) {
            musicService.stop();
        }
        playWhenReady = false;
    }

    @ReactMethod
    public void seekToTime(double time) {
        if (musicService != null) musicService.seekToTime(time);
    }

    @ReactMethod public void currentTime(Callback callback) {
        try {
            callback.invoke(null, musicService.getCurrentTime());
        } catch (Exception e) {
            callback.invoke(null, 0);
        }
    }

    @ReactMethod
    public void status(Callback callback) {
        try {
            callback.invoke(null, musicService.getStatus());
        } catch (Exception e) {
            callback.invoke(null, "STOPPED");
        }
    }

}
