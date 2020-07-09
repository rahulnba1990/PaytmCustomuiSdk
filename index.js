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

    static initPaytmSDK(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
                        customEndpoint, merchantCallbackUrl) {
        return RNPaytmCustomuiSdk.initPaytmSDK(mid, orderId, txnToken, amount,
            isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
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

    static fetchUPIBalance(upiId, bankAccountJson) {
        return RNPaytmCustomuiSdk.fetchUPIBalance(upiId, bankAccountJson);
    }

    static setUpiMpin(vpa, bankAccountString) {
        return RNPaytmCustomuiSdk.setUpiMpin(vpa, bankAccountString);
    }

    static payViaUPI(paymentFlow, selectedAppName) {
        return RNPaytmCustomuiSdk.payViaUPI(paymentFlow, selectedAppName);
    }
}

export default PaytmCustomuiSdk;