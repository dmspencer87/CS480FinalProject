package com.cointransfer.android.coinexchange;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.coinbase.api.entity.OAuthTokensResponse;
import com.cointransfer.android.coinexchange.Network.CoinBaseApi;
import com.cointransfer.android.coinexchange.Network.QRDialogFragment;
import com.cointransfer.android.coinexchange.Network.SharedData;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements SharedData{

    private Button bLogin;
    private Button bSend;
    private Button qScanner;
    private TextView btcPrice;
    private TextView btcBalance;
    private TextView btcBalText;
    private EditText send_email;
    private EditText send_amount;
    private EditText send_note;
    private TextView getEmail;
    private TextView getAmount;
    private TextView getNote;
    private Button appLogout;


    CoinBaseApi api;
    OAuthTokensResponse oAuthRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new CoinBaseApi(this);
        init();

    }

    private void init() {
        bLogin = (Button) findViewById(R.id.bLogin);
        btcPrice = (TextView) findViewById(R.id.eText);
        btcBalance = (TextView) findViewById(R.id.btn_balance);
        btcBalText = (TextView) findViewById(R.id.btnBalanceText);
        send_email= (EditText) findViewById(R.id.email);
        send_amount = (EditText) findViewById(R.id.amount);
        send_note = (EditText) findViewById(R.id.note);
        getAmount = (TextView) findViewById(R.id.getAmount);
        getEmail = (TextView) findViewById(R.id.getEmail);
        getNote = (TextView) findViewById(R.id.getNote);
        bSend = (Button) findViewById(R.id.sentBTC);
        qScanner = (Button) findViewById(R.id.qr_scanner);
        appLogout = (Button) findViewById(R.id.logout);
        bLogin.setVisibility(View.VISIBLE);

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                api.login(MainActivity.this);
            }
        });

        bSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = send_email.getText().toString();
                String amount = send_amount.getText().toString();

//                if(isValidEmail(email) && isValidAmount(amount)){
                    api.sendBtc(email, amount , MainActivity.this);
//                }
                send_amount.setText("");
                send_email.setText("");
                send_note.setText("");

            }
        });
        qScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                BottomSheetDialogFragment dialog = new QRDialogFragment();
//                dialog.show(getSupportFragmentManager(), "Action");
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.setPrompt("Scan a QRcode");
                integrator.setOrientationLocked(false);
                integrator.initiateScan();


            }
        });
        appLogout.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                send_email.setText(result.getContents().replace("bitcoin:", ""));

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void setName(String name) {
        btcBalText.setText(name + "'s Wallet:");
    }


    private boolean isValidEmail(CharSequence email) {
        if (TextUtils.isEmpty(email)) {
            send_email.setError("Field is require");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            send_email.setError("Email is invalid");
            return false;
        }
        send_email.setError(null);
        return true;
    }


    @Override
    protected void onNewIntent(final Intent intent) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.VIEW")) {
            Log.d(TAG, "New Intent");
            //api.handleIntent(intent, MainActivity.this);
            api.completeLogin(MainActivity.this,intent.getData());
        }
    }

    @Override
    public void hideLogin(boolean view) {
        if(view){
            bLogin.setVisibility(View.GONE);
            btcBalance.setVisibility(View.VISIBLE);
            btcBalText.setVisibility(View.VISIBLE);
            send_email.setVisibility(View.VISIBLE);
            send_amount.setVisibility(View.VISIBLE);
            send_note.setVisibility(View.VISIBLE);
            getNote.setVisibility(View.VISIBLE);
            getEmail.setVisibility(View.VISIBLE);
            getAmount.setVisibility(View.VISIBLE);
            bSend.setVisibility(View.VISIBLE);
            qScanner.setVisibility(View.VISIBLE);
            appLogout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setPrice(Spannable price) {
        btcPrice.setText(price);
    }

    @Override
    public void errMsg(String text) {
        Log.d("ERROR", text);
    }


    @Override
    public void getEmail(String text) {

    }

    @Override
    public void saveOauthRespones(OAuthTokensResponse oauth) {
        this.oAuthRes = oauth;
    }

    @Override
    public void setBalance(Spannable price) {
        btcBalance.setText(price);
    }

    @Override
    public void printOauth() {
        Toast.makeText(this, oAuthRes.getAccessToken(), Toast.LENGTH_SHORT).show();
    }
}