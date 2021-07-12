package com.virmana.flix.ui.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.virmana.flix.Provider.PrefManager;
import com.virmana.flix.R;
import com.virmana.flix.api.apiClient;
import com.virmana.flix.api.apiRest;
import com.virmana.flix.entity.Channel;
import com.virmana.flix.entity.Poster;
import com.virmana.flix.ui.activities.MovieActivity;
import com.virmana.flix.ui.activities.SerieActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PosterAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Poster> posterList;
    private List<Channel> channelList;
    private Activity activity;
    private Boolean deletable = false;
    private LinearLayoutManager linearLayoutManagerChannelAdapter;
    private ChannelAdapter channelAdapter;
    private Integer position_selected;
    private Integer code_selected;
    private View view_selected;
    private InterstitialAd admobInterstitialAd;
    private com.facebook.ads.InterstitialAd facebookInterstitialAd;

    public PosterAdapter(List<Poster> posterList,List<Channel> channelList, Activity activity) {
        this.posterList = posterList;
        this.channelList = channelList;
        this.activity = activity;
    }
    public PosterAdapter(List<Poster> posterList, Activity activity) {
        this.posterList = posterList;
        this.activity = activity;
    }
    public PosterAdapter(List<Poster> posterList, Activity activity,boolean deletable) {
        this.posterList = posterList;
        this.activity = activity;
        this.deletable = deletable;
    }
    public PosterAdapter(List<Poster> posterList,List<Channel> channelList_, Activity activity,boolean deletable) {
        this.channelList = channelList_;
        this.posterList = posterList;
        this.activity = activity;
        this.deletable = deletable;
    }
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 1: {
                View v1 = inflater.inflate(R.layout.item_poster,null);
                viewHolder = new PosterHolder(v1);
                break;
            }
            case 2: {
                View v2 = inflater.inflate(R.layout.item_empty, parent, false);
                viewHolder = new EmptyHolder(v2);
                break;
            }
            case 3: {
                View v3 = inflater.inflate(R.layout.item_channels_search, parent, false);
                viewHolder = new ChannelsHolder(v3);
                break;
            }
            case 4: {
                View v3 = inflater.inflate(R.layout.item_facebook_ads, parent, false);
                viewHolder = new FacebookNativeHolder(v3);
                break;
            }
            case 5: {
                View v4 = inflater.inflate(R.layout.item_admob_native_ads, parent, false);
                viewHolder = new AdmobNativeHolder(v4);
                break;
            }
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        switch (getItemViewType(position)) {
            case 1:

                final PosterHolder holder = (PosterHolder) viewHolder;
                Picasso.with(activity).load(posterList.get(position).getImage()).placeholder(R.drawable.poster_placeholder).into(holder.image_view_item_poster_image);
                if (deletable)
                    holder.relative_layout_item_poster_delete.setVisibility(View.VISIBLE);
                else
                    holder.relative_layout_item_poster_delete.setVisibility(View.GONE);


                if (posterList.get(position).getLabel() != null){
                    if (posterList.get(position).getLabel().length()>0) {
                        holder.text_view_item_poster_label.setText(posterList.get(position).getLabel());
                        holder.text_view_item_poster_label.setVisibility(View.VISIBLE);
                    }else{
                        holder.text_view_item_poster_label.setVisibility(View.GONE);
                    }
                }else{
                    holder.text_view_item_poster_label.setVisibility(View.GONE);
                }


                if (posterList.get(position).getSublabel() != null){
                    if (posterList.get(position).getSublabel().length()>0) {
                        holder.text_view_item_poster_sub_label.setText(posterList.get(position).getSublabel());
                        holder.text_view_item_poster_sub_label.setVisibility(View.VISIBLE);
                    }else{
                        holder.text_view_item_poster_sub_label.setVisibility(View.GONE);
                    }
                }else{
                    holder.text_view_item_poster_sub_label.setVisibility(View.GONE);
                }

                holder.image_view_item_poster_image.setOnClickListener(v -> {


                    ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.image_view_item_poster_image, "imageMain");
                    Intent intent = new Intent(activity, MovieActivity.class);
                    if (posterList.get(position).getType().equals("movie")) {
                        intent = new Intent(activity, MovieActivity.class);
                    } else if (posterList.get(position).getType().equals("serie")) {
                        intent = new Intent(activity, SerieActivity.class);
                    }
                    intent.putExtra("poster", posterList.get(holder.getAdapterPosition()));
                    final Intent intent1 = intent;

                    PrefManager prefManager= new PrefManager(activity);

                    if(checkSUBSCRIBED()){
                        activity.startActivity(intent1, activityOptionsCompat.toBundle());
                    }else{
                        if( prefManager.getString("ADMIN_INTERSTITIAL_TYPE").equals("ADMOB")){
                            requestAdmobInterstitial();

                            if(prefManager.getInt("ADMIN_INTERSTITIAL_CLICKS")<=prefManager.getInt("ADMOB_INTERSTITIAL_COUNT_CLICKS")){
                                if (admobInterstitialAd.isLoaded()) {
                                    prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",0);
                                    admobInterstitialAd.show();
                                    admobInterstitialAd.setAdListener(new AdListener() {
                                        @Override
                                        public void onAdClosed() {
                                            requestAdmobInterstitial();
                                            activity.startActivity(intent1, activityOptionsCompat.toBundle());
                                        }
                                    });
                                }else{
                                    activity.startActivity(intent, activityOptionsCompat.toBundle());
                                    requestAdmobInterstitial();
                                }
                            }else{
                                activity.startActivity(intent, activityOptionsCompat.toBundle());
                                prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",prefManager.getInt("ADMOB_INTERSTITIAL_COUNT_CLICKS")+1);
                            }
                        }else if(prefManager.getString("ADMIN_INTERSTITIAL_TYPE").equals("FACEBOOK")){
                            requestFacebookInterstitial();
                            if(prefManager.getInt("ADMIN_INTERSTITIAL_CLICKS")<=prefManager.getInt("ADMOB_INTERSTITIAL_COUNT_CLICKS")){
                                if (facebookInterstitialAd.isAdLoaded()) {
                                    prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",0);
                                    facebookInterstitialAd.show();
                                    code_selected = 1;
                                    position_selected = holder.getAdapterPosition();
                                    view_selected = holder.image_view_item_poster_image;
                                }else{
                                    activity.startActivity(intent, activityOptionsCompat.toBundle());
                                    requestFacebookInterstitial();
                                }
                            }else{
                                activity.startActivity(intent, activityOptionsCompat.toBundle());
                                prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",prefManager.getInt("ADMOB_INTERSTITIAL_COUNT_CLICKS")+1);
                            }
                        }else if(prefManager.getString("ADMIN_INTERSTITIAL_TYPE").equals("BOTH")){
                            requestAdmobInterstitial();
                            requestFacebookInterstitial();
                            if(prefManager.getInt("ADMIN_INTERSTITIAL_CLICKS")<=prefManager.getInt("ADMOB_INTERSTITIAL_COUNT_CLICKS")) {
                                if (prefManager.getString("AD_INTERSTITIAL_SHOW_TYPE").equals("ADMOB")){
                                    if (admobInterstitialAd.isLoaded()) {
                                        prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",0);
                                        prefManager.setString("AD_INTERSTITIAL_SHOW_TYPE","FACEBOOK");
                                        admobInterstitialAd.show();
                                        admobInterstitialAd.setAdListener(new AdListener(){
                                            @Override
                                            public void onAdClosed() {
                                                super.onAdClosed();
                                                activity.startActivity(intent1, activityOptionsCompat.toBundle());
                                                requestFacebookInterstitial();
                                            }
                                        });
                                    }else{
                                        activity.startActivity(intent, activityOptionsCompat.toBundle());
                                        requestFacebookInterstitial();
                                    }
                                }else{
                                    if (facebookInterstitialAd.isAdLoaded()) {
                                        prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",0);
                                        prefManager.setString("AD_INTERSTITIAL_SHOW_TYPE","ADMOB");
                                        facebookInterstitialAd.show();
                                        code_selected = 1;
                                        position_selected = holder.getAdapterPosition();
                                        view_selected = holder.image_view_item_poster_image;
                                    }else{
                                        activity.startActivity(intent, activityOptionsCompat.toBundle());
                                        requestFacebookInterstitial();
                                    }
                                }
                            }else{
                                activity.startActivity(intent, activityOptionsCompat.toBundle());
                                prefManager.setInt("ADMOB_INTERSTITIAL_COUNT_CLICKS",prefManager.getInt("ADMOB_INTERSTITIAL_COUNT_CLICKS")+1);
                            }
                        }else{
                            activity.startActivity(intent, activityOptionsCompat.toBundle());
                        }
                    }


                });
                holder.image_view_item_poster_delete.setOnClickListener(v->{
                    final PrefManager prefManager = new PrefManager(activity);
                    Integer id_user=  Integer.parseInt(prefManager.getString("ID_USER"));
                    String   key_user=  prefManager.getString("TOKEN_USER");
                    Retrofit retrofit = apiClient.getClient();
                    apiRest service = retrofit.create(apiRest.class);
                    Call<Integer> call = service.AddMyList(posterList.get(position).getId(),id_user,key_user,"poster");
                    call.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, retrofit2.Response<Integer> response) {
                            if (response.isSuccessful()){
                                if (response.body() == 202){
                                    Toasty.warning(activity, "This movie has been removed from your list", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                        }
                    });
                    posterList.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();

                });
                break;
            case 2:

                break;
            case 3:
                final ChannelsHolder holder_channel = (ChannelsHolder) viewHolder;
                this.linearLayoutManagerChannelAdapter=  new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
                this.channelAdapter =new ChannelAdapter(channelList,activity,deletable);
                holder_channel.recycle_view_channels_item.setHasFixedSize(true);
                holder_channel.recycle_view_channels_item.setAdapter(channelAdapter);
                holder_channel.recycle_view_channels_item.setLayoutManager(linearLayoutManagerChannelAdapter);
                channelAdapter.notifyDataSetChanged();
                break;
            case 5:{
                final AdmobNativeHolder holder_admob = (AdmobNativeHolder) viewHolder;

                holder_admob.adLoader.loadAd(new AdRequest.Builder().build());

                break;
            }
        }

    }
    @Override
    public int getItemCount() {
        return posterList.size();
    }
    public class PosterHolder extends RecyclerView.ViewHolder {
        private final TextView text_view_item_poster_label;
        private final TextView text_view_item_poster_sub_label;
        private ImageView image_view_item_poster_delete;
        public ImageView image_view_item_poster_image ;
        public RelativeLayout relative_layout_item_poster_delete ;
        public PosterHolder(View itemView) {
            super(itemView);
            this.image_view_item_poster_image =  (ImageView) itemView.findViewById(R.id.image_view_item_poster_image);
            this.relative_layout_item_poster_delete =  (RelativeLayout) itemView.findViewById(R.id.relative_layout_item_poster_delete);
            this.image_view_item_poster_delete =  (ImageView) itemView.findViewById(R.id.image_view_item_poster_delete);
            this.text_view_item_poster_sub_label =  (TextView) itemView.findViewById(R.id.text_view_item_poster_sub_label);
            this.text_view_item_poster_label =  (TextView) itemView.findViewById(R.id.text_view_item_poster_label);
        }
    }
    public class EmptyHolder extends RecyclerView.ViewHolder {
        public EmptyHolder(View itemView) {
            super(itemView);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if ((posterList.get(position).getTypeView())==0){
            return 1;
        }
        return   posterList.get(position).getTypeView();
    }

    private class ChannelsHolder extends RecyclerView.ViewHolder {
        private final RecyclerView recycle_view_channels_item;

        public ChannelsHolder(View v3) {
            super(v3);
            this.recycle_view_channels_item=(RecyclerView) itemView.findViewById(R.id.recycle_view_channels_item);
        }
    }

    public  class FacebookNativeHolder extends  RecyclerView.ViewHolder {
        private final String TAG = "WALLPAPERADAPTER";
        private com.facebook.ads.NativeAdLayout nativeAdLayout;
        private LinearLayout adView;
        private NativeAd nativeAd;
        public FacebookNativeHolder(View view) {
            super(view);
            loadNativeAd(view);
        }

        private void loadNativeAd(final View view) {
            // Instantiate a NativeAd object.
            // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
            // now, while you are testing and replace it later when you have signed up.
            // While you are using this temporary code you will only get test ads and if you release
            // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
            PrefManager prefManager= new PrefManager(activity);

            nativeAd = new NativeAd(activity,prefManager.getString("ADMIN_NATIVE_FACEBOOK_ID"));
            NativeAdListener nativeAdListener=new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {
                    // Native ad finished downloading all assets
                    Log.e(TAG, "Native ad finished downloading all assets.");
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    // Native ad failed to load
                    Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    // Native ad is loaded and ready to be displayed
                    Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                    // Race condition, load() called again before last ad was displayed
                    if (nativeAd == null || nativeAd != ad) {
                        return;
                    }
                   /* NativeAdViewAttributes viewAttributes = new NativeAdViewAttributes()
                            .setBackgroundColor(activity.getResources().getColor(R.color.colorPrimaryDark))
                            .setTitleTextColor(Color.WHITE)
                            .setDescriptionTextColor(Color.WHITE)
                            .setButtonColor(Color.WHITE);

                    View adView = NativeAdView.render(activity, nativeAd, NativeAdView.Type.HEIGHT_300, viewAttributes);

                    LinearLayout nativeAdContainer = (LinearLayout) view.findViewById(R.id.native_ad_container);
                    nativeAdContainer.addView(adView);*/
                    // Inflate Native Ad into Container
                    inflateAd(nativeAd,view);
                }

                @Override
                public void onAdClicked(Ad ad) {
                    // Native ad clicked
                    Log.d(TAG, "Native ad clicked!");
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    // Native ad impression
                    Log.d(TAG, "Native ad impression logged!");
                }
            };

            // Request an ad
            nativeAd.loadAd(
                    nativeAd.buildLoadAdConfig()
                            .withAdListener(nativeAdListener)
                            .build());
        }

        private void inflateAd(NativeAd nativeAd,View view) {

            nativeAd.unregisterView();

            // Add the Ad view into the ad container.
            nativeAdLayout = view.findViewById(R.id.native_ad_container);
            LayoutInflater inflater = LayoutInflater.from(activity);
            // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
            adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout_1, nativeAdLayout, false);
            nativeAdLayout.addView(adView);

            // Add the AdOptionsView
            LinearLayout adChoicesContainer = view.findViewById(R.id.ad_choices_container);
            AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);
            adChoicesContainer.removeAllViews();
            adChoicesContainer.addView(adOptionsView, 0);

            // Create native UI using the ad metadata.
            MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
            TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
            MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
            TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
            TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
            TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
            Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

            // Set the Text.
            nativeAdTitle.setText(nativeAd.getAdvertiserName());
            nativeAdBody.setText(nativeAd.getAdBodyText());
            nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
            nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
            nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
            sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

            // Create a list of clickable views
            List<View> clickableViews = new ArrayList<>();
            clickableViews.add(nativeAdTitle);
            clickableViews.add(nativeAdCallToAction);

            // Register the Title and CTA button to listen for clicks.
            nativeAd.registerViewForInteraction(
                    adView, nativeAdMedia, nativeAdIcon, clickableViews);




        }

    }
    public class AdmobNativeHolder extends RecyclerView.ViewHolder {
        private final AdLoader adLoader;
        private UnifiedNativeAd nativeAd;
        private FrameLayout frameLayout;

        public AdmobNativeHolder(@NonNull View itemView) {
            super(itemView);

            PrefManager prefManager= new PrefManager(activity);

            frameLayout = (FrameLayout) itemView.findViewById(R.id.fl_adplaceholder);
            AdLoader.Builder builder = new AdLoader.Builder(activity, prefManager.getString("ADMIN_NATIVE_ADMOB_ID"));

            builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                // OnUnifiedNativeAdLoadedListener implementation.
                @Override
                public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                    // You must call destroy on old ads when you are done with them,
                    // otherwise you will have a memory leak.
                    if (nativeAd != null) {
                        nativeAd.destroy();
                    }
                    nativeAd = unifiedNativeAd;

                    UnifiedNativeAdView adView = (UnifiedNativeAdView) activity.getLayoutInflater()
                            .inflate(R.layout.ad_unified, null);
                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                }

            });

            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();

            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();

            builder.withNativeAdOptions(adOptions);

            this.adLoader = builder.withAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {


                }
            }).build();

        }
    }

    /**
     * Populates a {@link UnifiedNativeAdView} object with data from a given
     * {@link UnifiedNativeAd}.
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView          the view to be populated
     */
    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        // adView.setNativeAd() is called.
        com.google.android.gms.ads.formats.MediaView mediaView = adView.findViewById(R.id.ad_media);

        mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if (child instanceof ImageView) {
                    ImageView imageView = (ImageView) child;
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    super.onVideoEnd();
                }
            });
        } else {

        }
    }
    public void selectOperation(Integer position,Integer code,View vew){
        switch (code){
            case 1:
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,vew, "imageMain");
                Intent intent = new Intent(activity, MovieActivity.class);
                if (posterList.get(position).getType().equals("movie")) {
                    intent = new Intent(activity, MovieActivity.class);
                } else if (posterList.get(position).getType().equals("serie")) {
                    intent = new Intent(activity, SerieActivity.class);
                }
                intent.putExtra("poster", posterList.get(position));
                activity.startActivity(intent);
                break;
        }
    }
    private void requestFacebookInterstitial() {
        if (facebookInterstitialAd==null) {
            PrefManager prefManager= new PrefManager(activity);
            facebookInterstitialAd = new com.facebook.ads.InterstitialAd(activity, prefManager.getString("ADMIN_INTERSTITIAL_FACEBOOK_ID"));
        }
        if (!facebookInterstitialAd.isAdLoaded()){

            InterstitialAdListener interstitialAdListener =   new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {}
                @Override
                public void onInterstitialDismissed(Ad ad) {
                    selectOperation(position_selected,code_selected,view_selected);
                }
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.v("FACEBOOK_INTER",adError.getErrorMessage());
                }
                @Override
                public void onAdLoaded(Ad ad) {Log.v("FACEBOOK_INTER","Loaded");}
                @Override
                public void onAdClicked(Ad ad) {}
                @Override
                public void onLoggingImpression(Ad ad) {}
            };
            facebookInterstitialAd.loadAd(
                    facebookInterstitialAd.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build());
        }
    }
    private void requestAdmobInterstitial() {
        if (admobInterstitialAd==null){
            PrefManager prefManager= new PrefManager(activity);
            admobInterstitialAd = new InterstitialAd(activity.getApplicationContext());
            admobInterstitialAd.setAdUnitId(prefManager.getString("ADMIN_INTERSTITIAL_ADMOB_ID"));
        }
        if (!admobInterstitialAd.isLoaded()){
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            admobInterstitialAd.loadAd(adRequest);
        }
    }
    public boolean checkSUBSCRIBED(){
        PrefManager prefManager= new PrefManager(activity);
        if (!prefManager.getString("SUBSCRIBED").equals("TRUE") && !prefManager.getString("NEW_SUBSCRIBE_ENABLED").equals("TRUE")) {
            return false;
        }
        return true;
    }
}
