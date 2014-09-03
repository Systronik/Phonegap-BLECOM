var blecommunication = {
    checkAvailability: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "checkAvailability", [{
        	"data": data
        }]);
    },
    scanDevices: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "scanDevices", [{
        	"data": data
        }]);
    },
    stopScanDevices: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "stopScanDevices", [{
        	"data": data
        }]);
    },
    connectDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "connectDevice" , [{
        	"address": address
        }]);
    },
    disconnectDevice: function(address, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "disconnectDevice" , [{
        	"address": address
        }]);
    },
    sendData: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "sendData", [{
        	"data": data
        }]);
    },
    getData: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "getData", [{
        	"data": data
        }]);
    }
};
module.exports = blecommunication;

