
package com.paytm.customui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import net.one97.paytm.nativesdk.PaytmSDK;
import net.one97.paytm.nativesdk.app.PaytmSDKCallbackListener;
import net.one97.paytm.nativesdk.common.Constants.SDKConstants;
import net.one97.paytm.nativesdk.common.widget.PaytmConsentCheckBox;
import net.one97.paytm.nativesdk.dataSource.PaytmPaymentsUtilRepository;
import net.one97.paytm.nativesdk.dataSource.models.UpiDataRequestModel;
import net.one97.paytm.nativesdk.dataSource.models.UpiIntentRequestModel;
import net.one97.paytm.nativesdk.dataSource.models.WalletRequestModel;
import net.one97.paytm.nativesdk.instruments.upicollect.models.UpiOptionsModel;
import net.one97.paytm.nativesdk.paymethods.datasource.PaymentMethodDataSource;
import net.one97.paytm.nativesdk.transcation.model.TransactionInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

public class RNPaytmCustomuiSdkModule extends ReactContextBaseJavaModule implements PaytmSDKCallbackListener {

    //private final ReactApplicationContext reactContext;
    private Promise mPromise;
    private PaytmSDK paytmSDK = null;
    private static String paymentFlow;
    private final int FETCH_UPI_BALANCE_REQUEST_CODE = 100;
    private final int SET_UPI_MPIN_REQUEST_CODE = 101;
    private final int TXN_START_CODE = 103;
    private RNPaytmCustomuiSdkModule instance = this;

