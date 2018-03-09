package com.cointransfer.android.coinexchange.Network;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.coinbase.android.sdk.OAuth;
import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.Account;
import com.coinbase.api.entity.AccountResponse;
import com.coinbase.api.entity.AccountsResponse;
import com.coinbase.api.entity.OAuthTokensResponse;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.User;
import com.coinbase.api.exception.UnauthorizedException;
import com.cointransfer.android.coinexchange.MainActivity;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.coinbase.android.sdk.OAuth;
import com.coinbase.api.Coinbase;
import com.coinbase.api.CoinbaseBuilder;
import com.coinbase.api.entity.Account;
import com.coinbase.api.entity.AccountResponse;
import com.coinbase.api.entity.AccountsResponse;
import com.coinbase.api.entity.OAuthTokensResponse;
import com.coinbase.api.entity.Transaction;
import com.coinbase.api.entity.User;
import com.coinbase.api.exception.UnauthorizedException;
import com.cointransfer.android.coinexchange.MainActivity;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel Spencer on 2/17/2018.
 */


public class CoinBaseApi {
    private static final String CLIENT_ID = "38471e3d2b78a323424234b5acc6896c0b80cbaaa32ddc95791a934613516b85";
    private static final String CLIENT_SECRET = "a92268650d7a611d814e4c8151cf00ab1b087779db3d17b3f17a269aeb1d610d";
    private static final String REDIRECT_URI = "cointransfer://coinbase-oauth";
    private static Coinbase cb;

    private SharedData data;
    private CompositeDisposable apiCalls;
    private OAuthTokensResponse Oauthentication;


    public CoinBaseApi(SharedData view){
        this.data = view;
        cb = new CoinbaseBuilder().build();
        apiCalls = new CompositeDisposable();
        this.setMoney();

    }
    /**
     * getNetwork observablel- JavaReactive thread
     *
     *
     */
    private <T> Observable<T> getNetworkObservable(Observable<T> observable) {
        return observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread());
    }

    /**
     * setBalance- worker thread
     *      accept - When authorized sets balance on MainActvity
     *
     */

    public void setBalance(){
        apiCalls.add(getAccountInfo()
                .subscribe(new Consumer<Money>() {
                    @Override
                    public void accept(Money money) throws Exception {
                        data.setBalance(getMoney(money));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        data.errMsg(throwable.getMessage());
                    }
                }));
    }
    /**
     * setMoney-
     *      accept - When authorized sets balance on MainActvity
     *      Exception Handler
     */



    public void setMoney(){
        apiCalls.add(getSpotPrice()
                .subscribe(new Consumer<Money>() {
                    @Override
                    public void accept(Money money) throws Exception {
                        data.setPrice(getMoney(money));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        data.errMsg(throwable.getMessage());
                    }
                }));
    }
    /**
     * getAccountinfo- worker thread
     *      When authorized sets balance sent to setBalance
     *
     */

    public Observable<Money> getAccountInfo() {
        return getNetworkObservable((Observable.create(new ObservableOnSubscribe<Money>() {
            @Override
            public void subscribe(ObservableEmitter<Money> e) throws Exception {
                AccountsResponse account = cb.getAccounts();
                List<Account> acc = account.getAccounts();
                e.onNext(acc.get(0).getBalance());
            }
        })));
    }
    /**
     * getSpotPrice- worker thread
     *      accept - When authorized sets bitcoin amount  to setMoney
     *
     */
    public Observable<Money> getSpotPrice() {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<Money>() {
            @Override
            public void subscribe(ObservableEmitter<Money> e) throws Exception {
                e.onNext(cb.getSpotPrice(CurrencyUnit.USD));
            }
        }));
    }

    /**
     * login- start Oauth2 process with coinbase
     *
     *      creates new intent handled on MainActivity
     */
    public void login(final Context context) {
        try {
            OAuth.beginAuthorization(context, CLIENT_ID, "user", REDIRECT_URI, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getMoney-
     *
     *      Retrives Spannable currency amount from Money object
     */
    private Spannable getMoney(Money money) {
        String m = money.toString();
        SpannableString span = new SpannableString(m);
        span.setSpan(new RelativeSizeSpan(0.5f), 0, m.indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }


    /**
     * completeLogin -exception Handler for completeOauth
     *      accept - Listener thread handles UI changes
     *      Exception Handler
     */
    public void completeLogin(final Context context, Uri uri){
        data.hideLogin(false);
        apiCalls.add(completeOauth(context,uri, data).subscribe(new Consumer<User>() {
            @Override
            public void accept(User user) throws Exception {
                setData(user);
                data.setName(user.getName());
                data.hideLogin(true);
                Toast.makeText(context,"OAUTH COMPLETED", Toast.LENGTH_SHORT).show();

            }
        }));
    }
    private void setData(User user){
        this.setBalance();
        data.getEmail(user.getName());

    }
    /**
     * completeOauth- worker thread
     *      waits for response from coinbaseAPI
     *      Exception Handler
     */

    public Observable<User> completeOauth(final Context context, final Uri uri, final SharedData d) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<User>() {
            @Override
            public void subscribe(ObservableEmitter<User> e) throws Exception {
                OAuthTokensResponse response = OAuth.completeAuthorization(context, CLIENT_ID, CLIENT_SECRET, uri);
                data.saveOauthRespones(response);
                Oauthentication = response;
                Log.d(TAG,response.getAccessToken());
                cb = new CoinbaseBuilder()
                        .withAccessToken(response.getAccessToken())
                        .build();
                e.onNext(cb.getUser());
            }
        }));
    }
    /**
     * sendBTC-
     *      accept - Create Toast notification if sent
     *      Exception Handler
     */
    public void sendBtc(String email, String amount,  final Context context) {
        Transaction t = new Transaction();
        t.setTo(email);
        t.setAmount(Money.of(CurrencyUnit.USD, Integer.parseInt(amount)));
        t.setNotes("CB");
        apiCalls.add(tansferMoney(t).subscribe(new Consumer<Transaction>() {
            @Override
            public void accept(Transaction transaction) throws Exception {
                data.errMsg("Success");
                Toast.makeText(context, "Sent", Toast.LENGTH_SHORT).show();
                setBalance();

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                data.errMsg(throwable.toString());
                Toast.makeText(context, throwable.toString(), Toast.LENGTH_SHORT).show();

            }
        }));
    }






    public Observable<Transaction> tansferMoney(final Transaction transaction) {
        return getNetworkObservable(Observable.create(new ObservableOnSubscribe<Transaction>() {
            @Override
            public void subscribe(ObservableEmitter<Transaction> e) throws Exception {

                Transaction r = cb.sendMoney(transaction);
                e.onNext(r);
            }
        }));
    }
}
