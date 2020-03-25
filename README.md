# cordova-plugin-log2c-fabu-love

基于[`Fabu.love`](https://github.com/rock-app/fabu.love) App内测分发平台开发的一个自动检查更新的Cordova plugin.

本组件依赖更新组件[`App update`](https://github.com/log2c/cordova-plugin-log2c-app-update)

## 使用

安装Plugin
```
cordova plugin cordova-plugin-log2c-fabu-love \
--variable DOMAIN='' \
--variable TEAM_ID=''
```

`DOMAIN`: 检查更新的URL地址(最后不要带`/`,如`http://127.0.0.1`即可)
`TEAM_ID`: 爱发布里`checkupdate`接口里的`team_id`

## API

```javascript
/**
 * 检查更新
 * @param {*} config 配置 {
 *  checkOnly:  boolean 只返回更新信息,不弹出检查更新的 alert
 * }
 * @param {*} successCallback 回调 {
 *  hasNewVersion: boolean, //是否有更新
 *  changelog:  string,
 *  version:    string  // 1.0.0
 * }
 * @param {*} errorCallback 异常回调
 */
checkUpdate: function (config, successCallback, errorCallback){};
```
