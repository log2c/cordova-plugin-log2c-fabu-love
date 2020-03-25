var exec = require('cordova/exec');

module.exports = {
    callNative: function (name, args, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'FabuLove', name, args);
    }
}