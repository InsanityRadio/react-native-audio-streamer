//
//  RNAudioStreamer.m
//  RNAudioStreamer
//
//  Created by Victor Chan on 29/11/2016.
//  Ported with FRadioPlayer by Insanity Radio
//  Copyright © 2016 Victor Chan and Insanity Radio. All rights reserved.
//

#import "RNAudioStreamer.h"
#import <AVFoundation/AVFoundation.h>
#import <React/RCTEventDispatcher.h>
#import "RNAudioStreamer-Swift.h"

// Player status
static NSString *PLAYING = @"PLAYING";
static NSString *PAUSED = @"PAUSED";
static NSString *STOPPED = @"STOPPED";
static NSString *FINISHED = @"FINISHED";
static NSString *BUFFERING = @"BUFFERING";
static NSString *ERROR = @"ERROR";

@interface RNAudioStreamer ()
@property(strong, nonatomic) FRadioPlayer *player;
@end

@implementation RNAudioStreamer

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(setUrl:(NSString *)urlString){

    [self killPlayer];
    
    if ([urlString isEqualToString:@""]) {
        return;
    }

    //Audio session
    NSError *err;
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:&err];
    if (!err){
        [[AVAudioSession sharedInstance] setActive:YES error:&err];
        if(err) NSLog(@"Audio session error");
    }else{
        NSLog(@"Audio session error");
    }


    NSURL *url = [[NSURL alloc] initWithString:urlString];
    
    _player = [FRadioPlayer getInstance];

    [_player setRadioURLWith:url];

    // Status observer
    
    [_player addObserver:self
              forKeyPath:@"state"
                 options:NSKeyValueObservingOptionNew
                 context:nil];

    [_player addObserver:self
              forKeyPath:@"playbackState"
                 options:NSKeyValueObservingOptionNew
                 context:nil];
}

RCT_EXPORT_METHOD(play) {
    if(_player) [_player play];
}

RCT_EXPORT_METHOD(pause) {
    if(_player) [_player pause];
}

RCT_EXPORT_METHOD(seekToTime: (double)time) {
   // if(_player) [_player setCurrentTime:time];
}

RCT_EXPORT_METHOD(duration:(RCTResponseSenderBlock)callback){
    // We don't have this data with FRadioPlayer
    callback(@[[NSNull null], @(0)]);
}

RCT_EXPORT_METHOD(currentTime:(RCTResponseSenderBlock)callback){
    // We don't have this data with FRadioPlayer
    callback(@[[NSNull null], @(0)]);
}

RCT_EXPORT_METHOD(status:(RCTResponseSenderBlock)callback){
    callback(@[[NSNull null], _player ? [self rnStatusFromFRadioStatus] : STOPPED]);
}

/**
 *  Status KVO
 */
- (void)observeValueForKeyPath:(NSString *)keyPath
                      ofObject:(id)object
                        change:(NSDictionary *)change
                       context:(void *)context {
    
    if ([keyPath isEqualToString:@"state"] || [keyPath isEqualToString:@"playbackState"]) {
        [self performSelector:@selector(statusChanged)
                     onThread:[NSThread mainThread]
                   withObject:nil
                waitUntilDone:NO];
    } else {
        [super observeValueForKeyPath:keyPath
                             ofObject:object
                               change:change
                              context:context];
    }
}

- (void)statusChanged {
    [self.bridge.eventDispatcher sendDeviceEventWithName:@"RNAudioStreamerStatusChanged"
                                                    body: _player ? [self rnStatusFromFRadioStatus] : STOPPED];
}

- (NSString *)rnStatusFromFRadioStatus {
    NSString *statusString = STOPPED;
    
    switch(_player.state) {
        case FRadioPlayerStateError:
            return ERROR;
        case FRadioPlayerStateLoading:
            return BUFFERING;
        default:
            break;
    }
    
    switch(_player.playbackState){
        case FRadioPlaybackStatePlaying:
            return PLAYING;
            break;
        case FRadioPlaybackStatePaused:
            return PAUSED;
            break;
        case FRadioPlaybackStateStopped:
            return STOPPED;
        default:
            break;
    }
    return statusString;
}

- (void)killPlayer{
  if (!_player) return;
  [_player stop];
  [_player removeObserver:self forKeyPath:@"state"];
  [_player removeObserver:self forKeyPath:@"playbackState"];
  _player = nil;
}

- (void)dealloc{
    [self killPlayer];
}

@end
