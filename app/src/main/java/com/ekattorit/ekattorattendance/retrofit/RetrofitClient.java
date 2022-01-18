package com.ekattorit.ekattorattendance.retrofit;
import com.ekattorit.ekattorattendance.network_class.ApiInterface;
import com.ekattorit.ekattorattendance.utils.AppConfig;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    //private static final String BASE_URL = AppConfig.Base_URL_ONLINE;
    private static final String BASE_URL = AppConfig.Base_URL_ONLINE;
    private static RetrofitClient mInstance;
    private final Retrofit retrofit;

    private RetrofitClient() {


        retrofit = new Retrofit.Builder()
                . baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public ApiInterface getApi() {
        return retrofit.create(ApiInterface.class);
    }
}
