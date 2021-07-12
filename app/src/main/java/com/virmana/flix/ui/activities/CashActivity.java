package com.virmana.flix.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.ProgressRequestBody;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.entity.ApiResponse;

import java.io.File;

import es.dmoral.toasty.Toasty;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CashActivity extends AppCompatActivity  implements  ProgressRequestBody.UploadCallbacks{

    private TextInputLayout text_input_layout_activity_cash_trans;
    private TextInputLayout text_input_layout_activity_cash_infos;
    private TextInputEditText text_input_editor_text_activity_cash_trans;
    private TextInputEditText text_input_editor_text_activity_cash_infos;
    private RelativeLayout payButton;

    private int plan_id;
    private ProgressDialog dialog_progress;
    private String plan_name;
    private Double price;
    private TextView text_view_activity_cash_plan;
    private TextView text_view_activity_cash_account;
    private TextView text_view_activity_cash_price;
    private RelativeLayout select_file;
    private int PICK_IMAGE = 1557;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras() ;
        this.plan_id =  bundle.getInt("plan");
        this.plan_name =  bundle.getString("name");
        this.price =  bundle.getDouble("price");
        setContentView(R.layout.activity_cash);
        initView();
        initAction();
    }

    private void initView() {
        this.text_input_layout_activity_cash_trans =  findViewById(R.id.text_input_layout_activity_cash_trans);
        this.text_input_layout_activity_cash_infos =  findViewById(R.id.text_input_layout_activity_cash_infos);


        this.text_input_editor_text_activity_cash_trans =  findViewById(R.id.text_input_editor_text_activity_cash_trans);
        this.text_input_editor_text_activity_cash_infos =  findViewById(R.id.text_input_editor_text_activity_cash_infos);

        this.text_view_activity_cash_plan =  findViewById(R.id.text_view_activity_cash_plan);
        this.text_view_activity_cash_account =  findViewById(R.id.text_view_activity_cash_account);
        this.text_view_activity_cash_price =  findViewById(R.id.text_view_activity_cash_price);

        this.payButton =  findViewById(R.id.payButton);
        this.select_file =  findViewById(R.id.select_file);

        text_view_activity_cash_price.setText(price+ " "+new PrefManager(getApplicationContext()).getString("APP_CURRENCY"));
        text_view_activity_cash_plan.setText(plan_name);
        text_view_activity_cash_account.setText(new PrefManager(getApplicationContext()).getString("APP_CASH_ACCOUNT"));
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK
                && null != data) {


            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            imageUrl = picturePath  ;


        } else {

            Log.i("SonaSys", "resultCode: " + resultCode);
            switch (resultCode) {
                case 0:
                    Log.i("SonaSys", "User cancelled");
                    break;
                case -1:
                    break;
            }
        }
    }
    private void initAction() {
        this.payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validatName(text_input_editor_text_activity_cash_trans,text_input_layout_activity_cash_trans))
                    return;
                if(!validatName(text_input_editor_text_activity_cash_infos,text_input_layout_activity_cash_infos))
                    return;

                submit(text_input_editor_text_activity_cash_trans.getText().toString(),text_input_editor_text_activity_cash_infos.getText().toString());
            }
        });
        this.select_file.setOnClickListener(v -> {
            SelectImage();
        });
    }
    private boolean validatName(TextInputEditText et,TextInputLayout lt) {
        if (et.getText().toString().trim().isEmpty() || et.getText().length()  < 5 ) {
            lt.setError(getString(R.string.error_short_value));
            requestFocus(et);
            return false;
        } else {
            lt.setErrorEnabled(false);
        }
        return true;
    }
    private void SelectImage() {
        if (ContextCompat.checkSelfPermission(CashActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CashActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
        }else{
            Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, PICK_IMAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    SelectImage();
                }
                return;
            }
        }
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void submit(String trans_id,String trans_infos){

        PrefManager prf= new PrefManager(CashActivity.this.getApplicationContext());
        if (prf.getString("LOGGED").toString().equals("TRUE")){
            dialog_progress= ProgressDialog.show(this, null,getResources().getString(R.string.operation_progress), true);

            Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
            String   key_user=  prf.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);


            MultipartBody.Part body = null;
            if (imageUrl != null){
                File file1 = new File(imageUrl);
                int file_size = Integer.parseInt(String.valueOf(file1.length() / 1024 / 1024));
                if (file_size > 20) {
                    Toasty.error(getApplicationContext(), "Max file size allowed 20M", Toast.LENGTH_LONG).show();
                }
                Log.v("SIZE", file1.getName() + "");



                final File file = new File(imageUrl);


                ProgressRequestBody requestFile = new ProgressRequestBody(file, CashActivity.this);

                body  = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);
            }


            Call<ApiResponse> call = service.SubscriptionCash(body, id_user,key_user,trans_id,trans_infos,plan_id);
            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()){
                        if (response.body().getCode()==200){

                            Intent intent = new Intent(CashActivity.this, FinishActivity.class);
                            intent.putExtra("title", response.body().getMessage());
                            startActivity(intent);
                            finish();

                        }else{
                            Intent intent = new Intent(CashActivity.this, FinishActivity.class);
                            intent.putExtra("title", response.body().getMessage());
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Toasty.error(CashActivity.this,getResources().getString(R.string.operation_canceller), Toast.LENGTH_SHORT).show();
                        finish();

                    }
                    dialog_progress.dismiss();
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    dialog_progress.dismiss();
                    Toasty.error(CashActivity.this,t.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }else{
            Intent intent = new Intent(CashActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }

    @Override
    public void onProgressUpdate(int percentage) {
        dialog_progress.setProgress(percentage);
    }

    @Override
    public void onError() {
        dialog_progress.dismiss();
        dialog_progress.cancel();
    }

    @Override
    public void onFinish() {
        dialog_progress.dismiss();
        dialog_progress.cancel();

    }
}
