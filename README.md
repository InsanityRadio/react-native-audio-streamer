# <img src="https://raw.githubusercontent.com/InsanityRadio/OnAirController/master/doc/headphones_dark.png" align="left" height=48 /> react-native-audio-streamer

A react-native audio streaming module which works on both iOS & Android, designed for live radio playback.

The module supports DASH and HLS natively on Android, and HLS natively on iOS. It also supports more traditional Icecast streams on all platforms.

iOS streaming is based on [FRadioPlayer](https://github.com/fethica/FRadioPlayer)

Android streaming is based on [ExoPlayer](https://github.com/google/ExoPlayer)

### Why?

This custom version of react-native-audio-streamer was built for Insanity Radio's open source radio player. We needed support for adaptive streaming to provide resilience to weird network conditions. 

The original code was taken from indiecastfm's [module of the same name](https://github.com/indiecastfm/react-native-audio-streamer), and from sveint's fork [react-native-music-streamer](https://github.com/sveint/react-native-music-streamer], to give us the "best of both worlds". 

This module makes no effort to support metadata. We're loading now playing information over WebSocket so this is irrelevant for us. 

## Installation

`npm install https://github.com/InsanityRadio/react-native-audio-streamer --save`

Then run the following command to link to iOS & Android project

`react-native link react-native-audio-streamer`

### Android Steps

You need to register the module's service. Edit your `android/app/src/main/AndroidManifest.xml` file and add the following:

      <service
        android:name="com.insanityradio.rnaudiostreamer.MusicStreamerService"
        android:label="@string/app_name">
      </service>

## Usage

### Basic

```javascript
import RNAudioStreamer from 'react-native-audio-streamer';

RNAudioStreamer.setUrl('https://scdnc.insanityradio.com/dash/hls/insanity/index.m3u8')
RNAudioStreamer.play()
RNAudioStreamer.pause()

// Player Status:
// - PLAYING
// - PAUSED
// - STOPPED
// - FINISHED
// - BUFFERING
// - ERROR
RNAudioStreamer.status((err, status)=>{
 if(!err) console.log(status)
})

```

### Status Change Observer

```javascript
const {
  DeviceEventEmitter
} = 'react-native'

// Status change observer
componentDidMount() {
    this.subscription = DeviceEventEmitter.addListener('RNAudioStreamerStatusChanged',this._statusChanged.bind(this))
}

// Player Status:
// - PLAYING
// - PAUSED
// - STOPPED
// - FINISHED
// - BUFFERING
// - ERROR
_statusChanged(status) {
  // Your logic
}
```

### Development

Feel free to make use of this project in your own software. If you're developing 'native' iOS and Android apps, see the associated projects on both Android and iOS platforms, as this is where the core logic lies. The 'bridge' code should provide a rough interim guide as to how to implement the player, whilst we work on documentation and refactoring the code & structure. 


### Credits

https://github.com/indiecastfm/react-native-audio-streamer
https://github.com/sveint/react-native-music-streamer


