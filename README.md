##UpdateApp插件使用说明
* 版本:2.3.3

##环境配置
* npm 4.4.1 +
* node 9.8.0 +


##使用流程
####注意:
######Mac平台,如果以下的控制台命令遇到权限问题，可以在命令前加sudo
######安卓平台需要添加支持包：com.android.support:support-v4:27.1.0及以下版本

######1.进入项目的根目录，添加相机插件::com.chinamobile.update.updateapp
* 为项目添加UpdateApp插件，执行:`cordova plugin add com.chinamobile.update.updateapp`
* 如果要删除插件,执行:`cordova plugin add com.chinamobile.update.UpdateApp`
* 为项目添加对应的platform平台,已添加过，此步忽略，执行:
* 安卓平台: `cordova platform add android`
* ios 平台:`cordova platform add ios`
* 将插件添加到对应平台后,执行: `cordova build`

######2.在js文件中,通过以下js方法调用插件，可以执行app更新功能
*
```javascript
    camera: function(){
        //向native发出app更新请求
        //android端
        //参数1：apk下载链接
        //参数2：apk下载后存储的手机文件夹名
        //参数3：apk下载后的文件名
        //参数4：apk下载时的状态栏通知标题
        cordova.exec(success, error, "UpdateApp", "coolMethod", [“http://www.test.com/test.apk”, "rfworker", "RF_worker.apk", "RF_worker"]);
        //ios端
        //参数1：apk的下载链接
        //使用appstore方式更新app时，链接格式为:items-apps://https://itunes.app.com/cn/podast
        //使用plist和ipa文件方式更新app时,链接格式为:
        //https://nqi.gmcc.net:20443/dev-prjmng-app/app.plist
        cordova.exec(null, null, "UpdateApp", "coolMethod", ["https://nqi.gmcc.net:20443/dev-prjmng-app/app.plist"]);
    }
    
    //目前只有android端会回调更新状态
    success: function(var result){
        //默认为0，目前只有0一种情况
        var updateType = result[0];
        //status=4:下载成功
        var status     = result[1];
        //提示信息
        var message    = result[2];
    }

    error: function(var result){
        //照相的异常提示
        /默认为0，目前只有0一种情况
        var updateType = result[0];
        //status=1:网络不可用
        //status=2:下载失败
        //status=3:下载中断
        var status     = result[1];
        //提示信息
        var message    = result[2];
    }
```
######说明:
*.plist文件格式:
```javascript
  <?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>items</key>
    <array>
        <dict>
            <key>assets</key>
            <array>
                <dict>
                    <key>kind</key>
                    <string>software-package</string>
                    <key>url</key>
                    <string>http://cloud.189.cn/download/client/iOS/test.ipa</string>
                </dict>
            </array>
            <key>metadata</key>
            <dict>
                <key>bundle-identifier</key>
                <string>请填上你的开发者证书用户名</string>
                <key>bundle-version</key>
                <string>请填上app版本</string>
                <key>kind</key>
                <string>software</string>
                <key>title</key>
                <string>请填上app安装包名</string>
            </dict>
        </dict>
    </array>
</dict>
</plist>
```
*注意问题:
######android端下载时，如果状态栏没有出现通知，请进入手机应用管理，找到当前应用打开允许通知。
######ios端的plist文件的下载必需是https协议的.
######ios打包ipa文件时，请不要选择appstore的方式打包,选择delevelop方式打包，否则会提示当前app无法下载安装.
######ios端的ipa文件的bundle-identifier最好和当前应用的bundle-identifier一致.

##问题反馈
  在使用中有任何问题，可以用以下联系方式.
  
  * 邮件:18520660170@139.com
  * 时间:2018-6-7 15:00:00


