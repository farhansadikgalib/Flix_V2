package com.virmana.flix.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.entity.ApiResponse;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PasswordActivity extends AppCompatActivity {
    private TextInputEditText           text_input_editor_text_activity_password_new;
    private TextInputEditText           text_input_editor_text_activity_password_confirm;
    private TextInputEditText           text_input_editor_text_activity_password_old;
    private TextInputLayout             text_input_layout_activity_password_new;
    private TextInputLayout             text_input_layout_activity_password_old;
    private TextInputLayout             text_input_layout_activity_password_confirm;
    private RelativeLayout              relative_layout_edit_activity_save;
    private ProgressDialog              login_progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        initView();
        initAction();
    }
    private void initView(){
        this.text_input_editor_text_activity_password_new=(TextInputEditText) findViewById(R.id.text_input_editor_text_activity_password_new);
        this.text_input_editor_text_activity_password_confirm=(TextInputEditText) findViewById(R.id.text_input_editor_text_activity_password_confirm);
        this.text_input_editor_text_activity_password_old=(TextInputEditText) findViewById(R.id.text_input_editor_text_activity_password_old);

        this.text_input_layout_activity_password_new= (TextInputLayout) findViewById(R.id.text_input_layout_activity_password_new);
        this.text_input_layout_activity_password_old= (TextInputLayout) findViewById(R.id.text_input_layout_activity_password_old);
        this.text_input_layout_activity_password_confirm= (TextInputLayout) findViewById(R.id.text_input_layout_activity_password_confirm);

        this.relative_layout_edit_activity_save=(RelativeLayout) findViewById(R.id.relative_layout_edit_activity_save);


    }
    private void initAction(){

        this.relative_layout_edit_activity_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }
    private void submitForm() {
        if (!validatePassword(text_input_editor_text_activity_password_old,text_input_layout_activity_password_old)) {
            return;
        }
        if (!validatePassword(text_input_editor_text_activity_password_new,text_input_layout_activity_password_new)) {
            return;
        }
        if (!validatePasswordConfrom()) {
            return;
        }
        login_progress= ProgressDialog.show(this,null,getString(R.string.operation_progress));

        PrefManager prf= new PrefManager(getApplicationContext());
        String id_ser=  prf.getString("ID_USER");

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<ApiResponse> call = service.changePassword(id_ser,text_input_editor_text_activity_password_old.getText().toString(),text_input_editor_text_activity_password_new.getText().toString());
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if(response.body()!=null){
                    int code = response.body().getCode();
                    String message=response.body().getMessage();
                    if (code==200){
                        String salt_user="0";
                        String token_user="0";
                        for (int i=0;i<response.body().getValues().size();i++){
                            if (response.body().getValues().get(i).getName().equals("salt")){
                                salt_user=response.body().getValues().get(i).getValue();
                            }
                            if (response.body().getValues().get(i).getName().equals("token")){
                                token_user=response.body().getValues().get(i).getValue();
                            }

                        }
                        PrefManager prf= new PrefManager(getApplicationContext());


                        prf.setString("SALT_USER",salt_user);
                        prf.setString("TOKEN_USER",token_user);
                        prf.setString("LOGGED","TRUE");

                        Toasty.success(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                        finish();
                        login_progress.dismiss();
                    } else if (code == 500) {
                        text_input_editor_text_activity_password_old.setError(response.body().getMessage().toString());
                        requestFocus(text_input_editor_text_activity_password_old);
                        login_progress.dismiss();
                    }
                }else{
                    Toasty.error(getApplicationContext(),getString(R.string.error_server),Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toasty.error(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                login_progress.dismiss();

            }
        });
    }

    private boolean validatePassword(TextInputEditText et,TextInputLayout tIL) {
        if (et.getText().toString().trim().isEmpty() || et.getText().length()  < 6 ) {
            tIL.setError(getString(R.string.error_short_value));
            requestFocus(et);
            return false;
        } else {
            tIL.setErrorEnabled(false);
        }
        return true;
    }
    private boolean validatePasswordConfrom() {
        if (!text_input_editor_text_activity_password_new.getText().toString().equals(text_input_editor_text_activity_password_confirm.getText().toString())) {
            text_input_layout_activity_password_confirm.setError(getString(R.string.password_confirm_message));
            requestFocus(text_input_editor_text_activity_password_confirm);
            return false;
        } else {
            text_input_layout_activity_password_confirm.setErrorEnabled(false);
        }
        return true;
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
