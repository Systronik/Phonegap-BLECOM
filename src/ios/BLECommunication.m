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
    
    NSLog(@"checkAvailability");
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT messageAsString:@"BLE enabled"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)scanDevices:(CDVInvokedUrlCommand*)command {
    
    NSLog(@"scanDevices");
    
    
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:TRUE];
    scanCallback = [command.callbackId copy];
    
    [peripherals removeAllObjects];
    
    
    [self.cBCM scanForPeripheralsWithServices:[NSArray arrayWithObject:[Brsp brspServiceUUID]] options:nil];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)stopScanDevices:(CDVInvokedUrlCommand*)command {
    
    NSLog(@"stopScanDevices");
    
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.cBCM stopScan];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)connectDevice:(CDVInvokedUrlCommand*)command {
    
    NSLog(@"connectDevice");
    CDVPluginResult *pluginResult = nil;
    
    NSString *arg1 = [command.arguments objectAtIndex:0];
    
    //init the object with default buffer sizes of 1024 bytes
    //    self.brspObject = [[Brsp alloc] initWithPeripheral:[AppDelegate app].activePeripheral];
    //init with custom buffer sizes
    activePeripheral = nil;
    for (NSUInteger i = 0; i < [peripherals count]; i++){
        if ([[arg1 valueForKey:@"address"] containsString:[[peripherals objectAtIndex:i] name]]){
            activePeripheral = [peripherals objectAtIndex:i];
            i = [peripherals count];
        }
    }
    
    if (activePeripheral){
        self.brspObject = [[Brsp alloc] initWithPeripheral:activePeripheral InputBufferSize:512 OutputBufferSize:512];
        //It is important to set this delegate before calling [Brsp open]
        self.brspObject.delegate = self;
        //Use CBCentral Manager to connect this peripheral
        [self.cBCM connectPeripheral:activePeripheral options:nil];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    
}

- (void)disconnectDevice:(CDVInvokedUrlCommand*)command {
    
    NSLog(@"disconnectDevices");
    CDVPluginResult *pluginResult = nil;
    if (activePeripheral){
        [self.brspObject close];
        //Use CBCentralManager to close the connection to this peripheral
        [self.cBCM cancelPeripheralConnection:activePeripheral];
        activePeripheral = nil;
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    }
    else{
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)sendData:(CDVInvokedUrlCommand*)command {
    
    NSLog(@"sendData");
    
    dataCallback = [command.callbackId copy];
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

#pragma mark BrspDelegate
- (void)brsp:(Brsp*)brsp OpenStatusChanged:(BOOL)isOpen {
    NSLog(@"OpenStatusChanged == %d", isOpen);
    if (isOpen) {
        //The BRSP object is ready to be used
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
        //Print the security level of the brsp service to console
        NSLog(@"BRSP Security Level is %d", _brspObject.securityLevel);
    } else {
        //brsp object has been closed
    }
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
- (void)brspDataReceived:(Brsp*)brsp {
    NSLog(@"Data recieved");
    //If there are items in the _commandQueue array, assume this data is part of a command response
    //The data incomming is in response to a sent command.
    NSString *newData = [brsp readString];
    CDVPluginResult *pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:newData];
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
        CDVPluginResult *pluginResult = nil;
        NSString *deviceData = [NSString stringWithFormat:@"%@;%@",[peripheral name],[peripheral name]];            // Connect with device name, as address is not useable and visible to connect in iOS
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
