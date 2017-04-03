/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;



public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>>{

    /* Tag for the log messages*/
    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    /* Adapter for the list of earthquakes*/
    private  EarthquakeAdapter mAdapter;

    /* URL to query USGS servers for earthqukae information*/
    public static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query";

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    /** SwipeToRefresh Layout, used to refresh the ListView*/
    private SwipeRefreshLayout mSwipeToRefresh;

    /** mLoaderManager to manage loaders
     *  initialzing it with an instance of LoaderManger
     */
    private LoaderManager mloadermanager = getLoaderManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Setting emptyStateTextView which will be displayed when there are no Earthquakes
        // Keeping the TextView empty at first to avoid "no earthquake message" before first load
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter which takes the list of earthquake as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

        // Set OnItemClickListener on listView items so that a clicking on
        // a earthquake opens the USGS url for that earthquake
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Get the current Earthquake
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Get the url from currentEarthquake and then parse it into Uri object
                Uri earthquakeUri = Uri.parse(currentEarthquake.getmUrl());

                // Create a new intent to view the earthquake Uri
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW,earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);

            }
        });

        // Checking for Internet connection using checkConnectivity function
        // We will initiate the loader only if there is an internet connection
        if(checkConnectivity())  {

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            mloadermanager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        }
        else {
            // Otherwise display error
            // First hide the loading circle indicator
            ProgressBar loadingCircle = (ProgressBar) findViewById(R.id.loading_circle);
            loadingCircle.setVisibility(View.GONE);

            // Now set the emptyView to show No Internet connection available error
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }


        // Implementing swipeToRefresh Widget which will allow
        // us to refresh the list of earthquakes
        mSwipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);

        //Setting OnRefreshListener for mSwipeToRefresh layout
        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // Calling mloaderManger.restartLoader to fetch and set the data all over again
                // Checking if mloadermanger is not null and network connection is available
                if(mloadermanager != null && checkConnectivity()) {

                    // When No Internet connection message is displaying on screen
                    // swiping down doesn't show animation so we will use our loadingCircle to
                    // show animation while loading

                    // Checking if EmptyStateTextView is showing no internet connection message
                    // if yes then make the loading circle visible
                    if(TextUtils.equals(mEmptyStateTextView.getText(),getString(R.string.no_internet_connection))) {
                        ProgressBar loadingCircle = (ProgressBar) findViewById(R.id.loading_circle);
                        loadingCircle.setVisibility(View.VISIBLE);
                    }

                    // As the loadingCircle is showing so we will hide the EmptyStateTextView
                    mEmptyStateTextView.setVisibility(View.GONE);
                    // Restarting loader
                    mloadermanager.restartLoader(EARTHQUAKE_LOADER_ID, null, EarthquakeActivity.this);
                }
                else {
                    // Clear the adapter to view the no Internet Connection TextView
                    mAdapter.clear();
                    // Otherwise display error
                    // First hide the loading circle indicator
                    ProgressBar loadingCircle = (ProgressBar) findViewById(R.id.loading_circle);
                    loadingCircle.setVisibility(View.GONE);

                    // Now set the emptyView to show No Internet connection available error
                    mEmptyStateTextView.setText(R.string.no_internet_connection);

                    // Telling mSwipeToRefresh that refreshing has been done and stop the animation
                    mSwipeToRefresh.setRefreshing(false);
                }


            }
        });



    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        String noOfEarthquake = sharedPrefs.getString(
                getString(R.string.setting_min_noOfEarthquake_key),
                getString(R.string.setting_min_noOfEarthquake_default));


        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", noOfEarthquake);
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        // Create a new loader for the given URL
        return new EarthquakeLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> data) {
        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_earthquakes);

        // Make the loading circle invisible now
        ProgressBar loadingCircle = (ProgressBar) findViewById(R.id.loading_circle);
        loadingCircle.setVisibility(View.GONE);

        // Telling mSwipeToRefresh that refreshing has been done and stop the animation
        mSwipeToRefresh.setRefreshing(false);

    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating the menu.xml file to show menu on Title bar
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If an option has been selected then getting the id of
        // the option and then performing task accordingly
        // In this we are opening settingsActivity using an Intent
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Function to check active network connection of device
     * @return true if device is connected to internet else false
     */
    private boolean checkConnectivity() {

        // Getting an instance of ConnectivityManager class
        ConnectivityManager cnnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = cnnMgr.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            return true;
        }

        return false;
    }
}
