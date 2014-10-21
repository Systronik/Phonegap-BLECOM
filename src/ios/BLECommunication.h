//
//  MEGBluetoothSerial.h
//  Bluetooth Serial Cordova Plugin
//
//  Created by Don Coleman on 5/21/13.
//
//

#ifndef BLECommunication_BlueRadio_h
#define BLECommunication_BlueRadio_h

#import <Cordova/CDV.h>
#import "Brsp.h"
#import "AppDelegate.h"


@interface BLECommunication : CDVPlugin <BrspDelegate, CBCentralManagerDelegate> {
    NSString* scanCallback;
    NSString* connectionCallback;
    NSString* dataCallback;
}

@property (nonatomic, strong)CBCentralManager *cBCM;

- (NSString*)hexString:(NSData*)inData;
- (void)checkAvailability:(CDVInvokedUrlCommand *)command;
- (void)scanDevices:(CDVInvokedUrlCommand *)command;

@end

#endif
