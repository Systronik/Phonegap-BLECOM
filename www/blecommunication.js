var blecommunication = {
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
    sendData: function(data, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "sendData", [{
        	"data": data
        }]);
    }
};
module.exports = blecommunication;

