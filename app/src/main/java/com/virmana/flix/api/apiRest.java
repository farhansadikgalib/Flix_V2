package com.virmana.flix.api;

import com.virmana.flix.config.Global;
import com.virmana.flix.entity.Actor;
import com.virmana.flix.entity.ApiResponse;
import com.virmana.flix.entity.Category;
import com.virmana.flix.entity.Channel;
import com.virmana.flix.entity.Comment;
import com.virmana.flix.entity.Country;
import com.virmana.flix.entity.Data;
import com.virmana.flix.entity.Genre;
import com.virmana.flix.entity.Language;
import com.virmana.flix.entity.Plan;
import com.virmana.flix.entity.Poster;
import com.virmana.flix.entity.Season;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Tamim on 28/09/2017.
 */

public interface apiRest {


    @GET("version/check/{code}/{user}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> check(@Path("code") Integer code,@Path("user") Integer user);

    @GET("install/add/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> addInstall(@Path("id") String id);

    @GET("device/{tkn}/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> addDevice(@Path("tkn")  String tkn);


    @GET("first/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<Data> homeData();

    @GET("search/{query}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<Data> searchData(@Path("query") String query);

    @GET("role/by/poster/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Actor>> getRolesByPoster(@Path("id") Integer id);

    @GET("actor/all/{page}/{search}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Actor>> getActorsList(@Path("page") Integer page,@Path("search") String search);

    @GET("movie/by/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<Poster> getPosterById(@Path("id") Integer id);

    @GET("channel/by/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<Channel> geChannelById(@Path("id") Integer id);


    @GET("movie/random/{genres}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Poster>> getRandomMoivies(@Path("genres") String genres);

    @GET("channel/random/{categories}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Channel>> getRandomChannel(@Path("categories") String categories);

    @FormUrlEncoded
    @POST("user/register/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> register(@Field("name") String name, @Field("username") String username, @Field("password") String password, @Field("type") String type, @Field("image") String image);


    @GET("user/password/{id}/{old}/{new_}/"+Global.SECURE_KEY+"/"+Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> changePassword(@Path("id") String id,@Path("old") String old,@Path("new_") String new_);


    @FormUrlEncoded
    @POST("user/login/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> login( @Field("username") String username, @Field("password") String password);


    @GET("user/email/{email}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> sendEmail(@Path("email") String email);

    @GET("user/request/{key}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> request(@Path("key") String key);


    @GET("user/reset/{id}/{key}/{new_password}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> reset(@Path("id") String user_id,@Path("key") String user_key,@Path("new_password") String new_password);


    @FormUrlEncoded
    @POST("user/token/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> editToken(@Field("user") Integer user, @Field("key") String key, @Field("token_f") String token_f, @Field("name") String name);


    @FormUrlEncoded
    @POST("comment/channel/add/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> addChannelComment(@Field("user") String user,@Field("key")  String key, @Field("id") Integer id, @Field("comment") String comment);


    @GET("comments/by/channel/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Comment>> getCommentsByChannel(@Path("id") Integer id);


    @FormUrlEncoded
    @POST("comment/poster/add/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> addPosterComment(@Field("user") String user,@Field("key")  String key, @Field("id") Integer id, @Field("comment") String comment);


    @GET("comments/by/poster/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Comment>> getCommentsByPoster(@Path("id") Integer id);

    @GET("subtitles/by/movie/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Language>> getSubtitlesByPoster(@Path("id") Integer id);

    @GET("subtitles/by/episode/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Language>> getSubtitlesByEpisode(@Path("id") Integer id);


    @FormUrlEncoded
    @POST("rate/poster/add/"+Global.SECURE_KEY+"/"+Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> addPosterRate(@Field("user")  String user,@Field("key")  String key,@Field("poster") Integer poster,@Field("value") float value);


    @FormUrlEncoded
    @POST("rate/channel/add/"+Global.SECURE_KEY+"/"+Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> addChannelRate(@Field("user")  String user,@Field("key")  String key,@Field("channel") Integer channel,@Field("value") float value);

    @FormUrlEncoded
    @POST("poster/add/share/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addPosterShare(@Field("id")  Integer id);

    @FormUrlEncoded
    @POST("channel/add/share/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addChannelShare(@Field("id")  Integer id);

    @GET("movie/by/actor/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Poster>> getPosterByActor(@Path("id") Integer id);

    @FormUrlEncoded
    @POST("support/add/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> addSupport(@Field("email") String email, @Field("name") String name , @Field("message") String message);


    @GET("movie/by/filtres/{genre}/{order}/{page}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Poster>> getMoviesByFiltres(@Path("genre") Integer genre,@Path("order") String order,@Path("page") Integer page);


    @GET("poster/by/filtres/{genre}/{order}/{page}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Poster>> getPostersByFiltres(@Path("genre") Integer genre,@Path("order") String order,@Path("page") Integer page);


    @GET("genre/all/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Genre>> getGenreList();

    @GET("serie/by/filtres/{genre}/{order}/{page}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Poster>> getSeriesByFiltres(@Path("genre") Integer genre,@Path("order") String order,@Path("page") Integer page);

    @GET("season/by/serie/{id}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Season>> getSeasonsBySerie(@Path("id") Integer id);

    @Multipart
    @POST("user/edit/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<ApiResponse> editProfile(@Part MultipartBody.Part file, @Part("id") Integer id, @Part("key") String key, @Part("name") String name);

    @GET("country/all/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Country>> getCountiesList();

    @GET("category/all/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Category>> getCategoriesList();

    @GET("channel/by/filtres/{category}/{country}/{page}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Channel>> getChannelsByFiltres(@Path("category") Integer category,@Path("country") Integer country,@Path("page") Integer page);

    @FormUrlEncoded
    @POST("movie/add/download/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addMovieDownload(@Field("id")  Integer id);

    @FormUrlEncoded
    @POST("episode/add/download/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addEpisodeDownload(@Field("id")  Integer id);

    @FormUrlEncoded
    @POST("movie/add/view/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addMovieView(@Field("id")  Integer id);

    @FormUrlEncoded
    @POST("episode/add/view/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addEpisodeView(@Field("id")  Integer id);

    @FormUrlEncoded
    @POST("channel/add/view/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> addChannelView(@Field("id")  Integer id);

    @GET("pack/all/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<List<Plan>> getPlans();

    @FormUrlEncoded
    @POST("subscription/paypal/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> SubscriptionPayPal(@Field("user") Integer user,@Field("key")  String key, @Field("id") String id, @Field("plan") int plan);

    @FormUrlEncoded
    @POST("subscription/stripe/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> SubscriptionStripe(@Field("user") Integer user,@Field("key")  String key, @Field("id") String id, @Field("plan") int plan);


    @FormUrlEncoded
    @POST("subscription/intent/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> StripeIntent(@Field("user") Integer user,@Field("key")  String key, @Field("plan") int plan);


    @Multipart
    @POST("subscription/cash/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<ApiResponse> SubscriptionCash(@Part MultipartBody.Part file, @Part("user") Integer user,@Part("key")  String key, @Part("id") String id, @Part("infos") String infos, @Part("plan") int plan);

    @GET("mylist/{id}/{key}/" + Global.SECURE_KEY + "/" + Global.ITEM_PURCHASE_CODE + "/")
    Call<Data> myList(@Path("id") Integer id,@Path("key") String key_user);


    @FormUrlEncoded
    @POST("add/mylist/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> AddMyList(@Field("id")  Integer id,@Field("user")  Integer user,@Field("key")  String key,@Field("type")  String type);

    @FormUrlEncoded
    @POST("check/mylist/"+ Global.SECURE_KEY+"/"+ Global.ITEM_PURCHASE_CODE+"/")
    Call<Integer> CheckMyList(@Field("id")  Integer id,@Field("user")  Integer user,@Field("key")  String key,@Field("type")  String type);

}

