//
//  UpdateApp.m
//  HelloCordova
//
//  Created by 梁仲太 on 2018/6/6.
//

#import "UpdateApp.h"

@interface UpdateApp()

@property(nonatomic,copy)NSString *callbackId;
@property(nonatomic,assign)NSInteger updateType;
@property(nonatomic,strong)NSString *url;
@property(nonatomic,strong)NSString *fileDir;

@end

@implementation UpdateApp

-(void)coolMethod:(CDVInvokedUrlCommand *)command{
    self.callbackId = command.callbackId;
    //AppStore形式：@"itms-apps://itunes.apple.com/cn/app/jie-zou-da-shi/id493901993?mt=8";
    //IPA形式：https://plist文件的地址;
    self.url = command.arguments[0];
    
    //跳转至app store 升级
    if([self.url hasPrefix:@"itms-apps:"]){
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[self.url  stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]]];
    //下载IPA文件安装
    }else if([self.url hasPrefix:@"https:"]){
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:[[NSString stringWithFormat:@"itms-services://?action=download-manifest&url=%@",self.url] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]]];
    }
}

@end
