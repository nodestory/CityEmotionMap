package tw.edu.ntu.csie.sed.CEM.activity;

import tw.edu.ntu.csie.sed.CEM.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;
import com.facebook.widget.PlacePickerFragment;

public class SelectActivity extends SherlockFragmentActivity {
	public static final Uri FRIEND_PICKER = Uri.parse("picker://friend");
    public static final Uri PLACE_PICKER = Uri.parse("picker://place");

    private static final int SEARCH_RADIUS_METERS = 1000;
    private static final int SEARCH_RESULT_LIMIT = 50;
    private static final String SEARCH_TEXT = "Restaurant";
    private static final int LOCATION_CHANGE_THRESHOLD = 50; // meters

    private static final Location SAN_FRANCISCO_LOCATION = new Location("") {{
            setLatitude(37.7750);
            setLongitude(-122.4183);
    }};

    private FriendPickerFragment friendPickerFragment;
    private PlacePickerFragment placePickerFragment;
    private LocationListener locationListener;
    
//    private Session.StatusCallback callback = new Session.StatusCallback() {
//        @Override
//        public void call(Session session, SessionState state, Exception exception) {
//            onSessionStateChange(session, state, exception);
//        }
//    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Bundle args = getIntent().getExtras();
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentToShow = null;
        Uri intentUri = getIntent().getData();

