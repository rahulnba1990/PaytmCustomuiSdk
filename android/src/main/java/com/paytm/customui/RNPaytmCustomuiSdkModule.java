
package com.paytm.customui;

import android.app.Activity;
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
import net.one97.paytm.nativesdk.dataSource.PaytmPaymentsUtilRepository;
import net.one97.paytm.nativesdk.dataSource.models.UpiDataRequestModel;
import net.one97.paytm.nativesdk.dataSource.models.UpiIntentRequestModel;
import net.one97.paytm.nativesdk.instruments.upicollect.models.UpiOptionsModel;
import net.one97.paytm.nativesdk.paymethods.datasource.PaymentMethodDataSource;
import net.one97.paytm.nativesdk.transcation.model.TransactionInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class RNPaytmCustomuiSdkModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private Promise mPromise;

    private PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();

    private PaytmSDK paytmSDK = null;

    private TransactionListener transactionListener = new TransactionListener();

    private final int FETCH_UPI_BALANCE_REQUEST_CODE = 100;
    private final int SET_UPI_MPIN_REQUEST_CODE = 101;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (mPromise != null) {
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
                        if (result != null && result.equalsIgnoreCase("SUCCESS")) {
                            //PaytmSDK.getPaymentsHelper().makeUPITransactionStatusRequest();
                            mPromise.resolve("SUCCESS");
                            paytmSDK.clear();
                        } else {
                            mPromise.resolve(data.getStringExtra("Status"));
                        }
                        paytmSDK.clear();
                        break;
                    default:
                        paytmSDK.clear();

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
            //e.printStackTrace();
            promise.reject(e);
        }
    }

    @ReactMethod
    public void initPaytmSDK(String mid, String orderId, String txnToken, double amount, boolean isNativePlusEnabled,
                             boolean isAssistEnabled, boolean loggingEnabled, String customEndpoint, String merchantCallbackUrl, Promise promise) {
        PaytmSDK.Builder builder = new PaytmSDK.Builder(reactContext, mid, orderId, txnToken, amount, transactionListener);
        builder.setAssistEnabled(isAssistEnabled);
        builder.setLoggingEnabled(loggingEnabled);
        if (customEndpoint != null) {
            builder.setCustomEndPoint("");
        }
        if (merchantCallbackUrl != null) {
            builder.setMerchantCallbackUrl(merchantCallbackUrl);
        }
        paytmSDK = builder.build();
        promise.resolve("SUCCESS");
    }

    @ReactMethod
    public void userHasSavedInstruments(String mid, Promise promise) {
        if (paytmSDK != null) {
            PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
            boolean hasInstruments = paymentsUtilRepository.userHasSavedInstruments(reactContext, mid);
            promise.resolve(hasInstruments);
            paytmSDK.clear();
        } else {
            promise.reject(new Exception("Initialize paytm SDK first by calling initPaytmSDK method"));
        }

    }

    @ReactMethod
    public void getLastNBSavedBank(Promise promise) {
        if (paytmSDK != null) {
            PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
            String bank = paymentsUtilRepository.getLastNBSavedBank();
            promise.resolve(bank);
            paytmSDK.clear();
        } else {
            promise.reject(new Exception("Initialize paytm SDK first by calling initPaytmSDK method"));
        }
    }

    @ReactMethod
    public void getLastSavedVPA(Promise promise) {
        if (paytmSDK != null) {
            PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();
            String bank = paymentsUtilRepository.getLastNBSavedBank();
            promise.resolve(bank);
            paytmSDK.clear();
        } else {
            promise.reject(new Exception("Initialize paytm SDK first by calling initPaytmSDK method"));
        }
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
        if (paytmSDK != null) {
            mPromise = promise;
            PaytmSDK.getPaymentsHelper().getNBList(netBankListCallBack);
        } else {
            promise.reject(new Exception("Initialize paytm SDK first by calling initPaytmSDK method"));
        }
    }

    ///////////////////////////////// UPI payment methods //////////////////////////////////////

    @ReactMethod
    public void fetchUPIBalance(String upiId, String bankAccountJson, Promise promise) {
        if (paytmSDK != null) {
            UpiDataRequestModel upiDataRequestModel = new UpiDataRequestModel(upiId, bankAccountJson, FETCH_UPI_BALANCE_REQUEST_CODE);
            PaymentActivity activity = new PaymentActivity();
            mPromise = promise;
            paytmSDK.fetchUpiBalance(activity, upiDataRequestModel);
        } else {
            promise.reject(new Exception("Initialize paytm SDK first by calling initPaytmSDK method"));
        }
    }

    @ReactMethod
    public void setUpiMpin(String vpa, String bankAccountString, Promise promise) {
        if (paytmSDK != null) {
            UpiDataRequestModel upiDataRequestModel = new UpiDataRequestModel(vpa,
                    bankAccountString, SET_UPI_MPIN_REQUEST_CODE);
            PaymentActivity activity = new PaymentActivity();
            mPromise = promise;
            paytmSDK.fetchUpiBalance(activity, upiDataRequestModel);
        } else {
            promise.reject(new Exception("Initialize paytm SDK first by calling initPaytmSDK method"));
        }
    }

    @ReactMethod
    public void payViaUPI(String paymentFlow, String selectedAppName, Promise promise) {
        List<UpiOptionsModel> apps = PaytmSDK.getPaymentsHelper().getUpiAppsInstalled(reactContext);
        for (UpiOptionsModel app : apps) {
            if (app.getAppName().equalsIgnoreCase(selectedAppName)) {
                UpiIntentRequestModel upiCollectRequestModel = new UpiIntentRequestModel(paymentFlow, selectedAppName, app.getResolveInfo().activityInfo);
                Activity currentActivity = getCurrentActivity();
                /*Intent intent = new Intent(getReactApplicationContext(), PaymentActivity.class);
                currentActivity.startActivityForResult(intent, SDKConstants.REQUEST_CODE_UPI_APP);*/
                //PaymentActivity activity = new PaymentActivity();
                /*Bundle bundle = new Bundle();
                activity.onCreate(bundle);*/
                mPromise = promise;
                Toast.makeText(reactContext, "payviaupi", Toast.LENGTH_LONG);
                paytmSDK.startTransaction(currentActivity, upiCollectRequestModel);
            }
        }
    }

    class TransactionListener implements PaytmSDKCallbackListener {

        @Override
        public void onTransactionResponse(TransactionInfo bundle) {
            if (bundle != null) {
                if (bundle.getTxnInfo() != null) {
                    String s = new Gson().toJson(bundle.getTxnInfo());
                    Toast.makeText(reactContext, s, Toast.LENGTH_SHORT).show();
                    mPromise.resolve(s);
                    paytmSDK.clear();
                }
            }
        }

        @Override
        public void networkError() {
            mPromise.reject(new Exception("NETWORK_ERROR"));
            paytmSDK.clear();
        }

        @Override
        public void onBackPressedCancelTransaction() {
            mPromise.reject(new Exception("USER_CANCELLED"));
            paytmSDK.clear();
        }

        @Override
        public void onGenericError(int errorCode, String errorMessage) {
            mPromise.reject(new Exception("GENERIC_ERROR:" + errorMessage));
            paytmSDK.clear();
        }
    }
}