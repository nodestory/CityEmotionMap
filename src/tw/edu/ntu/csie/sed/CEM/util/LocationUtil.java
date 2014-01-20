package tw.edu.ntu.csie.sed.CEM.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationUtil implements LocationListener {
	private LocationManager mLocationManager;

	public LocationUtil(Context context) {
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public void startUpdatingLocation() {
		mLocationManager.requestLocationUpdates(getBestProvider(), 0, 0, this);
	}

	public void stopUpdatingLocation() {
		mLocationManager.removeUpdates(this);
	}

	private String getBestProvider() {
		return mLocationManager.getBestProvider(new Criteria(), true);
	}

	// Implement LocationListener
	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
