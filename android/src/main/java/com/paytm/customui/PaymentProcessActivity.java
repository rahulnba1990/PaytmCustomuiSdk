package com.paytm.customui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.google.gson.Gson;

import net.one97.paytm.nativesdk.PaytmSDK;
import net.one97.paytm.nativesdk.app.PaytmSDKCallbackListener;
import net.one97.paytm.nativesdk.dataSource.PaytmPaymentsUtilRepository;
import net.one97.paytm.nativesdk.dataSource.models.UpiIntentRequestModel;
import net.one97.paytm.nativesdk.dataSource.models.WalletRequestModel;
import net.one97.paytm.nativesdk.instruments.upicollect.models.UpiOptionsModel;
import net.one97.paytm.nativesdk.transcation.model.TransactionInfo;

import java.util.List;

public class PaymentProcessActivity extends AppCompatActivity implements PaytmSDKCallbackListener {

    private Promise mPromise;

    private PaytmPaymentsUtilRepository paymentsUtilRepository = PaytmSDK.getPaymentsUtilRepository();

    private PaytmSDK paytmSDK = null;
    private final int TXN_START_CODE = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_process);
        Intent intent = this.getIntent();
        String paymentAction = intent.getStringExtra("paymentMode");
        if(paymentAction != null) {
            switch (paymentAction) {
                case "WALLET":
                    startWalletTransaction(intent);
                    break;
                case "UPI_INTENT":
                    startUPIIntentTransaction(intent);
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
        if(paymentFlow == null) {
            paymentFlow = "NONE";
        }
        WalletRequestModel walletRequestModel = new WalletRequestModel(paymentFlow);
        Toast.makeText(this, "payviawallet", Toast.LENGTH_LONG).show();
        paytmSDK.startTransaction(this, walletRequestModel);
    }

    private void startUPIIntentTransaction(Intent intent) {
        String paymentFlow = intent.getStringExtra("paymentFlow");
        if(paymentFlow == null) {
            paymentFlow = "NONE";
        }
        String selectedAppName = intent.getStringExtra("appName");
        List<UpiOptionsModel> apps = PaytmSDK.getPaymentsHelper().getUpiAppsInstalled(this);
        for (UpiOptionsModel app : apps) {
            if (app.getAppName().equalsIgnoreCase(selectedAppName)) {
                UpiIntentRequestModel upiCollectRequestModel = new UpiIntentRequestModel(paymentFlow, selectedAppName, app.getResolveInfo().activityInfo);
                Toast.makeText(this, "payviaupiintent", Toast.LENGTH_LONG).show();
                paytmSDK.startTransaction(this, upiCollectRequestModel);
            }
        }
    }

    @Override
    public void onTransactionResponse(TransactionInfo transactionInfo) {
        if (transactionInfo != null) {
            if (transactionInfo.getTxnInfo() != null) {
                String s = new Gson().toJson(transactionInfo.getTxnInfo());
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
                Intent mIntent = new Intent();
                mIntent.putExtra("result", s);
                setResult(RESULT_OK, mIntent);
                finishActivity(TXN_START_CODE);
                paytmSDK.clear();
            }
        } else {
            Intent mIntent = new Intent();
            mIntent.putExtra("result", "TXN_FAILURE");
            setResult(RESULT_OK, mIntent);
            finishActivity(TXN_START_CODE);
            paytmSDK.clear();
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
    public void onGenericError(int i, String s) {
        mPromise.reject(new Exception("GENERIC_ERROR:" + s));
        paytmSDK.clear();
    }
}