package com.paytm.customui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.google.gson.Gson;

import net.one97.paytm.nativesdk.PaytmSDK;
import net.one97.paytm.nativesdk.app.PaytmSDKCallbackListener;
import net.one97.paytm.nativesdk.dataSource.PaytmPaymentsUtilRepository;
import net.one97.paytm.nativesdk.dataSource.models.UpiDataRequestModel;
import net.one97.paytm.nativesdk.dataSource.models.UpiIntentRequestModel;
import net.one97.paytm.nativesdk.dataSource.models.WalletRequestModel;
import net.one97.paytm.nativesdk.instruments.upicollect.models.UpiOptionsModel;
import net.one97.paytm.nativesdk.transcation.model.TransactionInfo;

import java.util.List;

public class PaymentProcessActivity extends AppCompatActivity implements PaytmSDKCallbackListener {

    private Promise mPromise;

    private PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();

    private PaytmSDK paytmSDK = null;
    private final int FETCH_UPI_BALANCE_REQUEST_CODE = 100;
    private final int SET_UPI_MPIN_REQUEST_CODE = 101;
    private final int TXN_START_CODE = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_process);
        Intent intent = this.getIntent();
        String paymentAction = intent.getStringExtra("paymentAction");
        if (paymentAction != null) {
            switch (paymentAction) {
                case "WALLET":
                    startWalletTransaction(intent);
                    break;
                case "UPI_INTENT":
                    startUPIIntentTransaction(intent);
                    break;
                case "FETCH_UPI_BALANCE":
                    fetchUPIBalance(intent);
                    break;
                case "SET_UPI_MPIN":
                    setUpiMpin(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private void initPaytmSdk(Intent intent) {
        String mid = intent.getStringExtra("mid");
        String orderId = intent.getStringExtra("orderId");
        double amount = intent.getDoubleExtra("amount", 0);
        String txnToken = intent.getStringExtra("txnToken");
        boolean isAssistEnabled = intent.getBooleanExtra("isAssistEnabled", true);
        boolean loggingEnabled = intent.getBooleanExtra("loggingEnabled", false);
        String customEndpoint = intent.getStringExtra("customEndpoint");
        String merchantCallbackUrl = intent.getStringExtra("merchantCallbackUrl");
        PaytmSDK.Builder builder = new PaytmSDK.Builder(this, mid, orderId, txnToken, amount, this);
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

    private void startWalletTransaction(Intent intent) {
        initPaytmSdk(intent);
        String paymentFlow = intent.getStringExtra("paymentFlow");
        if (paymentFlow == null) {
            paymentFlow = "NONE";
        }
        WalletRequestModel walletRequestModel = new WalletRequestModel(paymentFlow);
        paytmSDK.startTransaction(this, walletRequestModel);
    }

    private void startUPIIntentTransaction(Intent intent) {
        initPaytmSdk(intent);
        String paymentFlow = intent.getStringExtra("paymentFlow");
        if (paymentFlow == null) {
            paymentFlow = "NONE";
        }
        String selectedAppName = intent.getStringExtra("appName");
        List<UpiOptionsModel> apps = PaytmSDK.getPaymentsHelper().getUpiAppsInstalled(this);
        for (UpiOptionsModel app : apps) {
            if (app.getAppName().equalsIgnoreCase(selectedAppName)) {
                UpiIntentRequestModel upiCollectRequestModel = new UpiIntentRequestModel(paymentFlow, selectedAppName, app.getResolveInfo().activityInfo);
                paytmSDK.startTransaction(this, upiCollectRequestModel);
            }
        }
    }

    private void fetchUPIBalance(Intent intent) {
        initPaytmSdk(intent);
        String upiId = intent.getStringExtra("upiId");
        String bankAccountJson = intent.getStringExtra("bankAccountJson");
        if (upiId != null && bankAccountJson != null) {
            UpiDataRequestModel upiDataRequestModel = new UpiDataRequestModel(upiId, bankAccountJson, FETCH_UPI_BALANCE_REQUEST_CODE);
            paytmSDK.fetchUpiBalance(this, upiDataRequestModel);
        } else {
            Intent mIntent = new Intent();
            mIntent.putExtra("response", "TXN_FAILURE");
            setResult(RESULT_OK, mIntent);
            finish();
            paytmSDK.clear();
        }
    }

    private void setUpiMpin(Intent intent) {
        initPaytmSdk(intent);
        String vpa = intent.getStringExtra("vpa");
        String bankAccountString = intent.getStringExtra("bankAccountString");
        if (vpa != null && bankAccountString != null) {
            UpiDataRequestModel upiDataRequestModel = new UpiDataRequestModel(vpa, bankAccountString, SET_UPI_MPIN_REQUEST_CODE);
            paytmSDK.fetchUpiBalance(this, upiDataRequestModel);
        } else {
            Intent mIntent = new Intent();
            mIntent.putExtra("response", "TXN_FAILURE");
            setResult(RESULT_OK, mIntent);
            finish();
            paytmSDK.clear();
        }
    }

    @Override
    public void onTransactionResponse(TransactionInfo transactionInfo) {
        if (transactionInfo != null) {
            if (transactionInfo.getTxnInfo() != null) {
                String s = new Gson().toJson(transactionInfo.getTxnInfo());
                Intent mIntent = new Intent();
                mIntent.putExtra("response", s);
                setResult(RESULT_OK, mIntent);
                finish();
                paytmSDK.clear();
            }
        } else {
            Intent mIntent = new Intent();
            mIntent.putExtra("response", "TXN_FAILURE");
            setResult(RESULT_OK, mIntent);
            finish();
            paytmSDK.clear();
        }
    }

    @Override
    public void networkError() {
        Intent mIntent = new Intent();
        mIntent.putExtra("response", "NETWORK_ERROR");
        setResult(RESULT_OK, mIntent);
        finish();
        paytmSDK.clear();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Intent mIntent = new Intent();
        mIntent.putExtra("response", "USER_CANCELLED");
        setResult(RESULT_OK, mIntent);
        finish();
        paytmSDK.clear();
    }

    @Override
    public void onGenericError(int i, String s) {
        Intent mIntent = new Intent();
        mIntent.putExtra("response", "GENERIC_ERROR:" + s);
        setResult(RESULT_OK, mIntent);
        finish();
        paytmSDK.clear();
    }
}