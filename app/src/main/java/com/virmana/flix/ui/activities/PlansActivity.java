package com.virmana.flix.ui.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.entity.ApiResponse;
import com.virmana.flix.entity.Plan;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PlansActivity extends AppCompatActivity {

    private PlanAdapter planAdapter;
    private LinearLayout relative_layout_plans;
    private RelativeLayout relative_layout_loading;
    private RecyclerView recycler_view_plans;
    private RelativeLayout relative_layout_select_plan;

    private GridLayoutManager gridLayoutManager;
    private final List<Plan> planList = new ArrayList<>();
    private Integer selected_id = -1;
    private Integer selected_pos = -1;


    private static final int PAYPAL_REQUEST_CODE = 7777;

    private static PayPalConfiguration config ;

    private String method = "null";
    private ProgressDialog dialog_progress;
    private TextView text_view_activity_plans_method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        Bundle bundle = getIntent().getExtras() ;
        PrefManager prf= new PrefManager(PlansActivity.this.getApplicationContext());
        this.method =  bundle.getString("method");
        config =  new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION)
                .clientId(prf.getString("APP_PAYPAL_CLIENT_ID"));
        initView();
        initAction();
        loadPlans();
    }

    private void initView() {
            this.text_view_activity_plans_method = findViewById(R.id.text_view_activity_plans_method);
            this.relative_layout_select_plan = findViewById(R.id.relative_layout_select_plan);
            this.relative_layout_plans = findViewById(R.id.relative_layout_plans);
            this.relative_layout_loading = findViewById(R.id.relative_layout_loading);
            this.recycler_view_plans = findViewById(R.id.recycler_view_plans);
            this.gridLayoutManager = new GridLayoutManager(this, 1);
            this.planAdapter = new PlanAdapter();
            switch (method){
                case "pp":
                    text_view_activity_plans_method.setText("PayPal");
                    break;
                case "cc":
                    text_view_activity_plans_method.setText("Credit card");
                    break;
                case "cash":
                    text_view_activity_plans_method.setText("Cash");
                    break;

            }

    }

    private void initAction() {
        this.relative_layout_select_plan.setOnClickListener(v->{
            if (selected_id == -1){
                Toasty.error(getApplicationContext(),getResources().getString(R.string.select_plan),Toast.LENGTH_LONG).show();
            }else{
                switch (method){
                    case "pp":
                        PayPalPayNow();
                        break;
                    case "cc":
                        Intent intent = new Intent(PlansActivity.this,StripeActivity.class);
                        intent.putExtra("plan",selected_id);
                        intent.putExtra("name",planList.get(selected_pos).getTitle());
                        intent.putExtra("price",planList.get(selected_pos).getPrice());
                        startActivity(intent);
                        finish();
                        break;
                    case "cash":
                        Intent intent1 = new Intent(PlansActivity.this,CashActivity.class);
                        intent1.putExtra("plan",selected_id);
                        intent1.putExtra("name",planList.get(selected_pos).getTitle());
                        intent1.putExtra("price",planList.get(selected_pos).getPrice());
                        startActivity(intent1);
                        finish();
                        break;

                }
            }
        });
    }

    private void loadPlans() {
        relative_layout_plans.setVisibility(View.GONE);
        relative_layout_loading.setVisibility(View.VISIBLE);
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Plan>> call = service.getPlans();
        call.enqueue(new Callback<List<Plan>>() {
            @Override
            public void onResponse(Call<List<Plan>> call, final Response<List<Plan>> response) {
                if (response.isSuccessful()){
                    for (int i = 0; i <response.body().size() ; i++) {
                        planList.add(response.body().get(i));
                    }
                    relative_layout_plans.setVisibility(View.VISIBLE);
                    relative_layout_loading.setVisibility(View.GONE);

                    if (response.isSuccessful()) {
                        planList.clear();
                        for (int i = 0; i < response.body().size(); i++) {
                            planList.add(response.body().get(i));
                        }
                    }
                    recycler_view_plans.setHasFixedSize(true);
                    recycler_view_plans.setLayoutManager(gridLayoutManager);
                    recycler_view_plans.setAdapter(planAdapter);
                    planAdapter.notifyDataSetChanged();
                }else{
                    relative_layout_plans.setVisibility(View.GONE);
                    relative_layout_loading.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<Plan>> call, Throwable t) {
                relative_layout_plans.setVisibility(View.GONE);
                relative_layout_loading.setVisibility(View.GONE);
            }
        });
    }

    public class PlanAdapter extends  RecyclerView.Adapter<PlanAdapter.PlanHolder>{


        @Override
        public PlanHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan,parent, false);
            PlanHolder mh = new PlanHolder(v);
            return mh;
        }
        @Override
        public void onBindViewHolder(PlanHolder holder, final int position) {
            holder.text_view_plan_discount.setVisibility(View.GONE);
            holder.text_view_plan_description.setVisibility(View.GONE);

            if (planList.get(position).getDiscount() !=  null){
                if (planList.get(position).getDiscount().length()>0){
                    holder.text_view_plan_discount.setVisibility(View.VISIBLE);
                    holder.text_view_plan_discount.setText(planList.get(position).getDiscount());
                }
            }
            if (planList.get(position).getDescription() !=  null){
                if (planList.get(position).getDescription().length()>0){
                    holder.text_view_plan_description.setVisibility(View.VISIBLE);
                    holder.text_view_plan_description.setText(planList.get(position).getDescription());
                }
            }
            holder.text_view_plan_title.setText(planList.get(position).getTitle());
            holder.text_view_plan_price.setText(planList.get(position).getPrice()+ " "+new PrefManager(getApplicationContext()).getString("APP_CURRENCY"));
            if(planList.get(position).getId()   == selected_id){
                holder.card_view_plan.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            }else{
                holder.card_view_plan.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            }
            holder.card_view_plan.setOnClickListener(v -> {
                selected_id = planList.get(position).getId();
                selected_pos = position;
                planAdapter.notifyDataSetChanged();
            });
        }
        @Override
        public int getItemCount() {
            return planList.size();
        }
        public class PlanHolder extends RecyclerView.ViewHolder {
            private final TextView text_view_plan_discount;
            private final CardView card_view_plan;
            private final TextView text_view_plan_title;
            private final TextView text_view_plan_description;
            private final TextView text_view_plan_price;

            public PlanHolder(View itemView) {
                super(itemView);
                this.text_view_plan_discount =  (TextView) itemView.findViewById(R.id.text_view_plan_discount);
                this.card_view_plan =  (CardView) itemView.findViewById(R.id.card_view_plan);
                this.text_view_plan_title =  (TextView) itemView.findViewById(R.id.text_view_plan_title);
                this.text_view_plan_description =  (TextView) itemView.findViewById(R.id.text_view_plan_description);
                this.text_view_plan_price =  (TextView) itemView.findViewById(R.id.text_view_plan_price);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        JSONObject transaction = new JSONObject(paymentDetails);
                        JSONObject respone = new JSONObject(transaction.get("response").toString());

                        submitPayPal(respone.get("id").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)
                Toasty.error(this, "Cancel", Toast.LENGTH_SHORT).show();
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toasty.error(this, "Invalid", Toast.LENGTH_SHORT).show();
        }
    }
    private void PayPalPayNow() {
        PrefManager prf= new PrefManager(PlansActivity.this.getApplicationContext());
        if (prf.getString("LOGGED").toString().equals("TRUE")){
            Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
            String   key_user=  prf.getString("TOKEN_USER");
            PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(String.valueOf(planList.get(selected_pos).getPrice())),new PrefManager(getApplicationContext()).getString("APP_CURRENCY").toUpperCase(),
                    "Purchase Goods",PayPalPayment.PAYMENT_INTENT_SALE);
            payPalPayment.custom("user:"+id_user+",pack:"+planList.get(selected_pos).getId());
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
            startActivityForResult(intent,PAYPAL_REQUEST_CODE);
        }else{
            finish();
            Intent intent = new Intent(PlansActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }
    private void submitPayPal(String trans_id){
        dialog_progress= ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);

        PrefManager prf= new PrefManager(PlansActivity.this.getApplicationContext());
        if (prf.getString("LOGGED").toString().equals("TRUE")){
            Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
            String   key_user=  prf.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
                apiRest service = retrofit.create(apiRest.class);
                Call<ApiResponse> call = service.SubscriptionPayPal(id_user,key_user,trans_id,selected_id);
                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()){
                            if (response.body().getCode()==200){
                                Intent intent = new Intent(PlansActivity.this, FinishActivity.class);
                                intent.putExtra("title", response.body().getMessage());
                                startActivity(intent);
                                finish();
                                prf.setString("NEW_SUBSCRIBE_ENABLED","TRUE");
                            }else if (response.body().getCode()==201){
                                Intent intent = new Intent(PlansActivity.this, FinishActivity.class);
                                intent.putExtra("title", response.body().getMessage());
                                startActivity(intent);
                                finish();
                            }else{
                                Toasty.error(PlansActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toasty.error(PlansActivity.this,getResources().getString(R.string.operation_canceller), Toast.LENGTH_SHORT).show();
                        }
                        dialog_progress.dismiss();
                    }
                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toasty.error(PlansActivity.this,getResources().getString(R.string.operation_canceller), Toast.LENGTH_SHORT).show();
                        dialog_progress.dismiss();
                    }
                });
        }else{
            finish();
            Intent intent = new Intent(PlansActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            dialog_progress.dismiss();

        }
    }

}
