//
//  MEGBluetoothSerial.m
//  Bluetooth Serial Cordova Plugin
//
//  Created by Don Coleman on 5/21/13.
//
//

#import "BLECommunication.h"

@interface BLECommunication()

@property (strong, nonatomic) Brsp *brspObject;

@end

@implementation BLECommunication{
    
    CBPeripheral *activePeripheral;
    NSMutableArray *peripherals;
    BrspMode _lastMode;
}

- (void)pluginInitialize {
    
    NSLog(@"BLE Plugin BlueRadio Stack");
    NSLog(@"(c)2014 Systronik GmbH");
    self.cBCM = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    peripherals  = [NSMutableArray new];
    [super pluginInitialize];
}

#pragma mark - Cordova Plugin Methods

- (void)checkAvailability:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSLog(@"checkAvailability");
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"BLE enabled"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)scanDevices:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSLog(@"scanDevices");
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallbackAsBool:TRUE];
        scanCallback = [command.callbackId copy];
        
        [peripherals removeAllObjects];
        
        
        [self.cBCM scanForPeripheralsWithServices:[NSArray arrayWithObject:[Brsp brspServiceUUID]] options:nil];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)stopScanDevices:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSLog(@"stopScanDevices");
        
        [self.cBCM stopScan];
        
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Scanning stopped"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        scanCallback = nil;
    }];
}

- (void)connectDevice:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSLog(@"connectDevice");
        CDVPluginResult *pluginResult = nil;
        
        NSString *arg1 = [command.arguments objectAtIndex:0];
        
        activePeripheral = nil;
        NSString *index = [[arg1 valueForKey:@"address"] substringToIndex:2];
        NSInteger convertedIndex = [index integerValue];
        if (convertedIndex < [peripherals count]){
            activePeripheral = [peripherals objectAtIndex:convertedIndex];
        }
        
        //activePeripheral = nil;
        //for (NSUInteger i = 0; i < [peripherals count]; i++){
        //    if ([[arg1 valueForKey:@"address"] containsString:[[peripherals objectAtIndex:i] name]]){
        //        activePeripheral = [peripherals objectAtIndex:i];
        //        i = [peripherals count];
        //    }
        //}
        
        if (activePeripheral){
            //init the object with default buffer sizes of 1024 bytes
            //    self.brspObject = [[Brsp alloc] initWithPeripheral:[AppDelegate app].activePeripheral];
            //init with custom buffer sizes
            self.brspObject = [[Brsp alloc] initWithPeripheral:activePeripheral InputBufferSize:1024 OutputBufferSize:1024];
            //It is important to set this delegate before calling [Brsp open]
            self.brspObject.delegate = self;
            //Use CBCentral Manager to connect this peripheral
            [self.cBCM connectPeripheral:activePeripheral options:nil];
            connectionCallback = [command.callbackId copy];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
            [pluginResult setKeepCallbackAsBool:TRUE];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:connectionCallback];
        }
        else{
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }];
}

- (void)disconnectDevice:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSLog(@"disconnectDevices");
        CDVPluginResult *pluginResult = nil;
        if (activePeripheral){
            [self.brspObject close];
            //Use CBCentralManager to close the connection to this peripheral
            [self.cBCM cancelPeripheralConnection:activePeripheral];
            activePeripheral = nil;
            connectionCallback = [command.callbackId copy];
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:connectionCallback];
        }
        else{
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        }
        [pluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)sendData:(CDVInvokedUrlCommand*)command {
    [self.commandDelegate runInBackground:^{
        NSLog(@"sendData");
        [self.brspObject flushOutputBuffer];
        [self.brspObject flushInputBuffer];

        NSString *arg1 = [command.arguments objectAtIndex:0];
        
        NSString *data = [arg1 valueForKey:@"data"];
        NSError *writeError = [self.brspObject writeString:data];
        if (writeError){
            NSLog(@"%@", writeError.description);
        }
        dataCallback = [command.callbackId copy];
        CDVPluginResult *pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
        [pluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:dataCallback];
    }];
}

