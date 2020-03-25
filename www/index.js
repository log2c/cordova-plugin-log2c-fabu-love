var exec = require('cordova/exec');

module.exports = {
    callNative: function (name, args, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, 'FabuLove', name, args);
    },

    /**
     * 检查更新
     * @param {*} config 配置 {
     *  checkOnly:  boolean 只返回信息,不弹出检查更新的 alert
     * }
     * @param {*} successCallback 回调 {
     *  hasNewVersion: boolean,
     *  changelog:  string,
     *  version:    string
     * }
     * @param {*} errorCallback 回调
     */
    checkUpdate: function (config, successCallback, errorCallback) {
        this.callNative('checkUpdate', [{
            ...{
                checkOnly: false
            },
            ...config
        }], successCallback, errorCallback);
    }
}