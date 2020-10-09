package vip.fastgo.tracer.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiClient {
    private static ApiClient instance = null;

    private static OkHttpClient client;

    private ApiClient() {
        client = new OkHttpClient.Builder()
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public static ApiClient getClient() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }


    public void post(String url, RequestBody formBody, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void post(String url, Map<String, Object> params, Callback callback) {
        FormBody.Builder builder = new FormBody.Builder();
        for ( String key: params.keySet()) {
            builder.add(key, String.valueOf(params.get(key)));
        }

        post(url, builder.build(), callback);
    }

    public void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

}
