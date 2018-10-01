package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.serializer.DateDeserializer;
import com.bence.projector.common.serializer.DateSerializer;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    public static Retrofit getClient() {
        String BASE_URL = "http://192.168.100.4:8080";
        String SECOND_BASE_URL = "http://192.168.100.5:8080";
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient shortOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor).build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor).build();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new DateDeserializer()).registerTypeAdapter(Date.class,
                new DateSerializer());
        GsonConverterFactory factory = GsonConverterFactory.create(gsonBuilder.create());
        Retrofit build = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(factory)
                .client(shortOkHttpClient)
                .build();
        Call<List<LanguageDTO>> languages = build.create(LanguageApi.class).getLanguages();
        Response<List<LanguageDTO>> response;
        try {
            response = languages.execute();
            if (response.isSuccessful()) {
                return build;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Retrofit.Builder()
                .baseUrl(SECOND_BASE_URL)
                .addConverterFactory(factory)
                .client(okHttpClient)
                .build();

    }
}
