
# react-native-paytm-customui-sdk

This library is a react native implementation of Paytm's custom-ui SDK android/ios.
For more information you can visit https://developer.paytm.com/docs/custom-ui-sdk.
Current version works for Android only. Will support ios integration very soon.

## Getting started

`$ npm install react-native-paytm-customui-sdk --save`

### Mostly automatic installation

`$ react-native link react-native-paytm-customui-sdk`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-paytm-customui-sdk` and add `RNPaytmCustomuiSdk.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNPaytmCustomuiSdk.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
    - Add `import com.reactlibrary.RNPaytmCustomuiSdkPackage;` to the imports at the top of the file
    - Add `new RNPaytmCustomuiSdkPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-paytm-customui-sdk'
  	project(':react-native-paytm-customui-sdk').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-paytm-customui-sdk/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-paytm-customui-sdk')
  	```
  	
4. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import net.one97.paytm.nativesdk.PaytmSDK;`  to the imports at the top of the file
  - Add `PaytmSDK.init(this);` at the end of `onCreate()` method.

## Usage
```javascript
import PaytmCustomuiSdk from 'react-native-paytm-customui-sdk';
```
  
##API

All the APIs return promise. Please go to https://developer.paytm.com/docs/custom-ui-sdk
for more information about different function and parameter information.

####checkPaytmInstalled()
Checks if the Paytm App is installed on the device.

####Example
```javascript
PaytmCustomuiSdk.isPaytmAppInstalled().then(paytmInstalled => {
    // your logic
});
```

####initPaytmSDK()
Initialize Paytm SDK. It is required before starting any transaction.
1. Input Params:
    - mid - String - provided by Paytm
    - orderId - String
    - txnToken - String - received from initiate transaction API response via server
    - amount - float
    - isAssistEnabled - boolean
    - loggingEnabled - boolean
2. Returns "SUCCESS" as response string.

####Example
```javascript
PaytmCustomuiSdk.initPaytmSDK(mid, orderId, txnToken, amount, isAssistEnabled, loggingEnabled,
     customEndpoint, merchantCallbackUrl).then(response => {
    // returns "SUCCESS" as response after inititialization
});
```

####fetchAuthCode()
Fetch auth code for Paytm.
1. Input Params:
    - clientId - String - provided by Paytm
2. Returns authCode as string.  

####Example
```javascript
PaytmCustomuiSdk.fetchAuthCode(clientId).then(authCode => {
    // returns authCode as a string
});
```

####getUPIAppsInstalled()
Gets the list of UPI apps installed on the device.
1. Input Params:
    - clientId - String - provided by Paytm
2. Returns list of UPI app names as string.

####Example
```javascript
PaytmCustomuiSdk.getUPIAppsInstalled().then(apps => {
    // returns list of app names ex.['Paytm', 'GooglePay'] as string. parse it to JSON
    const appNames = JSON.parse(apps);
});
```

####payViaUPI()
Start paytm via Paytm UPI intent flow
1. Input Params:
    - paymentFlow - String - 	It’s value can be NONE,HYBRID,ADDNPAY
    - selectedAppName - any one value selected from app list you get it by calling getUPIAppsInstalled()
2. Returns TransactionInfo object data as string.

####Example
```javascript
PaytmCustomuiSdk.payViaUPI().then(txnInfo => {
    // returns TransactionInfo object data
    const appNames = JSON.parse(apps);
}).catch(err=>{
    
});
```
#####Sample TransactionInfo data:
```
{
    "ORDERID": "PARCEL15816826759",
    "MID": "AliSub58582630351896",
    "TXNID": "20200214111212800110168052313701129",
    "TXNAMOUNT": "1.00",
    "PAYMENTMODE": "CC",
    "CURRENCY": "INR",
    "TXNDATE": "2020-02-14 17:48:13.0",
    "STATUS": "TXN_SUCCESS",
    "RESPCODE": "01",
    "RESPMSG": "Txn Success",
    "MERC_UNQ_REF": "test4",
    "UDF_1": "test1",
    "UDF_2": "test2",
    "UDF_3": "test3",
    "ADDITIONAL_INFO": "test5",
    "GATEWAYNAME": "ICICIPAY",
    "BANKTXNID": "68568621250",
    "BANKNAME": "HSBC",
    "PROMO_CAMP_ID": "PROMO CODE",
    "PROMO_RESPCODE": "702",
    "PROMO_STATUS": "FAILURE"
}
```