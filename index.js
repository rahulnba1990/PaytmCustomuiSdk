import {NativeModules} from 'react-native';

const {RNPaytmCustomuiSdk} = NativeModules;

class PaytmCustomuiSdk {

    static isPaytmAppInstalled() {
        return RNPaytmCustomuiSdk.isPaytmAppInstalled();
    }

    static fetchAuthCode(clientId, isStaging) {
        return RNPaytmCustomuiSdk.fetchAuthCode(clientId, isStaging);
    }

    static getUPIAppsInstalled() {
        return RNPaytmCustomuiSdk.getUPIAppsInstalled();
    }

    static startWalletTransaction(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                        customEndpoint, merchantCallbackUrl, paymentFlow, isStaging) {
        return RNPaytmCustomuiSdk.startWalletTransaction(mid, orderId, txnToken, amount,
            isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl, paymentFlow, isStaging);
    }

    static startUPIIntentTransaction(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, paymentFlow, appName, isStaging) {
        return RNPaytmCustomuiSdk.startUPIIntentTransaction(mid, orderId, txnToken, amount,
            isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl, paymentFlow, appName, isStaging);
    }

    static fetchUPIBalance(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, upiId, bankAccountJson, isStaging) {
        return RNPaytmCustomuiSdk.fetchUPIBalance(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, upiId, bankAccountJson, isStaging);
    }

    static setUpiMpin(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, vpa, bankAccountString, isStaging) {
        return RNPaytmCustomuiSdk.setUpiMpin(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, vpa, bankAccountString, isStaging);
    }

    static userHasSavedInstruments(mid, isStaging) {
        return RNPaytmCustomuiSdk.userHasSavedInstruments(mid, isStaging);
    }

    static getLastNBSavedBank(isStaging) {
        return RNPaytmCustomuiSdk.getLastNBSavedBank(isStaging);
    }

    static getLastSavedVPA(isStaging) {
        return RNPaytmCustomuiSdk.getLastSavedVPA(isStaging);
    }

    static getNetBankingList(isStaging) {
        return RNPaytmCustomuiSdk.getNetBankingList(isStaging);
    }
}

export default PaytmCustomuiSdk;