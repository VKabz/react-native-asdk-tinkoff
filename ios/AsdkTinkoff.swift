    import TinkoffASDKCore
    import TinkoffASDKUI

    @objc(AsdkTinkoff)
class AsdkTinkoff: NSObject {
    
    var promiseResolve: RCTPromiseResolveBlock? = nil
    var promiseReject: RCTPromiseRejectBlock? = nil
    var sdk:AcquiringSdk? = nil
    var ui:AcquiringUISDK? = nil
    
    struct PaymentData: Decodable
    {
        let TerminalKey: String
        let Password: String
        let PublicKey: String
        let OrderId: String
        let CustomerKey: String
        let Amount: Int64
        let email: String
    }
    
    struct SuccessPayment: Encodable {
        let PaymentId: String?
        /// Идентификатор заказа в системе продавца
        let orderId: String?
        /// Сумма заказа в копейках
        let amount: Int64?
        let Success: Bool
        let Status: String
        let canceled: Bool
    }
    
    struct Config: Decodable {
        let TerminalKey: String
        let Password: String
        let PublicKey: String
    }
    
    
    @objc(Pay:withResolver:withRejecter:)
    func Pay(json:String, resolve:@escaping RCTPromiseResolveBlock, reject:@escaping RCTPromiseRejectBlock) -> Void {
        promiseResolve = resolve
        promiseReject = reject
        
        
        
        let cfg = try! JSONDecoder().decode(Config.self, from: json.data(using: .utf8)!)
        
        
        
        let coreSDKConfiguration = AcquiringSdkConfiguration(
            
            credential: AcquiringSdkCredential(
                terminalKey: cfg.TerminalKey,
                publicKey: cfg.PublicKey
                
            ),
            server: .prod
        )
        
        
        let uiSDKConfiguration = UISDKConfiguration()
        
        
        do {
            let sdk = try AcquiringUISDK(
                coreSDKConfiguration: coreSDKConfiguration,
                uiSDKConfiguration: uiSDKConfiguration
            )
            
            
            
            let data = try! JSONDecoder().decode(PaymentData.self, from: json.data(using: .utf8)!)
            
            
            let orderOptions = OrderOptions(
                /// Идентификатор заказа в системе продавца
                orderId: data.OrderId,
                // Полная сумма заказа в копейках
                amount: data.Amount
            )
            
            let customerOptions = CustomerOptions(
                
                // Идентификатор покупателя в системе продавца.
                // С помощью него можно привязать карту покупателя к терминалу после успешного платежа
                customerKey: data.CustomerKey,
                email: data.email
            )
            
            let paymentOptions = PaymentOptions(
                orderOptions: orderOptions,
                customerOptions: customerOptions
            )
            
            
            let paymentFlow: PaymentFlow = .full(paymentOptions: paymentOptions)
            
            sdk.presentMainForm(on: (UIApplication.shared.delegate?.window!?.rootViewController)!, paymentFlow: paymentFlow, configuration: MainFormUIConfiguration(orderDescription: "")) { res in
                switch res {
                    
                    
                case .succeeded(let success):
                    
                    let successPayment = SuccessPayment(PaymentId: success.paymentId, orderId: success.orderId, amount: success.amount, Success: true, Status: "CONFIRMED", canceled: false)
                    self.promiseResolve?(try! successPayment.encode2JSONObject())
                case .failed(let error):
                    if
                        let apiError = error as? APIError,
                        case .failure(let err) = apiError
                    {
                        let successPayment = SuccessPayment(PaymentId: err.paymentId, orderId: nil, amount: nil, Success: false, Status: "ERROR", canceled: false)
                        self.promiseResolve?(try! successPayment.encode2JSONObject())
                    }
                case .cancelled(_):
                    let successPayment = SuccessPayment(PaymentId: nil, orderId: nil, amount: nil, Success: false, Status: "ERROR", canceled: true)
                    self.promiseResolve?(try! successPayment.encode2JSONObject())
                }
            }
            
        } catch {
            // Ошибка может возникнуть при некорректном параметре `publicKey`
            self.promiseReject?(nil, nil, error)
        }
        
        
        
    }
}
