package com.example.android.quakereport;

/**
 * Created by Anku on 2/10/2017.
 */

public class Earthquake {

    /* Magnitude of the earthquake */
    private double mMagnitude;

    /* Location of the earthquake */
    private String mLocation;

    /* Time of the earthquake */
    private long mTimeInMilliseconds;

    /* url of the earthquake */
    private String mUrl;

    /**
     * @param mMagnitude          magnitude of the earthquake
     * @param mLocation           Location of the earthquake
     * @param mTimeInMilliseconds Date of the earthquake
     */
    public Earthquake(double mMagnitude, String mLocation, long mTimeInMilliseconds, String mUrl) {
        this.mMagnitude = mMagnitude;
        this.mLocation = mLocation;
        this.mTimeInMilliseconds = mTimeInMilliseconds;
        this.mUrl = mUrl;
    }

    /**
     * Returns magnitude of the earthquake
     */
    public double getmMagnitude() {
        return mMagnitude;
    }

    /**
     * Returns date of the earthquake
     */
    public long getmTimeInMilliseconds() {
        return mTimeInMilliseconds;
    }

    /**
     * Returns Location of the earthquake
     */
    public String getmLocation() {
        return mLocation;
    }

    /**
     * Returns url of the earthquake
     */
    public String getmUrl() {
        return mUrl;
    }
}
