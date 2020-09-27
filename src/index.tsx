import { NativeModules, Platform } from 'react-native'

export type Config = {
    TerminalKey: string
    Password: string
    PublicKey: string
}

export type Method = 'Init' | 'Finish'

export interface Init {
    request: {
        OrderId: number
        Amount: number
        CustomerKey: string
        Recurrent?: 'Y'
        Receipt?: Receipt
    }
    response: {
        Amount: number
        OrderId: number
        PaymentId: number
        Status: 'NEW' | 'REJECTED' | 'AUTHORIZED' | 'CONFIRMED'
        Success: boolean
        ErrorCode: number
        Message?: string
        Details?: string
        TerminalKey?: string
    }
}
export interface Finish {
    request: {
        PaymentId: number
        PaymentSource: {
            PAN?: string
            ExpDate?: string
            CVV?: string
            CardId?: number
            PaymentData?: string
        }
        Receipt?: Receipt
    }
    response: {
        paymentStatus: 'REJECTED' | 'CONFIRMED' | 'AUTHORIZED' | '3DS_CHECKING'
        success: boolean
        errorCode: number
        errorMessage?: string
        errorDetails?: string
    }
}

export type ReceiptTaxation =
    'osn' | // общая
    'usn_income' | // упрощенная (доходы)
    'usn_income_outcome' | // упрощенная (доходы минус расходы)
    'patent' | // патентная
    'envd' | // единый налог на вмененный доход
    'esn' // единый сельскохозяйственный налог

export type ReceiptPaymentMethod =
    'full_payment' | // полный расчет
    'full_prepayment' | // предоплата 100%
    'prepayment' | // предоплата
    'advance' | // аванс
    'partial_payment' | // частичный расчет и кредит
    'credit' | // передача в кредит
    'credit_payment' // оплата кредита

export type ReceiptPaymentObject =
    'commodity' | //товар
    'excise' | //подакцизный товар
    'job' | //работа
    'service' | //услуга
    'gambling_bet' | //ставка азартной игры
    'gambling_prize' | //выигрыш азартной игры
    'lottery' | //лотерейный билет
    'lottery_prize' | //выигрыш лотереи
    'intellectual_activity' | //предоставление результатов интеллектуальной деятельности
    'payment' | //платеж
    'agent_commission' | //агентское вознаграждение
    'composite' | //составной предмет расчета
    'another' //иной предмет расчета

export type ReceiptTax =
    'none' | //без НДС
    'vat0' | //0%
    'vat10' | //10%
    'vat20' | //20%
    'vat110' | //10/110
    'vat120' //20/120

export type ReceiptItem = {
    /**
     * Наименование товара
     */
    Name: string
    /**
     * Количество или вес товара
     */
    Quantity: number
    /**
     * Стоимость товара в копейках
     * Произведение Quantity и Price
     */
    Amount: number
    /**
     * Цена за единицу товара в копейках
     */
    Price: number
    /**
     * Признак способа расчета
     * Если не передан, в кассу отправляется значение full_payment
     */
    PaymentMethod?: ReceiptPaymentMethod
    /**
     * Признак предмета расчета
     * Если не передан, в кассу отправляется значение commodity
     */
    PaymentObject?: ReceiptPaymentObject
    /**
     * Ставка НДС
     */
    Tax: ReceiptTax
    /**
     * Маркировка товара
     */
    Ean13?: string
    /**
     * Код магазина
     */
    ShopCode?: string
    /**
     * Данные агента
     * Используется при работе по агентской схеме
     */
    AgentData?: any
    /**
     * Данные поставщика платежного агента
     * Обезателен, если передается значение AgentSign в объекте AgentData
     */
    SupplierInfo?: any
}
export type Receipt = {
    Phone: string
    Email?: string
    EmailCompany?: string
    Taxation: ReceiptTaxation
    Items: ReceiptItem[]
}

export default class TinkoffASDKCore {
    private config?: Config

    constructor(config: Config) {
        this.config = config
    }

    Pay(params: Init['request']): Promise<Init['response']> {
        return NativeModules.AsdkTinkoff.Pay(JSON.stringify({
            ...this.config,
            ...params
        }))
    }

    ApplePayAvailable(merchant: String): Promise<boolean> {
        if (Platform.OS !== 'ios') {
            return Promise.resolve(false)
        }
        return NativeModules.AsdkTinkoff.ApplePayAvailable(merchant)
    }

    ApplePay(params: Init['request'], merchant: String): Promise<Init['response']> {
        if (Platform.OS !== 'ios') {
            throw new Error(`Cannot use ApplePay on ${Platform.OS}`)
        }
        return NativeModules.AsdkTinkoff.ApplePay(JSON.stringify({
            ...this.config,
            ...params,
        }), merchant)
    }
    
    GooglePay(params: Init['request']): Promise<Init['response']> {
        if (Platform.OS !== 'android') {
            throw new Error(`Cannot use GooglePay on ${Platform.OS}`)
        }
        return NativeModules.AsdkTinkoff.GooglePay(JSON.stringify({
            ...this.config,
            ...params
        }))
    }

    // private check(arrayOfParams: string[], params: object) {
    //     for (const p of arrayOfParams) {
    //         if ((params as any)[p] === void 0) {
    //             throw new Error(`Parameter ${p} cannot be empty`)
    //         }
    //     }
    //     return true
    // }
    // private request<T extends { request: any, response: any }>(params: T['request'], method: Method, arrayOfParams: string[]): Promise<T['response']> {
    //     this.check(arrayOfParams, params)
    //     return NativeModules.AsdkTinkoff[method](JSON.stringify({
    //         ...this.config,
    //         ...params
    //     }))
    // }
    // Init(params: Init['request']): Promise<Init['response']> {
    //     return this.request<Init>(params, 'Init', ['CustomerKey', 'OrderId', 'Amount'])
    // }
    // Finish(params: Finish['request']): Promise<Finish['response']> {
    //     return this.request<Finish>(params, 'Finish', ['PaymentId', 'PaymentSource'])
    // }
}
