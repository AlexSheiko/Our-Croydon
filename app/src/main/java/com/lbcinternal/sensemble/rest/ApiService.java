package com.lbcinternal.sensemble.rest;

import com.lbcinternal.sensemble.rest.model.Idea;
import com.lbcinternal.sensemble.rest.model.IdeaDetails;
import com.lbcinternal.sensemble.rest.model.User;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiService {

    @GET("/api/news/getall") void getNews(
            Callback<Response> callback);

    @GET("/api/offers/getall") void getOffers(
            Callback<Response> callback);

    @GET("/Api/Posts/GetList/500/0") void getIdeas(
            @Query("order") int order,
            Callback<List<Idea>> callback);

    @GET("/Api/Posts/GetList/500/0") void findIdeas(
            @Query("filter") String query,
            Callback<List<Idea>> callback);

    @GET("/Api/Posts/Get/{ideaId}") void getIdeaDetails(
            @Path("ideaId") String id,
            Callback<IdeaDetails> callback);

    @GET("/Account/login.aspx") void login(
            @Query("apiLogin") String username,
            @Query("apiPassword") String password,
            @Query("rememberMe") boolean remember,
            Callback<User> callback);

}
