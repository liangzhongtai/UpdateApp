##UpdateApp���ʹ��˵��
* �汾:2.3.3

##��������
* npm 4.4.1 +
* node 9.8.0 +


##ʹ������
####ע��:
######Macƽ̨,������µĿ���̨��������Ȩ�����⣬����������ǰ��sudo
######��׿ƽ̨��Ҫ���֧�ְ���com.android.support:support-v4:27.1.0�����°汾

######1.������Ŀ�ĸ�Ŀ¼�����������::com.chinamobile.update.updateapp
* Ϊ��Ŀ���UpdateApp�����ִ��:`cordova plugin add com.chinamobile.update.updateapp`
* ���Ҫɾ�����,ִ��:`cordova plugin add com.chinamobile.update.UpdateApp`
* Ϊ��Ŀ��Ӷ�Ӧ��platformƽ̨,����ӹ����˲����ԣ�ִ��:
* ��׿ƽ̨: `cordova platform add android`
* ios ƽ̨:`cordova platform add ios`
* �������ӵ���Ӧƽ̨��,ִ��: `cordova build`

######2.��js�ļ���,ͨ������js�������ò��������ִ��app���¹���
*
```javascript
    camera: function(){
        //��native����app��������
        //android��
        //����1��apk��������
        //����2��apk���غ�洢���ֻ��ļ�����
        //����3��apk���غ���ļ���
        //����4��apk����ʱ��״̬��֪ͨ����
        cordova.exec(success, error, "UpdateApp", "coolMethod", [��http://www.test.com/test.apk��, "rfworker", "RF_worker.apk", "RF_worker"]);
        //ios��
        //����1��apk����������
        //ʹ��appstore��ʽ����appʱ�����Ӹ�ʽΪ:items-apps://https://itunes.app.com/cn/podast
        //ʹ��plist��ipa�ļ���ʽ����appʱ,���Ӹ�ʽΪ:
        //https://nqi.gmcc.net:20443/dev-prjmng-app/app.plist
        cordova.exec(null, null, "UpdateApp", "coolMethod", ["https://nqi.gmcc.net:20443/dev-prjmng-app/app.plist"]);
    }
    
    //Ŀǰֻ��android�˻�ص�����״̬
    success: function(var result){
        //Ĭ��Ϊ0��Ŀǰֻ��0һ�����
        var updateType = result[0];
        //status=4:���سɹ�
        var status     = result[1];
        //��ʾ��Ϣ
        var message    = result[2];
    }

    error: function(var result){
        //������쳣��ʾ
        /Ĭ��Ϊ0��Ŀǰֻ��0һ�����
        var updateType = result[0];
        //status=1:���粻����
        //status=2:����ʧ��
        //status=3:�����ж�
        var status     = result[1];
        //��ʾ��Ϣ
        var message    = result[2];
    }
```
######˵��:
*.plist�ļ���ʽ:
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
                <string>��������Ŀ�����֤���û���</string>
                <key>bundle-version</key>
                <string>������app�汾</string>
                <key>kind</key>
                <string>software</string>
                <key>title</key>
                <string>������app��װ����</string>
            </dict>
        </dict>
    </array>
</dict>
</plist>
```
*ע������:
######android������ʱ�����״̬��û�г���֪ͨ��������ֻ�Ӧ�ù����ҵ���ǰӦ�ô�����֪ͨ��
######ios�˵�plist�ļ������ر�����httpsЭ���.
######ios���ipa�ļ�ʱ���벻Ҫѡ��appstore�ķ�ʽ���,ѡ��delevelop��ʽ������������ʾ��ǰapp�޷����ذ�װ.
######ios�˵�ipa�ļ���bundle-identifier��ú͵�ǰӦ�õ�bundle-identifierһ��.

##���ⷴ��
  ��ʹ�������κ����⣬������������ϵ��ʽ.
  
  * �ʼ�:18520660170@139.com
  * ʱ��:2018-6-7 15:00:00


