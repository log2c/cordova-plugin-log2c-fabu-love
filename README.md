# cordova-plugin-log2c-fabu-love

基于Fabu.love App内测分发平台开发的一个自动检查更新的Cordova plugin.

## 使用

安装Plugin
```
cordova plugin cordova-plugin-log2c-fabu-love \
--variable DOMAIN='' \
--variable TEAM_ID=''
```

`DOMAIN`: 检查更新的URL地址(最后不要带`/`)
`TEAM_ID`: 爱发布里`checkupdate`接口里的`team_id`
