# <img src="https://raw.githubusercontent.com/InsanityRadio/OnAirController/master/doc/headphones_dark.png" align="left" height=48 /> react-native-audio-streamer

A react-native audio streaming module which works on both iOS & Android, designed for live radio playback.

The module supports DASH and HLS natively on Android, and HLS natively on iOS. It also supports more traditional Icecast streams on all platforms.

iOS streaming is based on [FRadioPlayer](https://github.com/fethica/FRadioPlayer)

Android streaming is based on [ExoPlayer](https://github.com/google/ExoPlayer)

### Why?

This custom version of react-native-audio-streamer was built for Insanity Radio's open source radio player. We needed support for adaptive streaming to provide resilience to weird network conditions. 

The original code was taken from indiecastfm's [module of the same name](https://github.com/indiecastfm/react-native-audio-streamer), but is not backwards compatible.

This module makes no effort to support metadata. We're loading now playing information over WebSocket 

## Installation

`npm install https://github.com/InsanityRadio/react-native-audio-streamer --save`

Then run the following command to link to iOS & Android project

`react-native link react-native-audio-streamer`

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