#pragma mark BrspDelegate
- (void)brsp:(Brsp*)brsp OpenStatusChanged:(BOOL)isOpen {
    NSLog(@"OpenStatusChanged == %d", isOpen);
    CDVPluginResult *pluginResult = nil;
    NSString *connectionStatus = nil;
    if (isOpen) {
        connectionStatus = @"Connected";
        //The BRSP object is ready to be used
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        //Print the security level of the brsp service to console
        NSLog(@"BRSP Security Level is %d", _brspObject.securityLevel);
    } else {
        //brsp object has been closed
        connectionStatus = @"Disconnected";
    }
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:connectionStatus];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:connectionCallback];
}
- (void)brsp:(Brsp*)brsp SendingStatusChanged:(BOOL)isSending {
    //This is a good place to change BRSP mode
    //If we are on the last command in the queue and we are no longer sending, change the mode back to previous value
    if (isSending == NO)
    {
        if (_lastMode == _brspObject.brspMode)
            return;  //Nothing to do here
        //Change mode back to previous setting
        NSError *error = [_brspObject changeBrspMode:_lastMode];
        if (error)
            NSLog(@"%@", error);
    }
}

- (NSString*)hexString:(NSData*) inData {
    /* Returns hexadecimal string of NSData. Empty string if data is empty.   */
    
    const unsigned char *dataBuffer = (const unsigned char *)[inData bytes];
    
    if (!dataBuffer)
        return [NSString string];
    
    NSUInteger          dataLength  = [inData length];
    NSMutableString     *hexString  = [NSMutableString stringWithCapacity:(dataLength * 2)];
    
    for (int i = 0; i < dataLength; ++i)
        [hexString appendString:[NSString stringWithFormat:@"%02lx", (unsigned long)dataBuffer[i]]];
    
    return [NSString stringWithString:hexString];
}


- (void)brspDataReceived:(Brsp*)brsp {
    NSLog(@"Data received");
    NSData *unconvertedData = [brsp readBytes];
    const unsigned char *dataBuffer = (const unsigned char *)[unconvertedData bytes];
    
    NSUInteger          dataLength  = [unconvertedData length];
    NSMutableString     *hexString  = [NSMutableString stringWithCapacity:(dataLength * 2)];
    
    for (int i = 0; i < dataLength; ++i)
        [hexString appendString:[NSString stringWithFormat:@"%02lx", (unsigned long)dataBuffer[i]]];
    
    
    //If there are items in the _commandQueue array, assume this data is part of a command response
    //The data incomming is in response to a sent command.
    
    NSString *newData = [NSString stringWithString:hexString];
    CDVPluginResult *pluginResult = nil;

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:newData];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:dataCallback];
}
- (void)brsp:(Brsp*)brsp ErrorReceived:(NSError*)error {
    NSLog(@"%@", error.description);
}
- (void)brspModeChanged:(Brsp*)brsp BRSPMode:(BrspMode)mode {
    switch (mode) {
        case BrspModeData:
            
            break;
        case BrspModeRemoteCommand:
            
            break;
            
        default:
            
            break;
    }
}

#pragma mark - CBCentralManagerDelegate

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    if (![peripherals containsObject:peripheral]) {
        [peripherals addObject:peripheral];
        
        //public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
        //    String deviceInfo = device.getAddress() + ";" + device.getName();
        //    PluginResult result = new PluginResult(PluginResult.Status.OK, deviceInfo);
        //    result.setKeepCallback(true);
        //    deviceFoundCallback.sendPluginResult(result);
        //    Log.d(TAG, "Device found: " + deviceInfo);
        //};
        NSInteger actualIndex = [peripherals count] - 1;
        CDVPluginResult *pluginResult = nil;
        NSString *deviceData = [NSString stringWithFormat:@"%02d:11:22:33:44:55;%@",actualIndex,[peripheral name]];            // Connect with device name, as address is not useable and visible to connect in iOS
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:deviceData];
        [pluginResult setKeepCallbackAsBool:TRUE];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:scanCallback];
        NSLog(@"Device found: %@", [peripheral description]);    }
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    //call the open function to prepare the brsp service
    [self.brspObject open];
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    printf("Status of CoreBluetooth central manager changed %d \r\n",central.state);
    if (central.state==CBCentralManagerStatePoweredOn) {

    }
}

@end
