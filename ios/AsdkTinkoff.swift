    import TinkoffASDKCore
    import TinkoffASDKUI

    @objc(AsdkTinkoff)
    class AsdkTinkoff: NSObject {
        
        var promiseResolve: RCTPromiseResolveBlock? = nil
        var promiseReject: RCTPromiseRejectBlock? = nil
        var sdk:AcquiringSdk? = nil
        var ui:AcquiringUISDK? = nil

        func Configure(json:String, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) {
            
            promiseResolve = resolve
            promiseReject = reject

            struct Config: Decodable {
                let TerminalKey: String
                let Password: String
                let PublicKey: String
            }
            let cfg = try! JSONDecoder().decode(Config.self, from: json.data(using: .utf8)!)
            let configuration = AcquiringSdkConfiguration(
                credential: AcquiringSdkCredential(
                    terminalKey: cfg.TerminalKey,
                    password: cfg.Password,
                    publicKey: cfg.PublicKey
                ),
                server: .prod
            )
            
            sdk = try? AcquiringSdk.init(configuration: configuration)
            ui = try? AcquiringUISDK.init(configuration: configuration)
        }
        
        // MARK - Pay
        
        @objc(Pay:withResolver:withRejecter:)
        func Pay(json:String, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
            Configure(json:json, resolve:resolve, reject:reject)

            let data = try! JSONDecoder().decode(PaymentInitData.self, from: json.data(using: .utf8)!)
                    
            let viewConfigration = AcquiringViewConfiguration.init()

            viewConfigration.localizableInfo = AcquiringViewConfiguration.LocalizableInfo(lang: "ru")
            
            self.ui?.presentPaymentView(on: (UIApplication.shared.delegate?.window!?.rootViewController)!, paymentData: data, configuration: viewConfigration) { (res) in
                switch res {
                    case .success(let success):
                        self.promiseResolve?(try! success.encode2JSONObject())
                    case .failure(let error):
                        self.promiseReject?(nil, nil, error)
                }
            }
            
        }
        
        // MARK - ApplePay
        
        @objc(ApplePay:merchant:withResolver:withRejecter:)
        func ApplePay(json:String, merchant:String, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
            Configure(json:json, resolve:resolve, reject:reject)
            
            let data = try! JSONDecoder().decode(PaymentInitData.self, from: json.data(using: .utf8)!)
            
            var paymentConfiguration = AcquiringUISDK.ApplePayConfiguration.init()
            
            paymentConfiguration.merchantIdentifier = merchant
            
            self.ui?.presentPaymentApplePay(
                on: (UIApplication.shared.delegate?.window!?.rootViewController)!,
                paymentData: data,
                viewConfiguration: .init(),
                paymentConfiguration: paymentConfiguration) { (res) in
                    switch res {
                        case .success(let success):
                            self.promiseResolve?(try! success.encode2JSONObject())
                        case .failure(let error):
                            self.promiseReject?(nil, nil, error)
                    }
                }
        }

    //    // MARK - Init
    //
    //    @objc(Init:withResolver:withRejecter:)
    //    func Init(json:String, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
    //
    //        Configure(json:json, resolve:resolve, reject:reject)
    //
    //        let paymentData = try! JSONDecoder().decode(PaymentInitData.self, from: json.data(using: .utf8)!)
    //
    //        _ = sdk?.paymentInit(data: paymentData) { (res) in
    //            switch res {
    //                case .success(let success):
    //                    self.promiseResolve?(try! success.encode2JSONObject())
    //                case .failure(let error):
    //                    self.promiseReject?(nil, nil, error)
    //            }
    //        }
    //    }
    //
    //    // MARK - FinishAuthorize
    //
    //    @objc(Finish:withResolver:withRejecter:)
    //    func Finish(json:String, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
    //
    //        Configure(json:json, resolve:resolve, reject:reject)
    //
    //        let data = try! JSONDecoder().decode(PaymentFinishRequestData.self, from: json.data(using: .utf8)!)
    //
    //        _ = sdk?.paymentFinish(data: data) { (res) in
    //            switch res {
    //                case .success(let success):
    //                    self.promiseResolve?(try! success.encode2JSONObject())
    //                case .failure(let error):
    //                    self.promiseReject?(nil, nil, error)
    //            }
    //        }
    //    }
    }
