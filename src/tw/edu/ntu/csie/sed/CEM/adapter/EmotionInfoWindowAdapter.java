package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.HashMap;
import java.util.Map;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.model.EmotionBubble;
import android.app.Activity;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class EmotionInfoWindowAdapter implements InfoWindowAdapter {
	private TypedArray icons;
	private LayoutInflater inflater;

	private Map<String, EmotionBubble> bubbles = new HashMap<String, EmotionBubble>();

	protected EmotionBubble selectedBubble;

	public EmotionInfoWindowAdapter(Activity activity) {
		icons = activity.getResources().obtainTypedArray(R.array.emotion_icon_array);
		inflater = activity.getLayoutInflater();
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		EmotionBubble bubble = bubbles.get(marker.getId());
		selectedBubble = bubble;
		View window = inflater.inflate(R.layout.infowindow_emotion, null);
		TextView placeTextView = ((TextView) window.findViewById(R.id.textview_place));
		placeTextView.setText(bubble.getPlace());
		ImageView imageView = (ImageView) window.findViewById(R.id.imageview_emotion);
		Log.i(getClass().getName(), bubble.getEmotion());
		int type = Integer.parseInt(bubble.getEmotion());
		if (type <= 0 || type > 7) {
			type = 1;
		}
		imageView.setImageResource(icons.getResourceId(type - 1, 0));
		TextView messageTextView = ((TextView) window.findViewById(R.id.textview_message));
		messageTextView.setText(bubble.getMessage());

		return window;
	}

	public void registerBubble(EmotionBubble bubble) {
		bubbles.put(bubble.getMarker().getId(), bubble);
	}

	public EmotionBubble getSelectedBubble() {
		return selectedBubble;
	}
}