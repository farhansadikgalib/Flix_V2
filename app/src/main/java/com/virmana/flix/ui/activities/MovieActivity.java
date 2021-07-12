package com.virmana.flix.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jackandphantom.blurimage.BlurImage;
import com.orhanobut.hawk.Hawk;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.config.Global;
import com.virmana.flix.entity.Actor;
import com.virmana.flix.entity.ApiResponse;
import com.virmana.flix.entity.Comment;
import com.virmana.flix.entity.DownloadItem;
import com.virmana.flix.entity.Language;
import com.virmana.flix.entity.Poster;
import com.virmana.flix.entity.Source;
import com.virmana.flix.entity.Subtitle;
import com.virmana.flix.event.CastSessionEndedEvent;
import com.virmana.flix.event.CastSessionStartedEvent;
import com.virmana.flix.services.DownloadService;
import com.virmana.flix.services.ToastService;
import com.virmana.flix.ui.Adapters.ActorAdapter;
import com.virmana.flix.ui.Adapters.CommentAdapter;
import com.virmana.flix.ui.Adapters.GenreAdapter;
import com.virmana.flix.ui.Adapters.PosterAdapter;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MovieActivity extends AppCompatActivity {
    private  static String TAG= "MovieActivity";
    private CastContext mCastContext;
    private SessionManager mSessionManager;
    private CastSession mCastSession;
    private final SessionManagerListener mSessionManagerListener =
            new SessionManagerListenerImpl();

    //views
    private ImageView image_view_activity_movie_background;
    private ImageView image_view_activity_movie_cover;
    private TextView text_view_activity_movie_title;
    private TextView text_view_activity_movie_sub_title;
    private TextView text_view_activity_movie_description;
    private TextView text_view_activity_movie_year;
    private TextView text_view_activity_movie_duration;
    private TextView text_view_activity_movie_classification;
    private RatingBar rating_bar_activity_movie_rating;
    private RecyclerView recycle_view_activity_movie_genres;
    private FloatingActionButton floating_action_button_activity_movie_play;
    private FloatingActionButton floating_action_button_activity_movie_comment;

    private LinearLayout linear_layout_activity_movie_cast;
    private RecyclerView recycle_view_activity_activity_movie_cast;
    private LinearLayoutManager linearLayoutManagerCast;
    private LinearLayout linear_layout_movie_activity_trailer;
    private LinearLayout linear_layout_movie_activity_rate;
    // lists
    private ArrayList<Comment> commentList= new ArrayList<>();
    private ArrayList<Subtitle> subtitlesForCast = new ArrayList<>();

    private List<Source> downloadableList =  new ArrayList<>();
    private ArrayList<Source> playSources = new ArrayList<>();
    // movie
    private Poster poster;

    // layout manager
    private LinearLayoutManager linearLayoutManagerComments;
    private LinearLayoutManager linearLayoutManagerSources;
    private LinearLayoutManager linearLayoutManagerGenre;
    private LinearLayoutManager linearLayoutManagerMoreMovies;
    private LinearLayoutManager linearLayoutManagerDownloadSources;

    // adapters
    private GenreAdapter genreAdapter;
    private ActorAdapter actorAdapter;
    private CommentAdapter commentAdapter;
    private PosterAdapter posterAdapter;


    private LinearLayout linear_layout_movie_activity_trailer_clicked;
    private RelativeLayout relative_layout_subtitles_loading;
    private RecyclerView recycle_view_activity_activity_movie_more_movies;
    private LinearLayout linear_layout_activity_movie_more_movies;
    private LinearLayout linear_layout_activity_movie_my_list;
    private ImageView image_view_activity_movie_my_list;
    private Dialog play_source_dialog;
    private Dialog download_source_dialog;
    private LinearLayout linear_layout_movie_activity_download;
    private LinearLayout linear_layout_movie_activity_share;
    private String from;
    private int tryed = 0;
    private LinearLayout linear_layout_activity_movie_rating;
    private LinearLayout linear_layout_activity_movie_imdb;
    private TextView text_view_activity_movie_imdb_rating;

    private RewardedVideoAd mRewardedVideoAd;


    private  Boolean DialogOpened = false;
    private  Boolean fromLoad = false;
    private  int operationAfterAds = 0;
    private  int current_position_play=-1 ;
    private  int current_position_download=-1 ;

    IInAppBillingService mService;

    private static final String LOG_TAG = "iabv3";
    // put your Google merchant id here (as stated in public profile of your Payments Merchant Center)
    // if filled library will provide protection against Freedom alike Play Market simulators
    private static final String MERCHANT_ID=null;

    private BillingProcessor bp;
    private boolean readyToPurchase = false;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };
    private Dialog dialog;
    private boolean autoDisplay = false;
    private PrefManager prefManager;

    private String payment_methode_id = "null";
    private ProgressBar progress_bar_activity_movie_my_list;

    private class SessionManagerListenerImpl implements SessionManagerListener {
        @Override
        public void onSessionStarting(Session session) {
            Log.d(TAG,"onSessionStarting");
        }

        @Override
        public void onSessionStarted(Session session, String s) {
            Log.d(TAG,"onSessionStarted");
            invalidateOptionsMenu();
            EventBus.getDefault().post(new CastSessionStartedEvent());
        }

        @Override
        public void onSessionStartFailed(Session session, int i) {
            Log.d(TAG,"onSessionStartFailed");
        }

        @Override
        public void onSessionEnding(Session session) {
            Log.d(TAG,"onSessionEnding");
            EventBus.getDefault().post(new CastSessionEndedEvent(session.getSessionRemainingTimeMs()));
        }

        @Override
        public void onSessionEnded(Session session, int i) {
            Log.d(TAG,"onSessionEnded");
        }

        @Override
        public void onSessionResuming(Session session, String s) {
            Log.d(TAG,"onSessionResuming");
        }

        @Override
        public void onSessionResumed(Session session, boolean b) {
            Log.d(TAG,"onSessionResumed");
            invalidateOptionsMenu();
        }

        @Override
        public void onSessionResumeFailed(Session session, int i) {
            Log.d(TAG,"onSessionResumeFailed");
        }

        @Override
        public void onSessionSuspended(Session session, int i) {
            Log.d(TAG,"onSessionSuspended");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSessionManager = CastContext.getSharedInstance(this).getSessionManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        mCastContext = CastContext.getSharedInstance(this);
        prefManager= new PrefManager(getApplicationContext());

        initView();
        initAction();
        getMovie();
        setMovie();
        getPosterCastings();
        getRandomMovies();
        checkFavorite();
        setDownloadableList();
        setPlayableList();
        showAdsBanner();
        initRewarded();
        loadRewardedVideoAd();


        initBuy();

    }

    public void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(prefManager.getString("ADMIN_REWARDED_ADMOB_ID"),
                new AdRequest.Builder().build());
    }



    public void initRewarded() {

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                if (autoDisplay){
                    autoDisplay = false;
                    mRewardedVideoAd.show();
                }
                Log.d("Rewarded","onRewardedVideoAdLoaded ");

            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d("Rewarded","onRewardedVideoAdOpened ");
            }

            @Override
            public void onRewardedVideoStarted() {
                Log.d("Rewarded","onRewardedVideoStarted ");

            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardedVideoAd();
                Log.d("Rewarded","onRewardedVideoAdClosed ");

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                dialog.dismiss();
                Toasty.success(getApplicationContext(),getString(R.string.use_content_for_free)).show();
                Log.d("Rewarded","onRewarded ");
                switch (operationAfterAds){
                    case  100 :
                        poster.setDownloadas("1");
                        break;
                    case  200 :
                        poster.setPlayas("1");
                        break;
                    case 300 :
                        if (current_position_play != -1 ){
                            playSources.get(current_position_play).setPremium("1");
                            showSourcesPlayDialog();
                        }
                        break;
                    case 400:
                        if (current_position_download != -1 ){
                            downloadableList.get(current_position_download).setPremium("1");
                            showSourcesDownloadDialog();
                        }
                        break;
                }
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                Log.d("Rewarded","onRewardedVideoAdLeftApplication ");

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Log.d("Rewarded","onRewardedVideoAdFailedToLoad "+ i);
            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });

    }
    private void initBuy() {
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);


        if(!BillingProcessor.isIabServiceAvailable(this)) {
            //  showToast("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        bp = new BillingProcessor(this, Global.MERCHANT_KEY, MERCHANT_ID, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
                //  showToast("onProductPurchased: " + productId);
                Intent intent= new Intent(MovieActivity.this,SplashActivity.class);
                startActivity(intent);
                finish();
                updateTextViews();
            }
            @Override
            public void onBillingError(int errorCode, @Nullable Throwable error) {
                // showToast("onBillingError: " + Integer.toString(errorCode));
            }
            @Override
            public void onBillingInitialized() {
                //  showToast("onBillingInitialized");
                readyToPurchase = true;
                updateTextViews();
            }
            @Override
            public void onPurchaseHistoryRestored() {
                // showToast("onPurchaseHistoryRestored");
                for(String sku : bp.listOwnedProducts())
                    Log.d(LOG_TAG, "Owned Managed Product: " + sku);
                for(String sku : bp.listOwnedSubscriptions())
                    Log.d(LOG_TAG, "Owned Subscription: " + sku);
                updateTextViews();
            }
        });
        bp.loadOwnedPurchasesFromGoogle();
    }

    private void updateTextViews() {
        PrefManager prf= new PrefManager(getApplicationContext());
        bp.loadOwnedPurchasesFromGoogle();

    }
    public Bundle getPurchases(){
        if (!bp.isInitialized()) {


            //  Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
            return null;
        }
        try{
            // Toast.makeText(this, "good", Toast.LENGTH_SHORT).show();

            return  mService.getPurchases(Constants.GOOGLE_API_VERSION, getApplicationContext().getPackageName(), Constants.PRODUCT_TYPE_SUBSCRIPTION, null);
        }catch (Exception e) {
            //  Toast.makeText(this, "ex", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
        return null;
    }

    private void setPlayableList() {
        for (int i = 0; i < poster.getSources().size(); i++) {
            if (poster.getSources().get(i).getKind().equals("both") || poster.getSources().get(i).getKind().equals("play")){
                playSources.add(poster.getSources().get(i));
            }
        }

    }
    private void setDownloadableList() {
        for (int i = 0; i < poster.getSources().size(); i++) {
            if (poster.getSources().get(i).getKind().equals("both") || poster.getSources().get(i).getKind().equals("download")){
                if (!poster.getSources().get(i).getType().equals("youtube") && !poster.getSources().get(i).getType().equals("embed")){
                    downloadableList.add(poster.getSources().get(i));
                }
            }
        }
    }

    private void getRandomMovies() {
        String genres_list = "";
        for (int i = 0; i < poster.getGenres().size(); i++) {
            if (poster.getGenres().size()-1 == i){
                genres_list+=poster.getGenres().get(i).getId();
            }else{
                genres_list+=poster.getGenres().get(i).getId()+",";
            }
        }
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);

        Call<List<Poster>> call = service.getRandomMoivies(genres_list);
        call.enqueue(new Callback<List<Poster>>() {
            @Override
            public void onResponse(Call<List<Poster>> call, Response<List<Poster>> response) {
                if (response.isSuccessful()){
                    if (response.body().size()>0) {
                        List<Poster> posterList = new ArrayList<>();
                        for (int i = 0; i < response.body().size(); i++) {
                            if (response.body().get(i).getId() != poster.getId())
                                posterList.add(response.body().get(i));
                        }
                        linearLayoutManagerMoreMovies = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                        posterAdapter  = new PosterAdapter(posterList, MovieActivity.this);
                        recycle_view_activity_activity_movie_more_movies.setHasFixedSize(true);
                        recycle_view_activity_activity_movie_more_movies.setAdapter(posterAdapter);
                        recycle_view_activity_activity_movie_more_movies.setLayoutManager(linearLayoutManagerMoreMovies);
                        linear_layout_activity_movie_more_movies.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Poster>> call, Throwable t) {
            }
        });
    }
    private void getPosterCastings() {
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Actor>> call = service.getRolesByPoster(poster.getId());
        call.enqueue(new Callback<List<Actor>>() {
            @Override
            public void onResponse(Call<List<Actor>> call, Response<List<Actor>> response) {
                if (response.isSuccessful()){
                    if (response.body().size()>0) {
                        linearLayoutManagerCast = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                        actorAdapter = new ActorAdapter(response.body(), MovieActivity.this);
                        recycle_view_activity_activity_movie_cast.setHasFixedSize(true);
                        recycle_view_activity_activity_movie_cast.setAdapter(actorAdapter);
                        recycle_view_activity_activity_movie_cast.setLayoutManager(linearLayoutManagerCast);
                        linear_layout_activity_movie_cast.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Actor>> call, Throwable t) {
            }
        });
    }

    private void getMovie() {
        poster = getIntent().getParcelableExtra("poster");
        from = getIntent().getStringExtra("from");
    }
    private void setMovie() {
        Picasso.with(this).load((poster.getCover()!=null ? poster.getCover() : poster.getImage())).into(image_view_activity_movie_cover);
        final com.squareup.picasso.Target target = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                BlurImage.with(getApplicationContext()).load(bitmap).intensity(25).Async(true).into(image_view_activity_movie_background);
            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) { }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) { }
        };
        Picasso.with(getApplicationContext()).load(poster.getImage()).into(target);
        image_view_activity_movie_background.setTag(target);

        ViewCompat.setTransitionName(image_view_activity_movie_cover, "imageMain");
        text_view_activity_movie_title.setText(poster.getTitle());
        text_view_activity_movie_sub_title.setText(poster.getTitle());
        text_view_activity_movie_description.setText(poster.getDescription());
        if (poster.getYear()!=null){
            if (!poster.getYear().isEmpty()){
                text_view_activity_movie_year.setText(poster.getYear());
                text_view_activity_movie_year.setVisibility(View.VISIBLE);
            }
        }

        if (poster.getClassification()!=null){
            if (!poster.getClassification().isEmpty()){
                text_view_activity_movie_classification.setText(poster.getClassification());
                text_view_activity_movie_classification.setVisibility(View.VISIBLE);
            }
        }

        if (poster.getDuration()!=null){
            if (!poster.getDuration().isEmpty()){
                text_view_activity_movie_duration.setText(poster.getDuration());
                text_view_activity_movie_duration.setVisibility(View.VISIBLE);
            }
        }
        if (poster.getImdb()!=null){
            if (!poster.getImdb().isEmpty()){
                text_view_activity_movie_imdb_rating.setText(poster.getImdb());
                linear_layout_activity_movie_imdb.setVisibility(View.VISIBLE);
            }
        }


        rating_bar_activity_movie_rating.setRating(poster.getRating());
        linear_layout_activity_movie_rating.setVisibility(poster.getRating()==0 ? View.GONE:View.VISIBLE);

        this.linearLayoutManagerGenre=  new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        this.genreAdapter =new GenreAdapter(poster.getGenres(),this);
        recycle_view_activity_movie_genres.setHasFixedSize(true);
        recycle_view_activity_movie_genres.setAdapter(genreAdapter);
        recycle_view_activity_movie_genres.setLayoutManager(linearLayoutManagerGenre);

        if (poster.getTrailer()!=null){
            linear_layout_movie_activity_trailer.setVisibility(View.VISIBLE);
        }
        if (poster.getComment()){
            floating_action_button_activity_movie_comment.setVisibility(View.VISIBLE);
        }else{
            floating_action_button_activity_movie_comment.setVisibility(View.GONE);
        }

    }

    private void initAction() {
        linear_layout_movie_activity_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
        linear_layout_activity_movie_my_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMyList();
            }
        });
        linear_layout_movie_activity_trailer_clicked.setOnClickListener(v-> {
            playTrailer();
        });
        floating_action_button_activity_movie_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSUBSCRIBED()){
                    showSourcesPlayDialog();
                }else{
                    if (poster.getPlayas().equals("2")){
                        showDialog(false);
                    }else if(poster.getPlayas().equals("3") ){
                        operationAfterAds = 200;
                        showDialog(true);
                    }else{
                        showSourcesPlayDialog();
                    }
                }
            }
        });
        linear_layout_movie_activity_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateDialog();
            }
        });
        linear_layout_movie_activity_download.setOnClickListener(v->{
            if (checkSUBSCRIBED()){
                showSourcesDownloadDialog();
            }else{
                if (poster.getDownloadas().equals("2")){
                    showDialog(false);
                }else if(poster.getDownloadas().equals("3") ){
                    showDialog(true);
                    operationAfterAds = 100;
                }else{
                    showSourcesDownloadDialog();
                }
            }
        });
        floating_action_button_activity_movie_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCommentsDialog();
            }
        });
    }
    public boolean checkSUBSCRIBED(){
        PrefManager prefManager= new PrefManager(getApplicationContext());
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE") && !prefManager.getString("NEW_SUBSCRIBE_ENABLED").equals("TRUE")) {
            return false;
        }
        return true;
    }
    public void playSource(int position){
        addView();

        if (playSources.get(position).getType().equals("youtube")){
            Intent intent = new Intent(MovieActivity.this,YoutubeActivity.class);
            intent.putExtra("url",playSources.get(position).getUrl());
            startActivity(intent);
            return;
        }
        if (playSources.get(position).getType().equals("embed")){
            Intent intent = new Intent(MovieActivity.this,EmbedActivity.class);
            intent.putExtra("url",playSources.get(position).getUrl());
            startActivity(intent);
            return;
        }
        if (mCastSession == null) {
            mCastSession = mSessionManager.getCurrentCastSession();
        }
        if (mCastSession != null) {
            loadSubtitles(position);
        } else {
            Intent intent = new Intent(MovieActivity.this,PlayerActivity.class);
            intent.putExtra("id",poster.getId());
            intent.putExtra("url",playSources.get(position).getUrl());
            intent.putExtra("type",playSources.get(position).getType());
            intent.putExtra("image",poster.getImage());
            intent.putExtra("kind","movie");
            intent.putExtra("title",poster.getTitle());
            intent.putExtra("subtitle",poster.getTitle() + "("+poster.getYear()+")");
            startActivity(intent);
        }
    }

    public void playTrailer(){
        if (poster.getTrailer().getType().equals("youtube")){
            Intent intent = new Intent(MovieActivity.this,YoutubeActivity.class);
            intent.putExtra("url",poster.getTrailer().getUrl());
            startActivity(intent);
            return;
        }
        if (poster.getTrailer().getType().equals("embed")){
            Intent intent = new Intent(MovieActivity.this,EmbedActivity.class);
            intent.putExtra("url",poster.getTrailer().getUrl());
            startActivity(intent);
            return;
        }
        if (mCastSession == null) {
            mCastSession = mSessionManager.getCurrentCastSession();
        }
        if (mCastSession != null) {
            loadRemoteMedia(0, true);
        } else {
            Intent intent = new Intent(MovieActivity.this,PlayerActivity.class);
            intent.putExtra("url",poster.getTrailer().getUrl());
            intent.putExtra("type",poster.getTrailer().getType());
            intent.putExtra("image",poster.getImage());
            intent.putExtra("title",poster.getTitle());
            intent.putExtra("subtitle",poster.getTitle() + " Trailer");
            startActivity(intent);
        }
    }
    public void rateDialog(){
        Dialog rateDialog = new Dialog(this,
                R.style.Theme_Dialog);
        rateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        rateDialog.setCancelable(true);
        rateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = rateDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);

        rateDialog.setContentView(R.layout.dialog_rate);
        final AppCompatRatingBar AppCompatRatingBar_dialog_rating_app=(AppCompatRatingBar) rateDialog.findViewById(R.id.AppCompatRatingBar_dialog_rating_app);
        final Button buttun_send=(Button) rateDialog.findViewById(R.id.buttun_send);
        final Button button_cancel=(Button) rateDialog.findViewById(R.id.button_cancel);
        final TextView text_view_rate_title=(TextView) rateDialog.findViewById(R.id.text_view_rate_title);
        text_view_rate_title.setText(getResources().getString(R.string.rate_this_movie));
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateDialog.dismiss();
            }
        });
        buttun_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefManager prf = new PrefManager(getApplicationContext());
                if (prf.getString("LOGGED").toString().equals("TRUE")) {
                    Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
                    String   key_user=  prf.getString("TOKEN_USER");
                    Retrofit retrofit = apiClient.getClient();
                    apiRest service = retrofit.create(apiRest.class);
                    Call<ApiResponse> call = service.addPosterRate(id_user+"",key_user, poster.getId(), AppCompatRatingBar_dialog_rating_app.getRating());
                    call.enqueue(new retrofit2.Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful()) {
                                if (response.body().getCode() == 200) {
                                    Toasty.success(MovieActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    if (response.body().getValues().size()>0){
                                        if (response.body().getValues().get(0).getName().equals("rate") ){
                                            linear_layout_activity_movie_rating.setVisibility(View.VISIBLE);
                                            rating_bar_activity_movie_rating.setRating(Float.parseFloat(response.body().getValues().get(0).getValue()));
                                        }
                                    }
                                } else {
                                    Toasty.error(MovieActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            rateDialog.dismiss();
                        }
                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            rateDialog.dismiss();
                        }
                    });
                } else {
                    rateDialog.dismiss();
                    Intent intent = new Intent(MovieActivity.this,LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                }
            }
        });
        rateDialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    rateDialog.dismiss();
                }
                return true;
            }
        });
        rateDialog.show();

    }
    public void showCommentsDialog(){

        Dialog dialog= new Dialog(this,
                R.style.Theme_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        dialog.setContentView(R.layout.dialog_comment);
        TextView text_view_comment_dialog_count=dialog.findViewById(R.id.text_view_comment_dialog_count);
        ImageView image_view_comment_dialog_close=dialog.findViewById(R.id.image_view_comment_dialog_close);
        ImageView image_view_comment_dialog_empty=dialog.findViewById(R.id.image_view_comment_dialog_empty);
        ImageView image_view_comment_dialog_add_comment=dialog.findViewById(R.id.image_view_comment_dialog_add_comment);
        ProgressBar progress_bar_comment_dialog_comments=dialog.findViewById(R.id.progress_bar_comment_dialog_comments);
        ProgressBar progress_bar_comment_dialog_add_comment=dialog.findViewById(R.id.progress_bar_comment_dialog_add_comment);
        EditText edit_text_comment_dialog_add_comment=dialog.findViewById(R.id.edit_text_comment_dialog_add_comment);
        RecyclerView recycler_view_comment_dialog_comments=dialog.findViewById(R.id.recycler_view_comment_dialog_comments);

        image_view_comment_dialog_empty.setVisibility(View.GONE);
        recycler_view_comment_dialog_comments.setVisibility(View.GONE);
        progress_bar_comment_dialog_comments.setVisibility(View.VISIBLE);
        commentAdapter = new CommentAdapter(commentList, MovieActivity.this);
        linearLayoutManagerComments = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recycler_view_comment_dialog_comments.setHasFixedSize(true);
        recycler_view_comment_dialog_comments.setAdapter(commentAdapter);
        recycler_view_comment_dialog_comments.setLayoutManager(linearLayoutManagerComments);

        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Comment>> call = service.getCommentsByPoster(poster.getId());
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful()){
                    if (response.body().size()>0) {
                        commentList.clear();
                        for (int i = 0; i < response.body().size(); i++)
                            commentList.add(response.body().get(i));

                        commentAdapter.notifyDataSetChanged();

                        text_view_comment_dialog_count.setText(commentList.size()+" Comments");
                        image_view_comment_dialog_empty.setVisibility(View.GONE);
                        recycler_view_comment_dialog_comments.setVisibility(View.VISIBLE);
                        progress_bar_comment_dialog_comments.setVisibility(View.GONE);
                        recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                        recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                    }else{
                        image_view_comment_dialog_empty.setVisibility(View.VISIBLE);
                        recycler_view_comment_dialog_comments.setVisibility(View.GONE);
                        progress_bar_comment_dialog_comments.setVisibility(View.GONE);
                    }
                }else{
                    image_view_comment_dialog_empty.setVisibility(View.VISIBLE);
                    recycler_view_comment_dialog_comments.setVisibility(View.GONE);
                    progress_bar_comment_dialog_comments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                image_view_comment_dialog_empty.setVisibility(View.VISIBLE);
                recycler_view_comment_dialog_comments.setVisibility(View.GONE);
                progress_bar_comment_dialog_comments.setVisibility(View.GONE);
            }
        });

        image_view_comment_dialog_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_text_comment_dialog_add_comment.getText().length()>0){
                    PrefManager prf= new PrefManager(MovieActivity.this.getApplicationContext());
                    if (prf.getString("LOGGED").toString().equals("TRUE")){
                        Integer id_user=  Integer.parseInt(prf.getString("ID_USER"));
                        String   key_user=  prf.getString("TOKEN_USER");
                        byte[] data = new byte[0];
                        String comment_final ="";
                        try {
                            data = edit_text_comment_dialog_add_comment.getText().toString().getBytes("UTF-8");
                            comment_final = Base64.encodeToString(data, Base64.DEFAULT);
                        } catch (UnsupportedEncodingException e) {
                            comment_final = edit_text_comment_dialog_add_comment.getText().toString();
                            e.printStackTrace();
                        }
                        progress_bar_comment_dialog_add_comment.setVisibility(View.VISIBLE);
                        image_view_comment_dialog_add_comment.setVisibility(View.GONE);
                        Retrofit retrofit = apiClient.getClient();
                        apiRest service = retrofit.create(apiRest.class);
                        Call<ApiResponse> call = service.addPosterComment(id_user+"",key_user,poster.getId(),comment_final);
                        call.enqueue(new Callback<ApiResponse>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if (response.isSuccessful()){
                                    if (response.body().getCode()==200){
                                        recycler_view_comment_dialog_comments.setVisibility(View.VISIBLE);
                                        image_view_comment_dialog_empty.setVisibility(View.GONE);
                                        Toasty.success(MovieActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                        edit_text_comment_dialog_add_comment.setText("");
                                        String id="";
                                        String content="";
                                        String user="";
                                        String image="";

                                        for (int i=0;i<response.body().getValues().size();i++){
                                            if (response.body().getValues().get(i).getName().equals("id")){
                                                id=response.body().getValues().get(i).getValue();
                                            }
                                            if (response.body().getValues().get(i).getName().equals("content")){
                                                content=response.body().getValues().get(i).getValue();
                                            }
                                            if (response.body().getValues().get(i).getName().equals("user")){
                                                user=response.body().getValues().get(i).getValue();
                                            }
                                            if (response.body().getValues().get(i).getName().equals("image")){
                                                image=response.body().getValues().get(i).getValue();
                                            }
                                        }
                                        Comment comment= new Comment();
                                        comment.setId(Integer.parseInt(id));
                                        comment.setUser(user);
                                        comment.setContent(content);
                                        comment.setImage(image);
                                        comment.setEnabled(true);
                                        comment.setCreated(getResources().getString(R.string.now_time));
                                        commentList.add(comment);
                                        commentAdapter.notifyDataSetChanged();
                                        text_view_comment_dialog_count.setText(commentList.size()+" Comments");

                                    }else{
                                        Toasty.error(MovieActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                                recycler_view_comment_dialog_comments.scrollToPosition(recycler_view_comment_dialog_comments.getAdapter().getItemCount()-1);
                                commentAdapter.notifyDataSetChanged();
                                progress_bar_comment_dialog_add_comment.setVisibility(View.GONE);
                                image_view_comment_dialog_add_comment.setVisibility(View.VISIBLE);
                            }
                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                progress_bar_comment_dialog_add_comment.setVisibility(View.GONE);
                                image_view_comment_dialog_add_comment.setVisibility(View.VISIBLE);
                            }
                        });
                    }else{
                        Intent intent = new Intent(MovieActivity.this,LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    }
                }
            }
        });
        image_view_comment_dialog_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showSourcesDownloadDialog(){
        if (ContextCompat.checkSelfPermission(MovieActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MovieActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            return;
        }
        if (downloadableList.size()==0){
            Toasty.warning(getApplicationContext(),getResources().getString(R.string.no_source_available),Toast.LENGTH_LONG).show();
            return;
        }
        if (downloadableList.size()==1){
            if (checkSUBSCRIBED()) {
                if (!downloadableList.get(0).getExternal()) {
                    DownloadSource(downloadableList.get(0));
                } else {
                    openDownloadLink(0);
                }
            }else {

                if (downloadableList.get(0).getPremium().equals("2")) {
                    showDialog(false);
                } else if (downloadableList.get(0).getPremium().equals("3")) {
                    operationAfterAds = 400;
                    current_position_download = 0;
                    showDialog(true);
                } else {
                    if (!downloadableList.get(0).getExternal()) {
                        DownloadSource(downloadableList.get(0));
                    } else {
                        openDownloadLink(0);
                    }
                }
            }

            return;
        }

        download_source_dialog= new Dialog(this,
                R.style.Theme_Dialog);
        download_source_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        download_source_dialog.setCancelable(true);
        download_source_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = download_source_dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        download_source_dialog.setContentView(R.layout.dialog_download);

        RelativeLayout relative_layout_dialog_source_close =  download_source_dialog.findViewById(R.id.relative_layout_dialog_source_close);
        RecyclerView recycle_view_activity_dialog_sources =  download_source_dialog.findViewById(R.id.recycle_view_activity_dialog_sources);
        this.linearLayoutManagerDownloadSources =  new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        DownloadsAdapter sourceDownloadAdapter =new DownloadsAdapter();
        recycle_view_activity_dialog_sources.setHasFixedSize(true);
        recycle_view_activity_dialog_sources.setAdapter(sourceDownloadAdapter);
        recycle_view_activity_dialog_sources.setLayoutManager(linearLayoutManagerDownloadSources);

        relative_layout_dialog_source_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download_source_dialog.dismiss();
            }
        });
        download_source_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    download_source_dialog.dismiss();
                }
                return true;
            }
        });
        download_source_dialog.show();


    }
    public void showSourcesPlayDialog(){
        if (playSources.size()==0){
            Toasty.warning(getApplicationContext(),getResources().getString(R.string.no_source_available),Toast.LENGTH_LONG).show();
            return;
        }
        if (playSources.size()==1) {
            if (checkSUBSCRIBED()) {
                if (playSources.get(0).getExternal()) {
                    openLink(0);
                } else {
                    playSource(0);
                }
            } else {
                if (playSources.get(0).getPremium().equals("2")) {
                    showDialog(false);
                } else if (playSources.get(0).getPremium().equals("3")) {
                    operationAfterAds = 300;
                    current_position_play = 0;
                    showDialog(true);
                } else {
                    if (playSources.get(0).getExternal()) {
                        openLink(0);
                    } else {
                        playSource(0);
                    }
                }
            }
            return;
        }

        play_source_dialog= new Dialog(this,
                R.style.Theme_Dialog);
        play_source_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        play_source_dialog.setCancelable(true);
        play_source_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = play_source_dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        play_source_dialog.setContentView(R.layout.dialog_sources);

        RelativeLayout relative_layout_dialog_source_close =  play_source_dialog.findViewById(R.id.relative_layout_dialog_source_close);
        RecyclerView recycle_view_activity_dialog_sources =  play_source_dialog.findViewById(R.id.recycle_view_activity_dialog_sources);
        this.linearLayoutManagerSources =  new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        SourceAdapter sourceAdapter =new SourceAdapter();
        recycle_view_activity_dialog_sources.setHasFixedSize(true);
        recycle_view_activity_dialog_sources.setAdapter(sourceAdapter);
        recycle_view_activity_dialog_sources.setLayoutManager(linearLayoutManagerSources);

        relative_layout_dialog_source_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play_source_dialog.dismiss();
            }
        });
        play_source_dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    play_source_dialog.dismiss();
                }
                return true;
            }
        });
        play_source_dialog.show();


    }
    private void initView() {
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        this.text_view_activity_movie_imdb_rating =  (TextView) findViewById(R.id.text_view_activity_movie_imdb_rating);
        this.linear_layout_activity_movie_rating =  (LinearLayout) findViewById(R.id.linear_layout_activity_movie_rating);
        this.linear_layout_activity_movie_imdb =  (LinearLayout) findViewById(R.id.linear_layout_activity_movie_imdb);
        this.linear_layout_movie_activity_share =  (LinearLayout) findViewById(R.id.linear_layout_movie_activity_share);
        this.floating_action_button_activity_movie_comment =  (FloatingActionButton) findViewById(R.id.floating_action_button_activity_movie_comment);
        this.relative_layout_subtitles_loading =  (RelativeLayout) findViewById(R.id.relative_layout_subtitles_loading);
        this.floating_action_button_activity_movie_play =  (FloatingActionButton) findViewById(R.id.floating_action_button_activity_movie_play);
        this.image_view_activity_movie_background =  (ImageView) findViewById(R.id.image_view_activity_movie_background);
        this.image_view_activity_movie_cover =  (ImageView) findViewById(R.id.image_view_activity_movie_cover);
        this.text_view_activity_movie_title =  (TextView) findViewById(R.id.text_view_activity_movie_title);
        this.text_view_activity_movie_sub_title =  (TextView) findViewById(R.id.text_view_activity_movie_sub_title);
        this.text_view_activity_movie_description =  (TextView) findViewById(R.id.text_view_activity_movie_description);
        this.text_view_activity_movie_duration =  (TextView) findViewById(R.id.text_view_activity_movie_duration);
        this.text_view_activity_movie_year =  (TextView) findViewById(R.id.text_view_activity_movie_year);
        this.text_view_activity_movie_classification =  (TextView) findViewById(R.id.text_view_activity_movie_classification);
        this.rating_bar_activity_movie_rating =  (RatingBar) findViewById(R.id.rating_bar_activity_movie_rating);
        this.recycle_view_activity_movie_genres =  (RecyclerView) findViewById(R.id.recycle_view_activity_movie_genres);
        this.recycle_view_activity_activity_movie_cast =  (RecyclerView) findViewById(R.id.recycle_view_activity_activity_movie_cast);
        this.recycle_view_activity_activity_movie_more_movies =  (RecyclerView) findViewById(R.id.recycle_view_activity_activity_movie_more_movies);
        this.linear_layout_activity_movie_cast =  (LinearLayout) findViewById(R.id.linear_layout_activity_movie_cast);
        this.linear_layout_movie_activity_trailer =  (LinearLayout) findViewById(R.id.linear_layout_movie_activity_trailer);
        this.linear_layout_movie_activity_trailer_clicked =  (LinearLayout) findViewById(R.id.linear_layout_movie_activity_trailer_clicked);
        this.linear_layout_movie_activity_rate =  (LinearLayout) findViewById(R.id.linear_layout_movie_activity_rate);
        this.linear_layout_activity_movie_more_movies =  (LinearLayout) findViewById(R.id.linear_layout_activity_movie_more_movies);
        this.linear_layout_activity_movie_my_list =  (LinearLayout) findViewById(R.id.linear_layout_activity_movie_my_list);
        this.linear_layout_movie_activity_download =  (LinearLayout) findViewById(R.id.linear_layout_movie_activity_download);
        this.image_view_activity_movie_my_list =  (ImageView) findViewById(R.id.image_view_activity_movie_my_list);
        this.progress_bar_activity_movie_my_list =  (ProgressBar) findViewById(R.id.progress_bar_activity_movie_my_list);
    }
    private void loadRemoteMedia(int position, boolean autoPlay) {
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            mCastSession = mSessionManager.getCurrentCastSession();
            mSessionManager.addSessionManagerListener(mSessionManagerListener);
            if (mCastSession == null) {
                mCastSession = mSessionManager.getCurrentCastSession();
            }
            playTrailer();
            return;
        }

        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                Log.d(TAG,"onStatusUpdated");
                if (remoteMediaClient.getMediaStatus() != null) {

                }
            }
        });
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(getTrailerMediaInfos())
                .setAutoplay(autoPlay).build());
    }
    private void loadRemoteMediaSource(int position, boolean autoPlay) {
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            mCastSession = mSessionManager.getCurrentCastSession();
            mSessionManager.addSessionManagerListener(mSessionManagerListener);
            if (mCastSession == null) {
                mCastSession = mSessionManager.getCurrentCastSession();
            }
            tryed++;
            playSource(position);

            return;
        }

        remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
            @Override
            public void onStatusUpdated() {
                Log.d(TAG,"onStatusUpdated");
                if (remoteMediaClient.getMediaStatus() != null) {

                }
            }
        });
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(getSourceMediaInfos(position))
                .setAutoplay(autoPlay).build());
    }



    @Override
    protected void onResume() {
        super.onResume();
        mCastSession = mSessionManager.getCurrentCastSession();
        mSessionManager.addSessionManagerListener(mSessionManagerListener);
    }

    @Override
    protected void onPause() {
        mSessionManager.removeSessionManagerListener(mSessionManagerListener);
        mCastSession = null;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_cast, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(),
                menu,
                R.id.media_route_menu_item);
        return true;
    }




    private MediaInfo getSourceMediaInfos(int position) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, poster.getTitle());
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, poster.getTitle());

        movieMetadata.addImage(new WebImage(Uri.parse(poster.getImage())));
        movieMetadata.addImage(new WebImage(Uri.parse(poster.getImage())));
        List<MediaTrack> tracks =  new ArrayList<>();

        for (int i = 0; i < subtitlesForCast.size(); i++) {
            tracks.add(buildTrack(i+1,"text","captions",subtitlesForCast.get(i).getUrl(),subtitlesForCast.get(i).getLanguage(),"en-US"));
        }

        MediaInfo mediaInfo = new MediaInfo.Builder(playSources.get(position).getUrl())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(movieMetadata)
                .setMediaTracks(tracks)
                .build();
        return mediaInfo;
    }
    private MediaInfo getTrailerMediaInfos() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, poster.getTitle());
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, poster.getTitle() +" Trailer");

        movieMetadata.addImage(new WebImage(Uri.parse(poster.getImage())));
        movieMetadata.addImage(new WebImage(Uri.parse(poster.getImage())));


        List<MediaTrack> tracks =  new ArrayList<>();
        MediaInfo mediaInfo = new MediaInfo.Builder(poster.getTrailer().getUrl())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(movieMetadata)
                .setMediaTracks(tracks)
                .build();

        return mediaInfo;
    }
    private static MediaTrack buildTrack(long id, String type, String subType, String contentId,
                                         String name, String language) {
        int trackType = MediaTrack.TYPE_UNKNOWN;
        if ("text".equals(type)) {
            trackType = MediaTrack.TYPE_TEXT;
        } else if ("video".equals(type)) {
            trackType = MediaTrack.TYPE_VIDEO;
        } else if ("audio".equals(type)) {
            trackType = MediaTrack.TYPE_AUDIO;
        }

        int trackSubType = MediaTrack.SUBTYPE_NONE;
        if (subType != null) {
            if ("captions".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_CAPTIONS;
            } else if ("subtitle".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_SUBTITLES;
            }
        }

        return new MediaTrack.Builder(id, trackType)
                .setContentType(MediaFormat.MIMETYPE_TEXT_VTT)
                .setName(name)
                .setSubtype(trackSubType)
                .setContentId(contentId)
                .setLanguage(language).build();
    }
    @Override
    public void onBackPressed(){
        if (from!=null){
            Intent intent =  new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            super.onBackPressed();
        }
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (from!=null){
            Intent intent =  new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    public class SourceAdapter extends  RecyclerView.Adapter<SourceAdapter.SourceHolder>{


        @Override
        public SourceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_play,parent, false);
            SourceHolder mh = new SourceAdapter.SourceHolder(v);
            return mh;
        }
        @Override
        public void onBindViewHolder(SourceAdapter.SourceHolder holder, final int position) {
            if (playSources.get(position).getTitle() == null){
                holder.text_view_item_source_type.setText(playSources.get(position).getType());
            }else{
                holder.text_view_item_source_type.setText(playSources.get(position).getTitle());
            }
            holder.image_view_item_source_type_link.setVisibility(View.GONE);
            holder.image_view_item_source_type_play.setVisibility(View.VISIBLE);
            if (playSources.get(position).getExternal() != null) {
                if (playSources.get(position).getExternal()){
                    holder.image_view_item_source_type_link.setVisibility(View.VISIBLE);
                    holder.image_view_item_source_type_play.setVisibility(View.GONE);
                }
            }
            holder.image_view_item_source_premium.setVisibility(View.GONE);
            if (playSources.get(position).getPremium() != null) {
                if (!playSources.get(position).getPremium().equals("1")){
                    holder.image_view_item_source_premium.setVisibility(View.VISIBLE);
                }
            }

            holder.text_view_item_source_size.setVisibility(View.GONE);
            if (playSources.get(position).getSize() != null) {
                if (playSources.get(position).getSize().length()>0){
                    holder.text_view_item_source_size.setVisibility(View.VISIBLE);
                    holder.text_view_item_source_size.setText(playSources.get(position).getSize());
                }
            }

            if (playSources.get(position).getQuality() != null) {
                if (playSources.get(position).getQuality().length() >0 ){
                    holder.text_view_item_source_quality.setVisibility(View.VISIBLE);
                    holder.text_view_item_source_quality.setText(playSources.get(position).getQuality());

                }else{
                    holder.text_view_item_source_quality.setVisibility(View.GONE);
                }
            }else{
                holder.text_view_item_source_quality.setVisibility(View.GONE);
            }


            switch (playSources.get(position).getType()){
                case "mov":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mov_file));
                    break;
                case "mkv":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mkv_file));
                    break;
                case "webm":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_webm_file));
                    break;
                case "mp4":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mp4_file));
                    break;
                case "m3u8":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_m3u_file));
                    break;
                case "youtube":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_youtube));
                    break;
                case "embed":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_embed_file));
                    break;
            }
            holder.image_view_item_source_type_play.setOnClickListener(v-> {
                if (checkSUBSCRIBED()) {
                    playSource(position);
                }else{
                    if (playSources.get(position).getPremium().equals("2")) {
                        showDialog(false);
                    } else if (playSources.get(position).getPremium().equals("3")) {
                        operationAfterAds = 300;
                        current_position_play = position;
                        showDialog(true);
                    } else {
                        playSource(position);
                    }
                }
                play_source_dialog.dismiss();
            });
            holder.image_view_item_source_type_link.setOnClickListener( v -> {
                if (checkSUBSCRIBED()) {
                    openLink(position);
                }else{
                    if (playSources.get(position).getPremium().equals("2")){
                        showDialog(false);
                    }else if(playSources.get(position).getPremium().equals("3") ){
                        operationAfterAds = 300;
                        current_position_play=  position;
                        showDialog(true);
                    }else{
                        openLink(position);
                    }
                }
                play_source_dialog.dismiss();

            });

        }
        @Override
        public int getItemCount() {
            return playSources.size();
        }
        public class SourceHolder extends RecyclerView.ViewHolder {
            private final ImageView image_view_item_source_type_link;
            private final ImageView image_view_item_source_premium;
            private final ImageView image_view_item_source_type_play;
            private final ImageView image_view_item_source_type_image;
            private final TextView text_view_item_source_size;
            private final TextView text_view_item_source_type;
            private final TextView text_view_item_source_quality;

            public SourceHolder(View itemView) {
                super(itemView);
                this.text_view_item_source_quality =  (TextView) itemView.findViewById(R.id.text_view_item_source_quality);
                this.text_view_item_source_type =  (TextView) itemView.findViewById(R.id.text_view_item_source_type);
                this.text_view_item_source_size =  (TextView) itemView.findViewById(R.id.text_view_item_source_size);
                this.image_view_item_source_type_image =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_image);
                this.image_view_item_source_type_play =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_play);
                this.image_view_item_source_type_link =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_link);
                this.image_view_item_source_premium =  (ImageView) itemView.findViewById(R.id.image_view_item_source_premium);
            }
        }
    }

    public class DownloadsAdapter extends  RecyclerView.Adapter<DownloadsAdapter.DownloadHolder>{

        @Override
        public DownloadHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_download,parent, false);
            DownloadHolder mh = new DownloadHolder(v);
            return mh;
        }
        @Override
        public void onBindViewHolder(DownloadHolder holder, final int position) {

            if (downloadableList.get(position).getTitle() == null){
                holder.text_view_item_source_type.setText(downloadableList.get(position).getType());
            }else{
                holder.text_view_item_source_type.setText(downloadableList.get(position).getTitle());
            }

            holder.image_view_item_source_type_link.setVisibility(View.GONE);
            holder.image_view_item_source_type_download.setVisibility(View.VISIBLE);
            if (downloadableList.get(position).getExternal() != null) {
                if (downloadableList.get(position).getExternal()){
                    holder.image_view_item_source_type_link.setVisibility(View.VISIBLE);
                    holder.image_view_item_source_type_download.setVisibility(View.GONE);
                }
            }
            holder.image_view_item_source_premium.setVisibility(View.GONE);
            if (downloadableList.get(position).getPremium() != null) {
                if (!downloadableList.get(position).getPremium().equals("1")){
                    holder.image_view_item_source_premium.setVisibility(View.VISIBLE);
                }
            }

            holder.text_view_item_source_size.setVisibility(View.GONE);
            if (downloadableList.get(position).getSize() != null) {
                if (downloadableList.get(position).getSize().length()>0){
                    holder.text_view_item_source_size.setVisibility(View.VISIBLE);
                    holder.text_view_item_source_size.setText(downloadableList.get(position).getSize());
                }
            }

            if (downloadableList.get(position).getQuality() != null) {
                if (downloadableList.get(position).getQuality().length() >0 ){
                    holder.text_view_item_source_quality.setVisibility(View.VISIBLE);
                    holder.text_view_item_source_quality.setText(downloadableList.get(position).getQuality());

                }else{
                    holder.text_view_item_source_quality.setVisibility(View.GONE);
                }
            }else{
                holder.text_view_item_source_quality.setVisibility(View.GONE);
            }


            switch (downloadableList.get(position).getType()){
                case "mov":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mov_file));
                    break;
                case "mkv":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mkv_file));
                    break;
                case "webm":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_webm_file));
                    break;
                case "mp4":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_mp4_file));
                    break;
                case "m3u8":
                    holder.image_view_item_source_type_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_m3u_file));
                    break;
            }
            holder.image_view_item_source_type_download.setOnClickListener(v-> {
                if (checkSUBSCRIBED()){
                    DownloadSource(downloadableList.get(position));
                }else {
                    if (downloadableList.get(position).getPremium().equals("2")) {
                        showDialog(false);
                    } else if (downloadableList.get(position).getPremium().equals("3")) {
                        operationAfterAds = 400;
                        current_position_download = position;
                        showDialog(true);
                    } else {
                        DownloadSource(downloadableList.get(position));
                    }
                    download_source_dialog.dismiss();
                }
            });
            holder.image_view_item_source_type_link.setOnClickListener( v -> {
                if (checkSUBSCRIBED()){
                    openDownloadLink(position);
                }else {
                    if (downloadableList.get(position).getPremium().equals("2")) {
                        showDialog(false);
                    } else if (downloadableList.get(position).getPremium().equals("3")) {
                        operationAfterAds = 400;
                        current_position_download = position;
                        showDialog(true);
                    } else {
                        openDownloadLink(position);
                    }
                    download_source_dialog.dismiss();
                }

            });
        }
        @Override
        public int getItemCount() {
            return downloadableList.size();
        }
        public class DownloadHolder extends RecyclerView.ViewHolder {
            private final ImageView image_view_item_source_type_download;
            private final ImageView image_view_item_source_type_image;
            private final ImageView image_view_item_source_type_link;
            private final ImageView image_view_item_source_premium;
            private final TextView text_view_item_source_size;
            private final TextView text_view_item_source_type;
            private final TextView text_view_item_source_quality;

            public DownloadHolder(View itemView) {
                super(itemView);
                this.text_view_item_source_quality =  (TextView) itemView.findViewById(R.id.text_view_item_source_quality);
                this.text_view_item_source_type =  (TextView) itemView.findViewById(R.id.text_view_item_source_type);
                this.text_view_item_source_size =  (TextView) itemView.findViewById(R.id.text_view_item_source_size);
                this.image_view_item_source_type_image =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_image);
                this.image_view_item_source_type_download =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_download);
                this.image_view_item_source_type_link =  (ImageView) itemView.findViewById(R.id.image_view_item_source_type_link);
                this.image_view_item_source_premium =  (ImageView) itemView.findViewById(R.id.image_view_item_source_premium);
            }
        }
    }
    private void loadSubtitles(int position) {
        if (subtitlesForCast.size()>0){
            loadRemoteMediaSource(position, true);
            return;
        }
        relative_layout_subtitles_loading.setVisibility(View.VISIBLE);
        Retrofit retrofit = apiClient.getClient();
        apiRest service = retrofit.create(apiRest.class);
        Call<List<Language>> call = service.getSubtitlesByPoster(poster.getId());
        call.enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, Response<List<Language>> response) {
                relative_layout_subtitles_loading.setVisibility(View.GONE);
                if (response.isSuccessful()){
                    if (response.body().size()>0) {
                        subtitlesForCast.clear();
                        for (int i = 0; i < response.body().size(); i++){
                            for (int l = 0; l < response.body().get(i).getSubtitles().size(); l++) {
                                Subtitle subtitletocast =  response.body().get(i).getSubtitles().get(l);
                                subtitletocast.setLanguage(response.body().get(i).getLanguage());
                                subtitlesForCast.add(subtitletocast);
                            }
                        }
                    }
                }
                loadRemoteMediaSource(position, true);
            }
            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {
                relative_layout_subtitles_loading.setVisibility(View.GONE);
                loadRemoteMediaSource(position, true);
            }
        });
    }
    private void checkFavorite() {

        final PrefManager prefManager = new PrefManager(this);
        if (prefManager.getString("LOGGED").toString().equals("TRUE")){
            Integer id_user=  Integer.parseInt(prefManager.getString("ID_USER"));
            String   key_user=  prefManager.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            progress_bar_activity_movie_my_list.setVisibility(View.VISIBLE);
            linear_layout_activity_movie_my_list.setClickable(false);

            image_view_activity_movie_my_list.setVisibility(View.GONE);
            Call<Integer> call = service.CheckMyList(poster.getId(),id_user,key_user,"poster");
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                    if (response.isSuccessful()){
                        if (response.body() == 200){
                            image_view_activity_movie_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
                        }else{
                            image_view_activity_movie_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));

                        }
                    }
                    progress_bar_activity_movie_my_list.setVisibility(View.GONE);
                    image_view_activity_movie_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_movie_my_list.setClickable(true);

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    progress_bar_activity_movie_my_list.setVisibility(View.GONE);
                    image_view_activity_movie_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_movie_my_list.setClickable(true);


                }
            });
        }
    }

    public void share(){
        String shareBody = poster.getTitle()+"\n\n"+getResources().getString(R.string.get_this_movie_here)+"\n"+ Global.API_URL.replace("api","share")+ poster.getId()+".html";
        //String shareBody = poster.getTitle()+"\n\n"+getResources().getString(R.string.get_this_movie_here)+"\n"+ "https://admin.newhindivideosongs.in/share/"+ poster.getId()+".html";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,  getString(R.string.app_name));
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.app_name)));
        addShare();
    }
    public void addShare(){
        final PrefManager prefManager = new PrefManager(this);
        if (!prefManager.getString(poster.getId()+"_share").equals("true")) {
            prefManager.setString(poster.getId()+"_share", "true");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<Integer> call = service.addPosterShare(poster.getId());
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {

                }
            });
        }
    }
    public void addMyList(){
        final PrefManager prefManager = new PrefManager(this);
        if (prefManager.getString("LOGGED").toString().equals("TRUE")){
            Integer id_user=  Integer.parseInt(prefManager.getString("ID_USER"));
            String   key_user=  prefManager.getString("TOKEN_USER");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            progress_bar_activity_movie_my_list.setVisibility(View.VISIBLE);
            image_view_activity_movie_my_list.setVisibility(View.GONE);
            linear_layout_activity_movie_my_list.setClickable(false);
            Call<Integer> call = service.AddMyList(poster.getId(),id_user,key_user,"poster");
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                    if (response.isSuccessful()){
                        if (response.body() == 200){
                            image_view_activity_movie_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_close));
                            Toasty.info(MovieActivity.this, "This movie has been added to your list", Toast.LENGTH_SHORT).show();
                        }else{
                            image_view_activity_movie_my_list.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
                            Toasty.warning(MovieActivity.this, "This movie has been removed from your list", Toast.LENGTH_SHORT).show();
                        }
                    }
                    progress_bar_activity_movie_my_list.setVisibility(View.GONE);
                    image_view_activity_movie_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_movie_my_list.setClickable(true);

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    progress_bar_activity_movie_my_list.setVisibility(View.GONE);
                    image_view_activity_movie_my_list.setVisibility(View.VISIBLE);
                    linear_layout_activity_movie_my_list.setClickable(true);

                }
            });
        }else{
            Intent intent = new Intent(MovieActivity.this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        }
    }
    public void addView(){
        final PrefManager prefManager = new PrefManager(this);
        if (!prefManager.getString(poster.getId()+"_view").equals("true")) {
            prefManager.setString(poster.getId()+"_view", "true");
            Retrofit retrofit = apiClient.getClient();
            apiRest service = retrofit.create(apiRest.class);
            Call<Integer> call = service.addMovieView(poster.getId());
            call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {

                }
                @Override
                public void onFailure(Call<Integer> call, Throwable t) {

                }
            });
        }
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    public void DownloadSource(Source  source){
        switch (source.getType()){
            case "mov": {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    DownloadQ(source);
                }else {
                    Download(source);
                }
            }
            case "webm": {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    DownloadQ(source);
                }else {
                    Download(source);
                }
            }
            break;
            case "mkv": {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    DownloadQ(source);
                }else {
                    Download(source);
                }
            }
            break;
            case "mp4": {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    DownloadQ(source);
                }else {
                    Download(source);
                }
            }
            break;
            case "m3u8":
                if(!isMyServiceRunning(DownloadService.class)){


                    Intent intent = new Intent(MovieActivity.this, DownloadService.class);
                    intent.putExtra("type","movie");
                    intent.putExtra("url",source.getUrl());
                    intent.putExtra("title",poster.getTitle());
                    intent.putExtra("image",poster.getImage());
                    intent.putExtra("id",source.getId());
                    intent.putExtra("element",poster.getId());

                    if (poster.getDuration()!=null)
                        intent.putExtra("duration",poster.getDuration());
                    else
                        intent.putExtra("duration","");

                    Toasty.info(this,"Download has been started ...",Toast.LENGTH_LONG).show();
                    expandPanel(this);
                    startService(intent);
                }else{
                    Toasty.warning(MovieActivity.this, "Multi-download not supported with m3u8 files. please wait !", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private static void expandPanel(Context _context) {
        try {
            @SuppressLint("WrongConstant") Object sbservice = _context.getSystemService("statusbar");
            Class<?> statusbarManager;
            statusbarManager = Class.forName("android.app.StatusBarManager");
            Method showsb;
            if (Build.VERSION.SDK_INT >= 17) {
                showsb = statusbarManager.getMethod("expandNotificationsPanel");
            } else {
                showsb = statusbarManager.getMethod("expand");
            }
            showsb.invoke(sbservice);
        } catch (ClassNotFoundException _e) {
            _e.printStackTrace();
        } catch (NoSuchMethodException _e) {
            _e.printStackTrace();
        } catch (IllegalArgumentException _e) {
            _e.printStackTrace();
        } catch (IllegalAccessException _e) {
            _e.printStackTrace();
        } catch (InvocationTargetException _e) {
            _e.printStackTrace();
        }
    }
    public void showDialog(Boolean withAds){
        this.dialog = new Dialog(this,
                R.style.Theme_Dialog);



        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        final   PrefManager prf= new PrefManager(getApplicationContext());
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_subscribe);

        RelativeLayout relative_layout_watch_ads=(RelativeLayout) dialog.findViewById(R.id.relative_layout_watch_ads);
        TextView text_view_watch_ads=(TextView) dialog.findViewById(R.id.text_view_watch_ads);

        TextView text_view_policy_2=(TextView) dialog.findViewById(R.id.text_view_policy_2);
        TextView text_view_policy=(TextView) dialog.findViewById(R.id.text_view_policy);
        SpannableString content = new SpannableString(getResources().getString(R.string.subscription_policy));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        text_view_policy.setText(content);
        text_view_policy_2.setText(content);


        text_view_policy.setOnClickListener(view -> {
            startActivity(new Intent(MovieActivity.this,RefundActivity.class));
        });
        text_view_policy_2.setOnClickListener(view -> {
            startActivity(new Intent(MovieActivity.this,RefundActivity.class));
        });


        CardView card_view_gpay=(CardView) dialog.findViewById(R.id.card_view_gpay);
        CardView card_view_paypal=(CardView) dialog.findViewById(R.id.card_view_paypal);
        CardView card_view_cash=(CardView) dialog.findViewById(R.id.card_view_cash);
        CardView card_view_credit_card=(CardView) dialog.findViewById(R.id.card_view_credit_card);
        LinearLayout payment_methode=(LinearLayout) dialog.findViewById(R.id.payment_methode);
        LinearLayout dialog_content=(LinearLayout) dialog.findViewById(R.id.dialog_content);
        RelativeLayout relative_layout_subscibe_back=(RelativeLayout) dialog.findViewById(R.id.relative_layout_subscibe_back);

        RelativeLayout relative_layout_select_method=(RelativeLayout) dialog.findViewById(R.id.relative_layout_select_method);

        if (prf.getString("APP_STRIPE_ENABLED").toString().equals("FALSE")){
            card_view_credit_card.setVisibility(View.GONE);
        }
        if (prf.getString("APP_PAYPAL_ENABLED").toString().equals("FALSE")){
            card_view_paypal.setVisibility(View.GONE);
        }
        if (prf.getString("APP_CASH_ENABLED").toString().equals("FALSE")){
            card_view_cash.setVisibility(View.GONE);
        }
        if (prf.getString("APP_GPLAY_ENABLED").toString().equals("FALSE")){
            card_view_gpay.setVisibility(View.GONE);
        }
        relative_layout_select_method.setOnClickListener(v->{
            if(payment_methode_id.equals("null")) {
                Toasty.error(getApplicationContext(), getResources().getString(R.string.select_payment_method), Toast.LENGTH_LONG).show();
                return;
            }
            switch (payment_methode_id){
                case "gp" :
                    bp.subscribe(MovieActivity.this, Global.SUBSCRIPTION_ID);
                    dialog.dismiss();
                    break;
                default:
                    PrefManager prf1= new PrefManager(getApplicationContext());
                    if (prf1.getString("LOGGED").toString().equals("TRUE")){
                        Intent intent  =  new Intent(getApplicationContext(), PlansActivity.class);
                        intent.putExtra("method",payment_methode_id);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                        dialog.dismiss();

                    }else{
                        Intent intent= new Intent(MovieActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                    }
                    dialog.dismiss();
                    break;
            }
        });

        if (withAds){
            relative_layout_watch_ads.setVisibility(View.VISIBLE);
        }else{
            relative_layout_watch_ads.setVisibility(View.GONE);
        }
        relative_layout_watch_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRewardedVideoAd.isLoaded()){
                    mRewardedVideoAd.show();
                }else{
                    autoDisplay =  true;
                    loadRewardedVideoAd();
                    text_view_watch_ads.setText("SHOW LOADING.");
                }
            }
        });
        TextView text_view_go_pro=(TextView) dialog.findViewById(R.id.text_view_go_pro);
        text_view_go_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment_methode.setVisibility(View.VISIBLE);
                dialog_content.setVisibility(View.GONE);
                relative_layout_subscibe_back.setVisibility(View.VISIBLE);
            }
        });
        relative_layout_subscibe_back.setOnClickListener(v->{
            payment_methode.setVisibility(View.GONE);
            dialog_content.setVisibility(View.VISIBLE);
            relative_layout_subscibe_back.setVisibility(View.GONE);
        });
        card_view_gpay.setOnClickListener(v->{
            payment_methode_id="gp";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
        });
        card_view_paypal.setOnClickListener(v->{
            payment_methode_id="pp";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
        });
        card_view_credit_card.setOnClickListener(v->{
            payment_methode_id="cc";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
        });
        card_view_cash.setOnClickListener(v->{
            payment_methode_id="cash";
            card_view_gpay.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_paypal.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
            card_view_cash.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            card_view_credit_card.setCardBackgroundColor(getResources().getColor(R.color.dark_gray));
        });
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    dialog.dismiss();
                }
                return true;
            }
        });
        dialog.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    showSourcesDownloadDialog();
                }
                return;
            }
        }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConn);

    }
    public void Download(Source source){
        com.virmana.flix.Utils.Log.log("Android P<=");

        File fileName = new File(Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.download_foler) + "/", poster.getTitle().replace(" ", "_").replace(".", "").replaceAll("[^\\.A-Za-z0-9_]", "") + "_" + source.getId() + "." + source.getType());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(source.getUrl()))
                .setTitle(poster.getTitle())// Title of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDescription("Downloading")// Description of the Download Notification
                .setVisibleInDownloadsUi(true)

                .setDestinationUri(Uri.fromFile(fileName));// Uri of the destination file
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        if (!isMyServiceRunning(SerieActivity.class)) {
            startService(new Intent(MovieActivity.this, ToastService.class));
        }
        Toasty.info(this, "Download has been started ...", Toast.LENGTH_LONG).show();
        expandPanel(this);


        DownloadItem downloadItem = new DownloadItem(source.getId(), poster.getTitle(), "movie", Uri.fromFile(fileName).getPath(), poster.getImage(), "", "", poster.getId(), downloadId);
        if (poster.getDuration() != null)
            downloadItem.setDuration(poster.getDuration());
        else
            downloadItem.setDuration("");

        List<DownloadItem> my_downloads_temp = Hawk.get("my_downloads_temp");
        if (my_downloads_temp == null) {
            my_downloads_temp = new ArrayList<>();
        }
        for (int i = 0; i < my_downloads_temp.size(); i++) {
            if (my_downloads_temp.get(i).getId().equals(source.getId())) {
                my_downloads_temp.remove(my_downloads_temp.get(i));
                Hawk.put("my_downloads_temp", my_downloads_temp);
            }
        }
        my_downloads_temp.add(downloadItem);
        Hawk.put("my_downloads_temp", my_downloads_temp);
    }
    public void DownloadQ(Source source){
        com.virmana.flix.Utils.Log.log("Android Q");
        File fileName = new File(Environment.getExternalStorageDirectory() +"/"+ Environment.DIRECTORY_DOWNLOADS+"/", poster.getTitle().replace(" ", "_").replace(".", "").replaceAll("[^\\.A-Za-z0-9_]", "") + "_" + source.getId() + "." + source.getType());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(source.getUrl()))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle(poster.getTitle())// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setVisibleInDownloadsUi(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,poster.getTitle().replace(" ", "_").replace(".", "").replaceAll("[^\\.A-Za-z0-9_]", "") + "_" + source.getId() + "." + source.getType());// Uri of the destination file
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        if (!isMyServiceRunning(SerieActivity.class)) {
            startService(new Intent(MovieActivity.this, ToastService.class));
        }
        Toasty.info(this, "Download has been started ...", Toast.LENGTH_LONG).show();
        expandPanel(this);


        DownloadItem downloadItem = new DownloadItem(source.getId(), poster.getTitle(), "movie", Uri.fromFile(fileName).getPath(), poster.getImage(), "", "", poster.getId(), downloadId);
        if (poster.getDuration() != null)
            downloadItem.setDuration(poster.getDuration());
        else
            downloadItem.setDuration("");

        List<DownloadItem> my_downloads_temp = Hawk.get("my_downloads_temp");
        if (my_downloads_temp == null) {
            my_downloads_temp = new ArrayList<>();
        }
        for (int i = 0; i < my_downloads_temp.size(); i++) {
            if (my_downloads_temp.get(i).getId().equals(source.getId())) {
                my_downloads_temp.remove(my_downloads_temp.get(i));
                Hawk.put("my_downloads_temp", my_downloads_temp);
            }
        }
        my_downloads_temp.add(downloadItem);
        Hawk.put("my_downloads_temp", my_downloads_temp);
    }

    public void openLink(int position){
        String url = playSources.get(position).getUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    public void openDownloadLink(int position){
        String url = downloadableList.get(position).getUrl();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


}
