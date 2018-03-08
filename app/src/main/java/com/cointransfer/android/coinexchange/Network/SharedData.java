package com.cointransfer.android.coinexchange.Network;

import android.text.Spannable;

import com.coinbase.api.entity.Account;
import com.coinbase.api.entity.OAuthTokensResponse;

import java.math.BigDecimal;

/**
 * Created by Daniel Spencer on 3/3/2018.
 */

public interface SharedData {
    void hideLogin(boolean view);
    void setPrice(Spannable price);
    void errMsg(String text);
    void setBalance(Spannable price);
    void saveOauthRespones(OAuthTokensResponse oauth);
    void getEmail(String text);
    void printOauth();
    void setName(String name);
}
