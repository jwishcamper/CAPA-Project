package com.example.capaproject;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityIntentService extends IntentService {

    public ActivityIntentService() {
        super("ActivityIntentService");
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
                //Log.e( "ActivityRecognition", "In Vehicle: " + probableActivities.getConfidence() );
                //MainActivity.t.setText("In Vehicle: " + probableActivities.getConfidence());
                MainActivity.Companion.setCurrentActivity("In Vehicle");
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                MainActivity.Companion.setCurrentActivity("On Bicycle");
                break;
            }
            case DetectedActivity.ON_FOOT: {
                MainActivity.Companion.setCurrentActivity("On Foot");
                break;
            }
            case DetectedActivity.RUNNING: {
                MainActivity.Companion.setCurrentActivity("Running");
                break;
            }
            case DetectedActivity.STILL: {
                MainActivity.Companion.setCurrentActivity("Still");
                break;
            }
            case DetectedActivity.TILTING: {
                MainActivity.Companion.setCurrentActivity("Tilting");
                break;
            }
            case DetectedActivity.WALKING: {
                MainActivity.Companion.setCurrentActivity("Walking");
                break;
            }
            case DetectedActivity.UNKNOWN: {
                MainActivity.Companion.setCurrentActivity("Unknown");
                break;
            }
        }

    }
}

