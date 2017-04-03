package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by Anku on 3/26/2017.
 */

/**
 * Loads a list of earthquakes by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    /** Tag for log messages */
    private static final String LOG_TAG = EarthquakeLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link EarthquakeLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public EarthquakeLoader(Context context, String url) {
        super(context);
        this.mUrl = url;
    }

    /**
     * onStartLoading() method will call forceLoad() which is a required step to actually trigger
     * the loadInBackground method to execute
     */
    @Override
    protected void onStartLoading() {
        // Need to call forceLoad to call loadInBackground() method
        forceLoad();
    }

    /**
     * This is on a background thread.
     */

    @Override
    public List<Earthquake> loadInBackground() {
        Log.v(LOG_TAG,"I am in loadInBackground");
        // Don't perform the request if there are no URLs, or the first URL is null.
        if (mUrl == null || TextUtils.isEmpty(mUrl)) {
            return null;
        }

        List<Earthquake> result = QueryUtils.fetchEarthquakeData(mUrl);
        return result;
        }


    }



