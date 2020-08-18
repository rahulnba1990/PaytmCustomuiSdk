import {NativeModules} from 'react-native';

const {RNPaytmCustomuiSdk} = NativeModules;

class PaytmCustomuiSdk {

    static isPaytmAppInstalled() {
        return RNPaytmCustomuiSdk.isPaytmAppInstalled();
    }

    static fetchAuthCode(clientId) {
        return RNPaytmCustomuiSdk.fetchAuthCode(clientId);
    }

    static getUPIAppsInstalled() {
        return RNPaytmCustomuiSdk.getUPIAppsInstalled();
    }

    static startWalletTransaction(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                        customEndpoint, merchantCallbackUrl, paymentFlow) {
        return RNPaytmCustomuiSdk.startWalletTransaction(mid, orderId, txnToken, amount,
            isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl, paymentFlow);
    }

    static startUPIIntentTransaction(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, paymentFlow, appName) {
        return RNPaytmCustomuiSdk.startUPIIntentTransaction(mid, orderId, txnToken, amount,
            isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl, paymentFlow, appName);
    }

    static fetchUPIBalance(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, upiId, bankAccountJson) {
        return RNPaytmCustomuiSdk.fetchUPIBalance(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, upiId, bankAccountJson);
    }

    static setUpiMpin(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, vpa, bankAccountString) {
        return RNPaytmCustomuiSdk.setUpiMpin(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                                  customEndpoint, merchantCallbackUrl, vpa, bankAccountString);
    }

    static userHasSavedInstruments(mid) {
        return RNPaytmCustomuiSdk.userHasSavedInstruments(mid);
    }

    static getLastNBSavedBank() {
        return RNPaytmCustomuiSdk.getLastNBSavedBank();
    }

    static getLastSavedVPA() {
        return RNPaytmCustomuiSdk.getLastSavedVPA();
    }

    static getNetBankingList() {
        return RNPaytmCustomuiSdk.getNetBankingList();
    }
}

export default PaytmCustomuiSdk;