#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(AsdkTinkoff, NSObject)

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}
+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

RCT_EXTERN_METHOD(Pay:
                  (NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(ApplePay:
                  (NSString*)params merchant:(NSString*)merchant withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(ApplePayAvailable:
                  (NSString*)params merchant(NSString*)merchant withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

//RCT_EXTERN_METHOD(Init:
//                  (NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)
//
//RCT_EXTERN_METHOD(Finish:
//                  (NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

@end
