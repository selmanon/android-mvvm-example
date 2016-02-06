package de.rheinfabrik.mvvm_example.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import de.rheinfabrik.mvvm_example.network.deserializer.SearchResultsDeserializer;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Class used to generate OMDBApiService instances.
 */
public class OMDBApiFactory {

    // Constants

    private static final String API_ENDPOINT = "http://www.omdbapi.com";

    // Public API

    /**
     * Creates a fresh OMDBApiService instance.
     */
    public static OMDBApiService newApi() {
        return newApi(new OkHttpClient());
    }

    /**
     * Creates a fresh OMDBApiService instance with the given client.
     */
    public static OMDBApiService newApi(OkHttpClient client) {

        // Gson
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(SearchResultsDeserializer.TYPE, new SearchResultsDeserializer());
        Gson gson = gsonBuilder.create();

        // Build API service
        return new Retrofit.Builder()
                .client(client)
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(OMDBApiService.class);
    }
}
