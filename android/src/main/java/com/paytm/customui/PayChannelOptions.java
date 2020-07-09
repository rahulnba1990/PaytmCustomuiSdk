package com.paytm.customui;

public class PayChannelOptions {
    String channelName;
    String channelCode;
    String iconUrl;
    HasLowSuccess hasLowSuccess;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public HasLowSuccess getHasLowSuccess() {
        return hasLowSuccess;
    }

    public void setHasLowSuccess(HasLowSuccess hasLowSuccess) {
        this.hasLowSuccess = hasLowSuccess;
    }
}