        if (FRIEND_PICKER.equals(intentUri)) {
            if (savedInstanceState == null) {
                friendPickerFragment = new FriendPickerFragment(args);
            } else {
                friendPickerFragment = (FriendPickerFragment) manager.findFragmentById(R.id.picker_fragment);;
            }

            friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
                @Override
                public void onError(PickerFragment<?> fragment, FacebookException error) {
                	SelectActivity.this.onError(error);
                }
            });
            friendPickerFragment.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
                @Override
                public void onDoneButtonClicked(PickerFragment<?> fragment) {
                    finishActivity();
                }
            });
            fragmentToShow = friendPickerFragment;

        } else if (PLACE_PICKER.equals(intentUri)) {
            if (savedInstanceState == null) {
                placePickerFragment = new PlacePickerFragment(args);
            } else {
                placePickerFragment = (PlacePickerFragment) manager.findFragmentById(R.id.picker_fragment);
            }
            placePickerFragment.setOnSelectionChangedListener(new PickerFragment.OnSelectionChangedListener() {
                @Override
                public void onSelectionChanged(PickerFragment<?> fragment) {
                    finishActivity(); // call finish since you can only pick one place
                }
            });
            placePickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
                @Override
                public void onError(PickerFragment<?> fragment, FacebookException error) {
                	SelectActivity.this.onError(error);
                }
            });
            placePickerFragment.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
                @Override
                public void onDoneButtonClicked(PickerFragment<?> fragment) {
                    finishActivity();
                }
            });
            fragmentToShow = placePickerFragment;
        } else {
            // Nothing to do, finish
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        manager.beginTransaction().replace(R.id.picker_fragment, fragmentToShow).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FRIEND_PICKER.equals(getIntent().getData())) {
            try {
                friendPickerFragment.loadData(false);
            } catch (Exception ex) {
                onError(ex);
            }
        } else if (PLACE_PICKER.equals(getIntent().getData())) {
            try {
                Location location = null;
                Criteria criteria = new Criteria();
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                String bestProvider = locationManager.getBestProvider(criteria, false);
                if (bestProvider != null) {
                    location = locationManager.getLastKnownLocation(bestProvider);
                    if (locationManager.isProviderEnabled(bestProvider) && locationListener == null) {
                        locationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                float distance = location.distanceTo(placePickerFragment.getLocation());
                                if (distance >= LOCATION_CHANGE_THRESHOLD) {
                                    placePickerFragment.setLocation(location);
                                    placePickerFragment.loadData(true);
                                }
                            }
                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {
                            }
                            @Override
                            public void onProviderEnabled(String s) {
                            }
                            @Override
                            public void onProviderDisabled(String s) {
                            }
                        };
                        locationManager.requestLocationUpdates(bestProvider, 1, LOCATION_CHANGE_THRESHOLD,
                                locationListener, Looper.getMainLooper());
                    }
                }
                if (location == null) {
                    String model = Build.MODEL;
                    if (model.equals("sdk") || model.equals("google_sdk") || model.contains("x86")) {
                        // this may be the emulator, pretend we're in an exotic place
                        location = SAN_FRANCISCO_LOCATION;
                    }
                }
                if (location != null) {
                    placePickerFragment.setLocation(location);
                    placePickerFragment.setRadiusInMeters(SEARCH_RADIUS_METERS);
//                    placePickerFragment.setSearchText(SEARCH_TEXT);
                    placePickerFragment.setResultsLimit(SEARCH_RESULT_LIMIT);
                    placePickerFragment.loadData(false);
                } else {
                	//TODO
                    onError(getResources().getString(R.string.hello), true);
                }
            } catch (Exception ex) {
                onError(ex);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationListener != null) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    private void onError(Exception error) {
    	//TODO
//        String text = getString(R.string.hello, error.getMessage());
    	Log.d("!!", error.getMessage());
        String text = error.getMessage();
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void onError(String error, final boolean finishActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //TODO
        builder.setTitle(R.string.hello).
                setMessage(error).
                setPositiveButton(R.string.hello, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (finishActivity) {
                            finishActivity();
                        }
                    }
                });
        builder.show();
    }
    /*
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        Log.d("!!!!!!!!!", session.getState().toString());
        if (session != null && session.isOpened()) {
        	Session.openActiveSessionFromCache(getApplicationContext());
        } else {
        	if (FRIEND_PICKER.equals(getIntent().getData())) {
                try {
                    friendPickerFragment.loadData(false);
                } catch (Exception ex) {
                    onError(ex);
                }
            } else if (PLACE_PICKER.equals(getIntent().getData())) {
                try {
                    Location location = null;
                    Criteria criteria = new Criteria();
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String bestProvider = locationManager.getBestProvider(criteria, false);
                    if (bestProvider != null) {
                        location = locationManager.getLastKnownLocation(bestProvider);
                        if (locationManager.isProviderEnabled(bestProvider) && locationListener == null) {
                            locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    float distance = location.distanceTo(placePickerFragment.getLocation());
                                    if (distance >= LOCATION_CHANGE_THRESHOLD) {
                                        placePickerFragment.setLocation(location);
                                        placePickerFragment.loadData(true);
                                    }
                                }
                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) {
                                }
                                @Override
                                public void onProviderEnabled(String s) {
                                }
                                @Override
                                public void onProviderDisabled(String s) {
                                }
                            };
                            locationManager.requestLocationUpdates(bestProvider, 1, LOCATION_CHANGE_THRESHOLD,
                                    locationListener, Looper.getMainLooper());
                        }
                    }
                    if (location == null) {
                        String model = Build.MODEL;
                        if (model.equals("sdk") || model.equals("google_sdk") || model.contains("x86")) {
                            // this may be the emulator, pretend we're in an exotic place
                            location = SAN_FRANCISCO_LOCATION;
                        }
                    }
                    if (location != null) {
                        placePickerFragment.setLocation(location);
                        placePickerFragment.setRadiusInMeters(SEARCH_RADIUS_METERS);
                        placePickerFragment.setSearchText(SEARCH_TEXT);
                        placePickerFragment.setResultsLimit(SEARCH_RESULT_LIMIT);
                        placePickerFragment.loadData(false);
                    } else {
                    	//TODO
                        onError(getResources().getString(R.string.hello), true);
                    }
                } catch (Exception ex) {
                    onError(ex);
                }
            }
        }
    }
	*/
    private void finishActivity() {
    	Intent intent = new Intent();
        if (FRIEND_PICKER.equals(getIntent().getData())) {
            if (friendPickerFragment != null) {
            }
        } else if (PLACE_PICKER.equals(getIntent().getData())) {
            if (placePickerFragment != null) {
            	intent.putExtra("placeName", placePickerFragment.getSelection().getName());
            	intent.putExtra("placeId", placePickerFragment.getSelection().getId());
            	intent.putExtra("latitude", placePickerFragment.getSelection().getLocation().getLatitude());
            	intent.putExtra("longitude", placePickerFragment.getSelection().getLocation().getLongitude());
            }
        }
        setResult(RESULT_OK, intent);
        finish();
    }
}
