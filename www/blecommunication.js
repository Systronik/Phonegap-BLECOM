var blecommunication = {
    scanDevices: function(dataToSend, port, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BLECommunication", "scanDevices", [{
        	"dataToSend": dataToSend,
        	"port": port
        }]);
    }
};
module.exports = blecommunication;

