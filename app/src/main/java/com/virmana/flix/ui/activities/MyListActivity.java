package com.virmana.flix.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.entity.Channel;
import com.virmana.flix.entity.Data;
import com.virmana.flix.entity.Poster;
import com.virmana.flix.ui.Adapters.PosterAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyListActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipe_refresh_layout_list_my_list_search;
    private Button button_try_again;
    private LinearLayout linear_layout_layout_error;
    private RecyclerView recycler_view_activity_my_list;
    private ImageView image_view_empty_list;
    private GridLayoutManager gridLayoutManager;
    private PosterAdapter adapter;

    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;

    private Integer page = 0;
    private Integer position = 0;
    private Integer item = 0 ;
    ArrayList<Poster> posterArrayList = new ArrayList<>();
    ArrayList<Channel> channelArrayList = new ArrayList<>();

    private RelativeLayout relative_layout_load_more;
    private LinearLayout linear_layout_load_my_list_activity;

    private Integer lines_beetween_ads = 2 ;
    private boolean tabletSize;
    private Boolean native_ads_enabled = false ;
    private int type_ads = 0;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);
        prefManager= new PrefManager(getApplicationContext());

        initView();
        initAction();
        loadPosters();
        showAdsBanner();
    }




    private void loadPosters() {
        PrefManager prf= new PrefManager(MyListActivity.this.getApplicationContext());
        if (prf.getString("LOGGED").toString().equals("TRUE")){
                Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
                String   key_user=  prf.getString("TOKEN_USER");
                swipe_refresh_layout_list_my_list_search.setRefreshing(true);
                linear_layout_load_my_list_activity.setVisibility(View.VISIBLE);
                Retrofit retrofit = apiClient.getClient();
                apiRest service = retrofit.create(apiRest.class);
                Call<Data> call = service.myList(id_user,key_user);
                call.enqueue(new Callback<Data>() {
                    @Override
                    public void onResponse(Call<Data> call, final Response<Data> response) {
                        if (response.isSuccessful()){

                            if (response.body().getChannels() !=null){
                                for (int i = 0; i < response.body().getChannels().size(); i++) {
                                    channelArrayList.add(response.body().getChannels().get(i));
                                }
                            }

                            if (channelArrayList.size()>0){
                                posterArrayList.add(new Poster().setTypeView(3));
                                if (native_ads_enabled){
                                    Log.v("MYADS","ENABLED");
                                    if (tabletSize) {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                                        Log.v("MYADS","tabletSize");
                                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                            @Override
                                            public int getSpanSize(int position) {
                                                return ((position ) % (lines_beetween_ads + 1 ) == 0 || position == 0) ? 6 : 1;
                                            }
                                        });
                                    } else {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                            @Override
                                            public int getSpanSize(int position) {
                                                return ((position ) % (lines_beetween_ads + 1 ) == 0 || position == 0) ? 3 : 1;
                                            }
                                        });
                                    }
                                }else {
                                    if (tabletSize) {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                            @Override
                                            public int getSpanSize(int position) {
                                                return ( position == 0) ? 6 : 1;
                                            }
                                        });
                                    } else {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                            @Override
                                            public int getSpanSize(int position) {
                                                return ( position == 0) ? 3 : 1;
                                            }
                                        });
                                    }
                                }
                            }else{
                                if (native_ads_enabled){
                                    Log.v("MYADS","ENABLED");
                                    if (tabletSize) {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                                        Log.v("MYADS","tabletSize");
                                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                            @Override
                                            public int getSpanSize(int position) {
                                                return ((position  + 1) % (lines_beetween_ads  + 1  ) == 0 && position!=0) ? 6 : 1;
                                            }
                                        });
                                    } else {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                                        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                            @Override
                                            public int getSpanSize(int position) {
                                                return ((position  + 1) % (lines_beetween_ads + 1 ) == 0  && position!=0)  ? 3 : 1;
                                            }
                                        });
                                    }
                                }else {
                                    if (tabletSize) {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                                    } else {
                                        gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                                    }
                                }
                            }

                            if (response.body().getPosters() !=null){
                                for (int i = 0; i < response.body().getPosters().size(); i++) {
                                    posterArrayList.add(response.body().getPosters().get(i).setTypeView(1));
                                    if (native_ads_enabled){
                                        item++;
                                        if (item == lines_beetween_ads ){
                                            item= 0;
                                            if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("FACEBOOK")) {
                                                posterArrayList.add(new Poster().setTypeView(4));
                                            }else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("ADMOB")){
                                                posterArrayList.add(new Poster().setTypeView(5));
                                            } else if (prefManager.getString("ADMIN_NATIVE_TYPE").equals("BOTH")){
                                                if (type_ads == 0) {
                                                    posterArrayList.add(new Poster().setTypeView(4));
                                                    type_ads = 1;
                                                }else if (type_ads == 1){
                                                    posterArrayList.add(new Poster().setTypeView(5));
                                                    type_ads = 0;
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            if (channelArrayList.size() == 0 && posterArrayList.size() == 0){
                                linear_layout_layout_error.setVisibility(View.GONE);
                                recycler_view_activity_my_list.setVisibility(View.GONE);
                                image_view_empty_list.setVisibility(View.VISIBLE);
                            }else{
                                linear_layout_layout_error.setVisibility(View.GONE);
                                recycler_view_activity_my_list.setVisibility(View.VISIBLE);
                                image_view_empty_list.setVisibility(View.GONE);
                            }
                        }else{
                            linear_layout_layout_error.setVisibility(View.VISIBLE);
                            recycler_view_activity_my_list.setVisibility(View.GONE);
                            image_view_empty_list.setVisibility(View.GONE);
                        }
                        swipe_refresh_layout_list_my_list_search.setRefreshing(false);
                        linear_layout_load_my_list_activity.setVisibility(View.GONE);
                        recycler_view_activity_my_list.setLayoutManager(gridLayoutManager);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<Data> call, Throwable t) {
                        linear_layout_layout_error.setVisibility(View.VISIBLE);
                        recycler_view_activity_my_list.setVisibility(View.GONE);
                        image_view_empty_list.setVisibility(View.GONE);
                        swipe_refresh_layout_list_my_list_search.setVisibility(View.GONE);
                        linear_layout_load_my_list_activity.setVisibility(View.GONE);
                        swipe_refresh_layout_list_my_list_search.setRefreshing(false);


                    }
                });
        }else{
            Intent intent = new Intent(MyListActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
            finish();
        }
    }


    private void initAction() {



        swipe_refresh_layout_list_my_list_search.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                item = 0;
                page = 0;
                loading = true;
                channelArrayList.clear();
                posterArrayList.clear();
                adapter.notifyDataSetChanged();
                loadPosters();
            }
        });
        button_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                item = 0;
                page = 0;
                loading = true;
                channelArrayList.clear();
                posterArrayList.clear();
                adapter.notifyDataSetChanged();
                loadPosters();
            }
        });
    }

    private void initView() {

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (!prefManager.getString("ADMIN_NATIVE_TYPE").equals("FALSE")){
            native_ads_enabled=true;
            if (tabletSize) {
                lines_beetween_ads=6*Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }else{
                lines_beetween_ads=3*Integer.parseInt(prefManager.getString("ADMIN_NATIVE_LINES"));
            }
        }
        if (checkSUBSCRIBED()) {
            native_ads_enabled=false;
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("My list");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.linear_layout_load_my_list_activity=findViewById(R.id.linear_layout_load_my_list_activity);
        this.relative_layout_load_more=findViewById(R.id.relative_layout_load_more);
        this.swipe_refresh_layout_list_my_list_search=findViewById(R.id.swipe_refresh_layout_list_my_list_search);
        button_try_again            = findViewById(R.id.button_try_again);
        image_view_empty_list       = findViewById(R.id.image_view_empty_list);
        linear_layout_layout_error  = findViewById(R.id.linear_layout_layout_error);
        recycler_view_activity_my_list          = findViewById(R.id.recycler_view_activity_my_list);
        adapter = new PosterAdapter(posterArrayList,channelArrayList, this,true);

        if (native_ads_enabled){
            Log.v("MYADS","ENABLED");
            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
                Log.v("MYADS","tabletSize");
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return ((position  + 1) % (lines_beetween_ads  + 1  ) == 0 && position!=0) ? 6 : 1;
                    }
                });
            } else {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        return ((position  + 1) % (lines_beetween_ads + 1 ) == 0  && position!=0)  ? 3 : 1;
                    }
                });
            }
        }else {
            if (tabletSize) {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),6,RecyclerView.VERTICAL,false);
            } else {
                this.gridLayoutManager=  new GridLayoutManager(getApplicationContext(),3,RecyclerView.VERTICAL,false);
            }
        }
        recycler_view_activity_my_list.setHasFixedSize(true);
        recycler_view_activity_my_list.setAdapter(adapter);
        recycler_view_activity_my_list.setLayoutManager(gridLayoutManager);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem itemMenu) {
        switch (itemMenu.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
            }
            return true;
    }

    public boolean checkSUBSCRIBED(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE") && !prefManager.getString("NEW_SUBSCRIBE_ENABLED").equals("TRUE")) {
            return false;
        }
        return true;
    }
    public void showAdsBanner() {
        if (getString(R.string.AD_MOB_ENABLED_BANNER).equals("true")) {
            if (!checkSUBSCRIBED()) {
                PrefManager prefManager= new PrefManager(getApplicationContext());
                if (prefManager.getString("ADMIN_BANNER_TYPE").equals("FACEBOOK")){
                    showFbBanner();
                }
                if (prefManager.getString("ADMIN_BANNER_TYPE").equals("ADMOB")){
                    showAdmobBanner();
                }
                if (prefManager.getString("ADMIN_BANNER_TYPE").equals("BOTH")) {
                    if (prefManager.getString("Banner_Ads_display").equals("FACEBOOK")) {
                        prefManager.setString("Banner_Ads_display", "ADMOB");
                        showAdmobBanner();
                    } else {
                        prefManager.setString("Banner_Ads_display", "FACEBOOK");
                        showFbBanner();
                    }
                }
            }
        }
    }
    public void showAdmobBanner(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        LinearLayout linear_layout_ads =  (LinearLayout) findViewById(R.id.linear_layout_ads);
        final AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId(prefManager.getString("ADMIN_BANNER_ADMOB_ID"));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
        linear_layout_ads.addView(mAdView);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }
    public void showFbBanner(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        LinearLayout linear_layout_ads =  (LinearLayout) findViewById(R.id.linear_layout_ads);
        com.facebook.ads.AdView adView = new com.facebook.ads.AdView(this, prefManager.getString("ADMIN_BANNER_FACEBOOK_ID"), com.facebook.ads.AdSize.BANNER_HEIGHT_90);
        linear_layout_ads.addView(adView);
        adView.loadAd();

    }
}
