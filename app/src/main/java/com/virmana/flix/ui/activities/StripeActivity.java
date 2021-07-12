package com.virmana.flix.ui.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.entity.ApiResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StripeActivity extends AppCompatActivity {

    private String paymentIntentClientSecret;
    private Stripe stripe;
    private int plan_id;
    private ProgressDialog dialog_progress;
    private String plan_name;
    private Double price;
    private TextView text_view_activity_stripe_plan;
    private TextView text_view_activity_stripe_price;
    private RelativeLayout payButton;
    private CardInputWidget cardInputWidget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe);
        Bundle bundle = getIntent().getExtras() ;
        this.plan_id =  bundle.getInt("plan");
        this.plan_name =  bundle.getString("name");
        this.price =  bundle.getDouble("price");
        initView();
        initAction();
        submitPayPal();
        startCheckout();
    }

    private void initAction() {
        payButton.setOnClickListener((View view) -> {
            PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
            if (params != null) {
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                final Context context = getApplicationContext();
                stripe = new Stripe(
                        context,
                        PaymentConfiguration.getInstance(context).getPublishableKey()
                );
                stripe.confirmPayment(this, confirmParams);
                dialog_progress= ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);
            }
        });
    }

    private void initView() {
        this.text_view_activity_stripe_price = (TextView) findViewById(R.id.text_view_activity_stripe_price);
        this.text_view_activity_stripe_plan = (TextView) findViewById(R.id.text_view_activity_stripe_plan);
        this.payButton = findViewById(R.id.payButton);
        this.cardInputWidget = findViewById(R.id.cardInputWidget);

        text_view_activity_stripe_plan.setText(plan_name);
        text_view_activity_stripe_price.setText(price+" "+ new PrefManager(getApplicationContext()).getString("APP_CURRENCY"));
    }


    private void startCheckout() {
        PaymentConfiguration.init(
                getApplicationContext(),
                new PrefManager(getApplicationContext()).getString("APP_STRIPE_PUBLIC_KEY")
        );
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull(new PrefManager(getApplicationContext()).getString("APP_STRIPE_PUBLIC_KEY"))//Your publishable key
        );


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        stripe.onPaymentResult(requestCode, data, new PaymentResultCallback(this));
    }

    private  class PaymentResultCallback  implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<StripeActivity> activityRef;

        PaymentResultCallback(@NonNull StripeActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            dialog_progress.dismiss();
            final StripeActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String data = gson.toJson(paymentIntent);
                try {
                    JSONObject respone = new JSONObject(data);
                    checkPayment(respone.get("id").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                Toast.makeText(activity, "Payment failed", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final StripeActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            dialog_progress.dismiss();
            // Payment request failed – allow retrying using the same payment method
            Toast.makeText(activity, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void checkPayment(String trans_id){
        PrefManager prf= new PrefManager(StripeActivity.this.getApplicationContext());
        if (prf.getString("LOGGED").toString().equals("TRUE")){
            dialog_progress= ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);

            Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
            String   key_user=  prf.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<ApiResponse> call = service.SubscriptionStripe(id_user,key_user,trans_id,plan_id);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()){
                        if (response.body().getCode()==200){
                            Intent intent = new Intent(StripeActivity.this, FinishActivity.class);
                            intent.putExtra("title", response.body().getMessage());
                            startActivity(intent);
                            finish();
                            prf.setString("NEW_SUBSCRIBE_ENABLED","TRUE");
                        }else{
                            Intent intent = new Intent(StripeActivity.this, FinishActivity.class);
                            intent.putExtra("title", response.body().getMessage());
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Toasty.error(StripeActivity.this,getResources().getString(R.string.operation_canceller), Toast.LENGTH_SHORT).show();
                        finish();

                    }
                    dialog_progress.dismiss();
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    dialog_progress.dismiss();
                    Toasty.error(StripeActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }else{
            Intent intent = new Intent(StripeActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }
    private void submitPayPal(){
        PrefManager prf= new PrefManager(StripeActivity.this.getApplicationContext());
        if (prf.getString("LOGGED").toString().equals("TRUE")){
            dialog_progress= ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);
            Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
            String   key_user=  prf.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<ApiResponse> call = service.StripeIntent(id_user,key_user,plan_id);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()){
                        if (response.body().getCode()==200){
                            for (int i=0;i<response.body().getValues().size();i++){
                                if (response.body().getValues().get(i).getName().equals("client_secret")){
                                    paymentIntentClientSecret=response.body().getValues().get(i).getValue();
                                }
                            }
                        }else{
                            Toasty.error(StripeActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }else {
                        Toasty.error(StripeActivity.this,getResources().getString(R.string.operation_canceller), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    dialog_progress.dismiss();
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    dialog_progress.dismiss();
                    Toasty.error(StripeActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }else{
            Intent intent = new Intent(StripeActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }
}