    private ActivityEventListener activityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if(mPromise != null) {
                try {
                    if (data != null) {
                        switch (requestCode) {
                            case FETCH_UPI_BALANCE_REQUEST_CODE:
                                mPromise.resolve(data.getStringExtra("response"));
                                paytmSDK.clear();
                                break;
                            case SET_UPI_MPIN_REQUEST_CODE:
                                mPromise.resolve(data.getStringExtra("response"));
                                paytmSDK.clear();
                                break;
                            case SDKConstants.REQUEST_CODE_UPI_APP:
                                String result = data.getStringExtra("Status");
                                if (!"FAILED".equalsIgnoreCase(result) && !"FAILURE".equalsIgnoreCase(result)) {
                                    Context context = instance.getCurrentActivity();
                                    if(context == null) {
                                        context = instance.getReactApplicationContext();
                                    }
                                    PaytmSDK.getPaymentsHelper().makeUPITransactionStatusRequest(context, RNPaytmCustomuiSdkModule.paymentFlow);
                                } else {
                                    mPromise.resolve("TXN_FAILED");
                                    paytmSDK.clear();
                                }
                                break;
                            case TXN_START_CODE:
                                mPromise.resolve(data.getStringExtra("response"));
                                paytmSDK.clear();
                                break;
                        }
                    } else {
                        mPromise.resolve("TXN_FAILED");
                        paytmSDK.clear();
                    }
                } catch (Exception e) {
                    mPromise.resolve("TXN_FAILED");
                    paytmSDK.clear();
                    e.printStackTrace();
                }
            }
        }
    };

    public RNPaytmCustomuiSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        //this.reactContext = reactContext;
        reactContext.addActivityEventListener(activityEventListener);
    }

    @Override
    public String getName() {
        return "RNPaytmCustomuiSdk";
    }

    @ReactMethod
    public void isPaytmAppInstalled(Promise promise) {
        PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
        boolean installed = paymentsUtilRepository.isPaytmAppInstalled(this.getReactApplicationContext());
        promise.resolve(installed);
    }

    @ReactMethod
    public void fetchAuthCode(String clientId, Promise promise) {
        PaytmConsentCheckBox paytmConsentCheckBox = new PaytmConsentCheckBox(this.getReactApplicationContext());
        paytmConsentCheckBox.setChecked(true);
        PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
        String code = paymentsUtilRepository.fetchAuthCode(this.getReactApplicationContext(), clientId);
        promise.resolve(code);
    }

    @ReactMethod
    public void getUPIAppsInstalled(Promise promise) {
        List<UpiOptionsModel> apps = PaytmSDK.getPaymentsHelper().getUpiAppsInstalled(this.getReactApplicationContext());
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
        initPaytmSdk(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        if (paymentFlow == null) {
            paymentFlow = "NONE";
        }
        RNPaytmCustomuiSdkModule.paymentFlow = paymentFlow;
        WalletRequestModel walletRequestModel = new WalletRequestModel(paymentFlow);
        paytmSDK.startTransaction(this.getCurrentActivity(), walletRequestModel);
        mPromise = promise;
    }

    @ReactMethod
    public void startUPIIntentTransaction(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                                          String customEndpoint, String merchantCallbackUrl, String paymentFlow, String appName, Promise promise) {
        try {
            initPaytmSdk(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
            if (paymentFlow == null) {
                paymentFlow = "NONE";
            }
            RNPaytmCustomuiSdkModule.paymentFlow = paymentFlow;
            Context activity = this.getCurrentActivity();
            if(activity == null) {
                activity = this.getReactApplicationContext();
            }
            List<UpiOptionsModel> apps = PaytmSDK.getPaymentsHelper().getUpiAppsInstalled(activity);
            boolean found = false;
            for (UpiOptionsModel app : apps) {
                if (app.getAppName().equalsIgnoreCase(appName)) {
                    UpiIntentRequestModel upiCollectRequestModel = new UpiIntentRequestModel(paymentFlow, appName, app.getResolveInfo().activityInfo);
                    paytmSDK.startTransaction(this.getCurrentActivity(), upiCollectRequestModel);
                    found = true;
                }
            }
            if (found) {
                mPromise = promise;
            } else {
                promise.resolve(appName + " app not found on device");
            }
        } catch(Exception e) {
            e.printStackTrace();
            promise.resolve("TXN_FAILURE");
            paytmSDK.clear();
        }
    }

    @ReactMethod
    public void fetchUPIBalance(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                                String customEndpoint, String merchantCallbackUrl, String upiId, String bankAccountJson, Promise promise) {
        initPaytmSdk(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        if (upiId != null && bankAccountJson != null) {
            UpiDataRequestModel upiDataRequestModel = new UpiDataRequestModel(upiId, bankAccountJson, FETCH_UPI_BALANCE_REQUEST_CODE);
            paytmSDK.fetchUpiBalance(this.getReactApplicationContext(), upiDataRequestModel);
            mPromise = promise;
        } else {
            promise.resolve("Required params missing.");
            paytmSDK.clear();
        }
    }

    @ReactMethod
    public void setUpiMpin(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                           String customEndpoint, String merchantCallbackUrl, String vpa, String bankAccountString, Promise promise) {
        initPaytmSdk(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled, customEndpoint, merchantCallbackUrl);
        if (vpa != null && bankAccountString != null) {
            UpiDataRequestModel upiDataRequestModel = new UpiDataRequestModel(vpa, bankAccountString, SET_UPI_MPIN_REQUEST_CODE);
            paytmSDK.fetchUpiBalance(this.getReactApplicationContext(), upiDataRequestModel);
            mPromise = promise;
        } else {
            promise.resolve("Required params missing.");
            paytmSDK.clear();
        }
    }

    @ReactMethod
    public void userHasSavedInstruments(String mid, Promise promise) {
        PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
        boolean hasInstruments = paymentsUtilRepository.userHasSavedInstruments(this.getReactApplicationContext(), mid);
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
            //NBResponse nbResponse = new Gson().fromJson(String.valueOf(response), NBResponse.class);
            mPromise.reject(new Exception(volleyError));
        }
    };

    @ReactMethod
    public void getNetBankingList(Promise promise) {
        mPromise = promise;
        PaytmSDK.getPaymentsHelper().getNBList(netBankListCallBack);
    }

    private void initPaytmSdk(String mid, String orderId, String txnToken, double amount, boolean isAssistEnabled, boolean loggingEnabled,
                              String customEndpoint, String merchantCallbackUrl) {
        PaytmSDK.Builder builder = new PaytmSDK.Builder(this.getCurrentActivity(), mid, orderId, txnToken, amount, this);
        builder.setAssistEnabled(isAssistEnabled);
        builder.setLoggingEnabled(loggingEnabled);
        if (customEndpoint != null) {
            builder.setCustomEndPoint("");
        }
        if (merchantCallbackUrl != null) {
            builder.setMerchantCallbackUrl(merchantCallbackUrl);
        }
        paytmSDK = builder.build();
    }

    @Override
    public void onTransactionResponse(TransactionInfo transactionInfo) {
        if (transactionInfo != null) {
            if (transactionInfo.getTxnInfo() != null) {
                String s = new Gson().toJson(transactionInfo.getTxnInfo());
                //Toast.makeText(this.getCurrentActivity(), "txninfo:"+s, Toast.LENGTH_LONG).show();
                mPromise.resolve(s);
            } else {
                mPromise.resolve("TXN_FAILED");
            }
        } else {
            mPromise.resolve("TXN_FAILED");
        }
        paytmSDK.clear();
    }

    @Override
    public void networkError() {
        mPromise.resolve("TXN_FAILED");
        paytmSDK.clear();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        mPromise.resolve("TXN_FAILED");
        paytmSDK.clear();
    }

    @Override
    public void onGenericError(int i, String s) {
        mPromise.resolve("TXN_FAILED");
        paytmSDK.clear();
    }
}