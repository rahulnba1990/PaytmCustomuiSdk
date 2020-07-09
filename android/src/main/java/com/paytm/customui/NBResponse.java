package com.paytm.customui;

import org.json.JSONObject;

import java.util.List;

public class NBResponse extends JSONObject {
    List<PayChannelOptions> payChannelOptions;

    public List<PayChannelOptions> getPayChannelOptions() {
        return payChannelOptions;
    }

    public void setPayChannelOptions(List<PayChannelOptions> payChannelOptions) {
        this.payChannelOptions = payChannelOptions;
    }
}
