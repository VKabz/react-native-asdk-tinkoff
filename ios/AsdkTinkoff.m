#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(AsdkTinkoff, NSObject)

RCT_EXTERN_METHOD(Pay:
(NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(ApplePay:
(NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

//RCT_EXTERN_METHOD(Init:
//                  (NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)
//
//RCT_EXTERN_METHOD(Finish:
//                  (NSString*)params withResolver:(RCTPromiseResolveBlock)resolve withRejecter:(RCTPromiseRejectBlock)reject)

@end
