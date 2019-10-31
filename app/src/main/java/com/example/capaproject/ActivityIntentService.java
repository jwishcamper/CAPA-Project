package com.example.capaproject;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityIntentService extends IntentService {

    public ActivityIntentService() {
        super("ActivityIntentService");
    }
    public ActivityIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getMostProbableActivity() );
        }
    }
    private void handleDetectedActivities(DetectedActivity probableActivities) {

        switch( probableActivities.getType() ) {
            case DetectedActivity.IN_VEHICLE: {
                //Log.e( "ActivityRecogition", "In Vehicle: " + probableActivities.getConfidence() );
                //MainActivity.t.setText("In Vehicle: " + probableActivities.getConfidence());
                MainActivity.Companion.setCurrentActivity("In Vehicle: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                MainActivity.Companion.setCurrentActivity("On Bicycle: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.ON_FOOT: {
                MainActivity.Companion.setCurrentActivity("On Foot: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.RUNNING: {
                MainActivity.Companion.setCurrentActivity("Running: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.STILL: {
                MainActivity.Companion.setCurrentActivity("Still: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.TILTING: {
                MainActivity.Companion.setCurrentActivity("Tilting: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.WALKING: {
                MainActivity.Companion.setCurrentActivity("Walking: " + probableActivities.getConfidence() +"%");
                break;
            }
            case DetectedActivity.UNKNOWN: {
                MainActivity.Companion.setCurrentActivity("Unknown: " + probableActivities.getConfidence() +"%");
                break;
            }
        }

    }
}

