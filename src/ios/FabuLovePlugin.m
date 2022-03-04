#import "FabuLovePlugin.h"

@implementation FabuLovePlugin

-(void) pluginInitialize{
    @try {
        [self checkUpdate:nil];
    }
    @catch (NSException *exception) {
        NSLog(exception.reason);
    }
}

- (void)checkUpdate:(CDVInvokedUrlCommand*)command{
    BOOL checkOnly = false;

    if (command !=nil) {
        NSDictionary *args = [command.arguments objectAtIndex:0];
        checkOnly = [[args objectForKey:@"checkOnly"] boolValue] == YES;
    }

    NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
    NSDictionary *config = [infoDictionary objectForKey:@"fabu_love_config"];

    NSString *domain = [config objectForKey:@"fabu_love_domain"];
    NSString *teamId = [config objectForKey:@"fabu_love_team_id"];


    NSString *bundleId = [[NSBundle mainBundle]bundleIdentifier];
    NSString *version = [[[NSBundle mainBundle]infoDictionary] objectForKey:@"CFBundleVersion"];;

    NSString *url = [domain stringByAppendingFormat: @"/api/app/checkupdate/%@/ios/%@/%@",teamId,bundleId,version];

    NSLog(@"Final request URL: %@", url);
    [self requestCheckUpdate:url :domain checkOnly:checkOnly handle:^(BOOL error, NSString *errorMsg, BOOL hasNewVersion, NSString *changelog, NSString *version) {
        if (!checkOnly || command ==nil) {
            return ;
        }
        if (error) {
            id result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMsg];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            return;
        }

        NSDictionary *resultDict = @{
            @"hasNewVersion":@(hasNewVersion),
            @"changelog":changelog,
            @"version":version
        };
        id result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:resultDict];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

-(void) requestCheckUpdate: (NSString *) urlStr :(NSString *) domain checkOnly :(BOOL) checkOnly handle:(void(^)(BOOL error, NSString *errorMsg, BOOL hasNewVersion, NSString* changelog, NSString* version)) complatedHandle{
    NSURLSession *session = [NSURLSession sharedSession];
    NSURL *url = [NSURL URLWithString:urlStr];
    NSURLSessionDataTask *task = [session dataTaskWithURL:url completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        if (error != nil) {
            NSLog(@"Check update error!");
            complatedHandle(true, error.localizedDescription, false, nil, nil);
            return ;
        }
        NSDictionary *dict = (NSDictionary *)[NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:nil];

        BOOL success = [[dict objectForKey:@"success"] boolValue] == YES;
        if (!success) {
            NSLog(@"No update info.");
            complatedHandle(false, nil, false, nil, nil);
            return;
        }

        NSDictionary *versionData = [[dict objectForKey:@"data"] objectForKey:@"version"];
        BOOL forceUpdate = [[versionData objectForKey:@"updateMode"] isEqualToString:@"force"];
        NSString *logs = [versionData objectForKey:@"changelog"];
        NSString *versionStr = [versionData objectForKey:@"versionStr"];
        NSString *shortUrl = [[[dict objectForKey:@"data"] objectForKey:@"app"] objectForKey:@"shortUrl"];
        NSString *iOSUrl =  [domain stringByAppendingFormat:@"/%@",shortUrl];

        NSLog(@"Params: version = %@, forceUpdate = %i, logs = %@, installUrl = %@",versionStr, forceUpdate,logs,iOSUrl);

        if (!checkOnly) {   // 需要弹窗
            dispatch_async(dispatch_get_main_queue(), ^{
                [self alertUpdateWith:forceUpdate Changelog:logs URL:iOSUrl];
            });
        }else {
            complatedHandle(false, nil, true, logs, versionStr);
        }
    }];
    [task resume];
}

-(void) alertUpdateWith: (BOOL) isForceUpdate Changelog:(NSString *)changelog URL:(NSString *) installURL {
    NSURL *iOSUrl = [NSURL URLWithString:installURL];

    NSString *newAppVersionText = NSLocalizedStringWithDefaultValue(@"app_update_new_version_text", @"Localizable", NSBundle.mainBundle, @"NEW VERSION", nil);
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:newAppVersionText message:changelog preferredStyle:UIAlertControllerStyleAlert];

    if (changelog == nil) {
        changelog = @"";
    }

    changelog = [@"\n" stringByAppendingString:changelog];

    NSMutableParagraphStyle *paragraphStyle = [[NSMutableParagraphStyle alloc] init];
    [paragraphStyle setAlignment:NSTextAlignmentLeft];


    NSMutableAttributedString *attributedString = [[NSMutableAttributedString alloc] initWithString:changelog];
    [attributedString addAttribute:NSParagraphStyleAttributeName value:paragraphStyle range:NSMakeRange(0, [changelog length])];

    [alertController setValue:attributedString forKey:@"attributedMessage"];
    NSString *actionUpdateText = NSLocalizedStringWithDefaultValue(@"app_update_update_text", @"Localizable", NSBundle.mainBundle, @"UPDATE", nil);

    UIAlertAction *okAction = [UIAlertAction actionWithTitle:actionUpdateText style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [[UIApplication sharedApplication] openURL:iOSUrl options:@{} completionHandler:nil];
    }];

    [alertController addAction:okAction];

    if (!isForceUpdate) {
        NSString *actionIgnoreText = NSLocalizedStringWithDefaultValue(@"app_update_cancel_text", @"Localizable", NSBundle.mainBundle, @"IGNORE", nil);
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:actionIgnoreText style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        }];
        [alertController addAction:cancelAction];
    }
    [self.viewController presentViewController:alertController animated:YES completion:^{}];
}

@end
