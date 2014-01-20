package tw.edu.ntu.csie.sed.CEM.model;

import com.google.android.gms.maps.model.Marker;

public abstract class Bubble {
	public abstract String getKey();

	private Marker marker;

	public Bubble(Marker marker) {
		setMarker(marker);
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setVisible(boolean isVisible) {
		marker.setVisible(isVisible);
	}
}