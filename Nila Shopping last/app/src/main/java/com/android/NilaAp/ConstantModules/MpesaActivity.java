package com.android.NilaAp.ConstantModules;

import static com.android.NilaAp.ConstantModules.Constants.BUSINESS_SHORT_CODE;
import static com.android.NilaAp.ConstantModules.Constants.CALLBACKURL;
import static com.android.NilaAp.ConstantModules.Constants.PARTYB;
import static com.android.NilaAp.ConstantModules.Constants.PASSKEY;
import static com.android.NilaAp.ConstantModules.Constants.TRANSACTION_TYPE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.NilaAp.Model.AccessToken;
import com.android.NilaAp.Model.STKPush;
import com.android.NilaAp.R;
import com.android.NilaAp.Services.DarajaApiClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class MpesaActivity extends AppCompatActivity implements View.OnClickListener {

    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.etAmount)
    TextView mAmount;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.etPhone)
    EditText mPhone;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btnPay)
    Button mPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        int itemPrice = intent.getIntExtra("itemPrice", 0);

        mAmount.setText(String.valueOf(itemPrice));
        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.

        mPay.setOnClickListener(this);

        getAccessToken();

    }

    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    mApiClient.setAuthToken(response.body().accessToken);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }


    @Override
    public void onClick(View view) {
        if (view== mPay){
            String phone_number = mPhone.getText().toString();
            String amount = mAmount.getText().toString();
            performSTKPush(phone_number,amount);
        }
    }


    public void performSTKPush(String phone_number,String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = Utils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                Utils.getPassword(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                Utils.sanitizePhoneNumber(phone_number),
                PARTYB,
                Utils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "Nila Shop", //Account reference
                "nilaapp STK PUSH by janet"  //Transaction description
        );

        mApiClient.setGetAccessToken(false);
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        // Payment was successful
                        Timber.d("Payment successful. Response: %s", response.body());

                        // Navigate to the next activity or show a success message
//                        showSuccessMessage("Payment was successful!");
                        Toast.makeText(MpesaActivity.this, "Payment was successful!", Toast.LENGTH_SHORT).show();


                    } else {
                        // Payment failed or there was an error
                        Timber.e("Response: %s", response.errorBody().string());

                        // Handle the payment failure, display an error message, or take appropriate action
//                        showError("Payment failed. Please try again.");
                        Toast.makeText(MpesaActivity.this, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Timber.e(t);

                // Handle the failure, display an error message, or take appropriate action
//                showError("Payment failed. Please try again.");
                Toast.makeText(MpesaActivity.this, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
