var blecommunication = {
    scanDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "scanDevices");
    },
    stopScanDevices: function(successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "stopScanDevices");
    },
    connectDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "connectDevice" , [{
        	"address": address
        }]);
    },
    sendData: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "sendData", [{
        	"data": data
        }]);
    }
};
module.exports = blecommunication;

