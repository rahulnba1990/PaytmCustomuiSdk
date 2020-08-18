
package com.paytm.customui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import net.one97.paytm.nativesdk.PaytmSDK;
import net.one97.paytm.nativesdk.common.Constants.SDKConstants;
import net.one97.paytm.nativesdk.common.widget.PaytmConsentCheckBox;
import net.one97.paytm.nativesdk.dataSource.PaytmPaymentsUtilRepository;
import net.one97.paytm.nativesdk.instruments.upicollect.models.UpiOptionsModel;
import net.one97.paytm.nativesdk.paymethods.datasource.PaymentMethodDataSource;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RNPaytmCustomuiSdkModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Promise mPromise;
    private PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
    private final int FETCH_UPI_BALANCE_REQUEST_CODE = 100;
    private final int SET_UPI_MPIN_REQUEST_CODE = 101;
    private final int TXN_START_CODE = 103;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (mPromise != null) {
                switch (requestCode) {
                    case FETCH_UPI_BALANCE_REQUEST_CODE:
                        mPromise.resolve(data.getStringExtra("response"));
                        break;
                    case SET_UPI_MPIN_REQUEST_CODE:
                        mPromise.resolve(data.getStringExtra("response"));
                        break;
                    case SDKConstants.REQUEST_CODE_UPI_APP:
                        String result = data.getStringExtra("Status");
                        if (result != null && result.equalsIgnoreCase("SUCCESS")) {
                            mPromise.resolve("SUCCESS");
                        } else {
                            mPromise.resolve(data.getStringExtra("Status"));
                        }
                        break;
                    case TXN_START_CODE:
                        mPromise.resolve(data.getStringExtra("response"));
                        break;
                    default:

                }
            }
        }
    };

    public RNPaytmCustomuiSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNPaytmCustomuiSdk";
    }

    @ReactMethod
    public void isPaytmAppInstalled(Promise promise) {
        boolean installed = paymentsUtilRepository.isPaytmAppInstalled(reactContext);
        promise.resolve(installed);
    }

    @ReactMethod
    public void fetchAuthCode(String clientId, Promise promise) {
        PaytmConsentCheckBox paytmConsentCheckBox = new PaytmConsentCheckBox(reactContext);
        paytmConsentCheckBox.setChecked(true);
        String code = paymentsUtilRepository.fetchAuthCode(reactContext, clientId);
        promise.resolve(code);
    }

    @ReactMethod
    public void getUPIAppsInstalled(Promise promise) {
        List<UpiOptionsModel> apps = PaytmSDK.getPaymentsHelper().getUpiAppsInstalled(reactContext);
        List<String> appNames = new ArrayList<>();
        for (UpiOptionsModel app : apps) {
            appNames.add(app.getAppName());
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String data = mapper.writeValueAsString(appNames);
            promise.resolve(data);
        } catch (IOException e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void startWalletTransaction(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                             String customEndpoint, String merchantCallbackUrl, String paymentFlow, Promise promise) {
        Bundle bundle = new Bundle();
        Intent intent = setupIntentMetadata(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        intent.putExtra("paymentAction", "WALLET");
        intent.putExtra("paymentFlow", paymentFlow);
        reactContext.startActivityForResult(intent, TXN_START_CODE, bundle);
        mPromise = promise;
    }

    @ReactMethod
    public void startUPIIntentTransaction(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                                       String customEndpoint, String merchantCallbackUrl, String paymentFlow, String appName, Promise promise) {
        Bundle bundle = new Bundle();
        Intent intent = setupIntentMetadata(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        intent.putExtra("paymentAction", "UPI_INTENT");
        intent.putExtra("paymentFlow", paymentFlow);
        intent.putExtra("appName", appName);
        reactContext.startActivityForResult(intent, TXN_START_CODE, bundle);
        mPromise = promise;
    }

    @ReactMethod
    public void fetchUPIBalance(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                                String customEndpoint, String merchantCallbackUrl, String upiId, String bankAccountJson, Promise promise) {
        Bundle bundle = new Bundle();
        Intent intent = setupIntentMetadata(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        intent.putExtra("paymentAction", "FETCH_UPI_BALANCE");
        intent.putExtra("upiId", upiId);
        intent.putExtra("bankAccountJson", bankAccountJson);
        reactContext.startActivityForResult(intent, FETCH_UPI_BALANCE_REQUEST_CODE, bundle);
        mPromise = promise;
    }

    @ReactMethod
    public void setUpiMpin(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                           String customEndpoint, String merchantCallbackUrl, String vpa, String bankAccountString, Promise promise) {
        Bundle bundle = new Bundle();
        Intent intent = setupIntentMetadata(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        intent.putExtra("paymentAction", "FETCH_UPI_BALANCE");
        intent.putExtra("vpa", vpa);
        intent.putExtra("bankAccountString", bankAccountString);
        reactContext.startActivityForResult(intent, SET_UPI_MPIN_REQUEST_CODE, bundle);
        mPromise = promise;
    }

    @ReactMethod
    public void userHasSavedInstruments(String mid, Promise promise) {
        PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
        boolean hasInstruments = paymentsUtilRepository.userHasSavedInstruments(reactContext, mid);
        promise.resolve(hasInstruments);
    }

    @ReactMethod
    public void getLastNBSavedBank(boolean isStaging, Promise promise) {
        PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
        String bank = paymentsUtilRepository.getLastNBSavedBank();
        promise.resolve(bank);
    }

    @ReactMethod
    public void getLastSavedVPA(boolean isStaging, Promise promise) {
        PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
        String bank = paymentsUtilRepository.getLastNBSavedBank();
        promise.resolve(bank);
    }

    PaymentMethodDataSource.Callback<JSONObject> netBankListCallBack = new PaymentMethodDataSource.Callback<JSONObject>() {
        @Override
        public void onResponse(@Nullable JSONObject response) {
            NBResponse nbResponse = new Gson().fromJson(String.valueOf(response), NBResponse.class);
            if (nbResponse != null) {
                List<PayChannelOptions> banks = nbResponse.getPayChannelOptions();
                mPromise.resolve(new Gson().toJson(banks));
            } else {
                mPromise.resolve(null);
            }
        }

        @Override
        public void onErrorResponse(@Nullable com.android.volley.VolleyError volleyError, @Nullable JSONObject response) {
            NBResponse nbResponse = new Gson().fromJson(String.valueOf(response), NBResponse.class);
            mPromise.reject(new Exception(volleyError));
        }
    };

    @ReactMethod
    public void getNetBankingList(Promise promise) {
        mPromise = promise;
        PaytmSDK.getPaymentsHelper().getNBList(netBankListCallBack);
    }

    private Intent setupIntentMetadata(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                                     String customEndpoint, String merchantCallbackUrl) {
        Intent intent = new Intent(reactContext, PaymentProcessActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("mid", mid);
        intent.putExtra("amount", amount);
        intent.putExtra("txnToken", txnToken);
        intent.putExtra("isAssistEnabled", isAssistEnabled);
        intent.putExtra("loggingEnabled", loggingEnabled);
        intent.putExtra("customEndpoint", customEndpoint);
        intent.putExtra("merchantCallbackUrl", merchantCallbackUrl);
        return intent;
    }
}