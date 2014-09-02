var blecommunication = {
    scanDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "scanDevices");
    },
    stopScanDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "stopScanDevices");
    },
    connectDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "scanDevices" , [{
        	"address": address
        }]);
    },
    sendData: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "scanDevices", [{
        	"data": data
        }]);
    }
};
module.exports = blecommunication;

