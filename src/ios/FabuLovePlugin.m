#import "FabuLovePlugin.h"

@implementation FabuLovePlugin

-(void) pluginInitialize{
    @try {
        [self checkUpdate];
    }
    @catch (NSException *exception) {
        NSLog(@"@NSException");
    }
}

-(void) checkUpdate{
    NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
    NSDictionary *config = [infoDictionary objectForKey:@"fabu_love_config"];

    NSString *domain = [config objectForKey:@"fabu_love_domain"];
    NSString *teamId = [config objectForKey:@"fabu_love_team_id"];
    NSLog(@"Domain: %@, Team ID: %@",domain,teamId);

    NSString *bundleId = [[NSBundle mainBundle]bundleIdentifier];
    NSString *version = [[[NSBundle mainBundle]infoDictionary] objectForKey:@"CFBundleVersion"];;

    NSString *url = [domain stringByAppendingFormat: @"/api/app/checkupdate/%@/ios/%@/%@",teamId,bundleId,version];

    NSLog(@"Final request URL: %@", url);
    [self requestCheckUpdate:url :domain];

}

-(void) requestCheckUpdate: (NSString *) urlStr :(NSString *) domain{
    NSURLSession *session = [NSURLSession sharedSession];
    NSURL *url = [NSURL URLWithString:urlStr];
    NSURLSessionDataTask *task = [session dataTaskWithURL:url completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        NSLog(@"%@",[[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding]);

        if (error != nil) {
            NSLog(@"Check update error!");
            return ;
        }
        NSDictionary *dict = (NSDictionary *)[NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:nil];

        BOOL success = [[dict objectForKey:@"success"] boolValue] == YES;
        if (!success) {
            NSLog(@"No update info.");
            return;
        }

        NSDictionary *versionData = [[dict objectForKey:@"data"] objectForKey:@"version"];
        BOOL forceUpdate = [[versionData objectForKey:@"updateMode"] isEqualToString:@"force"];
        NSString *logs = [versionData objectForKey:@"changelog"];
        NSString *shortUrl = [[[dict objectForKey:@"data"] objectForKey:@"app"] objectForKey:@"shortUrl"];
        NSString *iOSUrl =  [domain stringByAppendingFormat:@"/%@",shortUrl];

        NSLog(@"Params: forceUpdate = %i, logs = %@, installUrl = %@",forceUpdate,logs,iOSUrl);
        dispatch_async(dispatch_get_main_queue(), ^{
            [self alertUpdateWith:forceUpdate Changelog:logs URL:iOSUrl];
        });
    }];
    [task resume];
}

-(void) alertUpdateWith: (BOOL) isForceUpdate Changelog:(NSString *)changelog URL:(NSString *) installURL {
    NSURL *iOSUrl = [NSURL URLWithString:installURL];

    NSString *newAppVersionText = NSLocalizedStringWithDefaultValue(@"app_update_new_version_text", @"Localizable", NSBundle.mainBundle, @"NEW VERSION", nil);
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:newAppVersionText message:changelog preferredStyle:UIAlertControllerStyleAlert];

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
