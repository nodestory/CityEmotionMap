package tw.edu.ntu.csie.sed.CEM.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.adapter.EmotionDrawerAdapter;
import tw.edu.ntu.csie.sed.CEM.adapter.EmotionInfoWindowAdapter;
import tw.edu.ntu.csie.sed.CEM.adapter.MissionDrawerAdapter;
import tw.edu.ntu.csie.sed.CEM.model.EmotionBubble;
import tw.edu.ntu.csie.sed.CEM.model.MissionBubble;
import tw.edu.ntu.csie.sed.CEM.rest.APIHelper;
import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SimpleMapFragment extends Fragment implements HTTPResponseReceiver {
	// UI Elements
	private MapView mMapView;
	private GoogleMap mMap;
	private CommentDialogFragment mDialog;

	private boolean mEmotionMapEnabled = true;
	private BaseAdapter mDrawerAdapter;
	private OnMapUpdatedListener mListener;
	private EmotionInfoWindowAdapter mEmotionInfoWindowAdapter;

	private APIHelper mAPIHelper;

	public interface OnMapUpdatedListener {
		public void beforeDrawerChanged(BaseAdapter adapter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			MapsInitializer.initialize(getActivity());
		} catch (GooglePlayServicesNotAvailableException impossible) {
		}
		mAPIHelper = new APIHelper(getActivity(), this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_emotion_map, container, false);
		mMapView = (MapView) view.findViewById(R.id.mapview);
		mMapView.onCreate(savedInstanceState);
		mMap = mMapView.getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.015008, 121.534216), 13));
		mMap.setMyLocationEnabled(true);
		mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				showCommentDialog(marker.getTitle());
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		mDrawerAdapter = new EmotionDrawerAdapter(getActivity());
		mEmotionInfoWindowAdapter = new EmotionInfoWindowAdapter(getActivity());
		mMap.setInfoWindowAdapter(mEmotionInfoWindowAdapter);
		if (mEmotionMapEnabled) {
			mAPIHelper.getCheckins("25.015008", "121.534216", "100");
		} else {
			mAPIHelper.getMissionList();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnMapUpdatedListener) {
			mListener = (OnMapUpdatedListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet EmotionMapFragment.OnMapUpdatedListener!");
		}
	}

	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mMapView.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	public void setEmotionMapEnabled(boolean isEmotionMapEnabled) {
		mEmotionMapEnabled = isEmotionMapEnabled;
		if (mEmotionMapEnabled) {
			((MissionDrawerAdapter) mDrawerAdapter).reset();
			mDrawerAdapter = new EmotionDrawerAdapter(getActivity());
			// TODO
			mAPIHelper.getCheckins("25.015008", "121.534216", "0.5");
			mMap.setInfoWindowAdapter(new EmotionInfoWindowAdapter(getActivity()));
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.015008, 121.534216), 13));
		} else {
			((EmotionDrawerAdapter) mDrawerAdapter).reset();
			mDrawerAdapter = new MissionDrawerAdapter(getActivity(), new String[] { getResources().getString(
					R.string.loading) });
			mAPIHelper.getMissionList();
			mAPIHelper.getCompletedMissions(getActivity().getSharedPreferences("CEM", 0).getString("fb_id",
					""));
			mMap.setInfoWindowAdapter(null);
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.015008, 121.534216), 10));
		}
		mListener.beforeDrawerChanged(mDrawerAdapter);
	}

	public void showCommentDialog(String messageId) {
		mDialog = CommentDialogFragment.newInstance(messageId);
		mDialog.show(getFragmentManager(), "comment");
		mAPIHelper.getCommments(messageId);
	}

	public void updateCommentDialog(JSONObject object) throws JSONException {
		JSONArray comments = object.getJSONArray("comments");
		String[] messages = new String[comments.length()];
		for (int i = 0; i < comments.length(); i++) {
			try {
				JSONObject comment = (JSONObject) comments.get(i);
				messages[i] = comment.getString("message");
				Log.i(getClass().getName() + ": comment", messages[i]);
			} catch (JSONException e) {
				e.printStackTrace();
				messages[i] = "";
			}
		}
		mDialog.update(messages);
	}

	private void updateEmotionMap(JSONArray checkins) {
		for (int i = 0; i < checkins.length(); i++) {
			JSONObject object;
			try {
				object = (JSONObject) checkins.get(i);
				MarkerOptions options = new MarkerOptions();
				options.icon(BitmapDescriptorFactory.fromBitmap(getBubbleDMarker(object.getString("color"),
						object.getString("inflation"))));
				options.position(new LatLng(Double.valueOf(object.getString("latitude")), Double
						.valueOf(object.getString("longitude"))));
				Marker marker = mMap.addMarker(options);
				marker.setTitle(object.getString("BubbleID"));
				EmotionBubble bubble = new EmotionBubble(marker);
				Log.i(getClass().getName(), object.getString("BubbleID"));
				bubble.init(object.getString("userID"), object.getString("BubbleID"),
						object.getString("emotionType"), object.getString("message"), "",
						Integer.valueOf(object.getString("inflation")));
				((EmotionDrawerAdapter) mDrawerAdapter).addBubble(bubble);
				mEmotionInfoWindowAdapter.registerBubble(bubble);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mListener.beforeDrawerChanged(mDrawerAdapter);
	}

	private void updateMissionMap(JSONArray missions) {
		String missionNames[] = new String[missions.length()];
		for (int i = 0; i < missions.length(); i++) {
			JSONObject object;
			try {
				object = (JSONObject) missions.get(i);
				missionNames[i] = object.getString("name");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mDrawerAdapter = new MissionDrawerAdapter(getActivity(), missionNames);
		for (int i = 0; i < missions.length(); i++) {
			JSONObject object;
			try {
				object = (JSONObject) missions.get(i);
				JSONArray places = object.getJSONArray("places");
				for (int j = 0; j < places.length(); j++) {
					JSONObject place = places.getJSONObject(j);
					MarkerOptions options = new MarkerOptions();
					options.position(new LatLng(Double.valueOf(place.getString("latitude")), Double
							.valueOf(place.getString("longitude"))));
					options.title(place.getString("name"));
					Marker marker = mMap.addMarker(options);
					MissionBubble bubble = new MissionBubble(marker);
					bubble.init(object.getString("missionID"), missionNames[i], place.getString("placeID"));
					((MissionDrawerAdapter) mDrawerAdapter).addBubble(bubble);
					if (j == 0) {
						mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
								new LatLng(Double.valueOf(place.getString("latitude")), Double.valueOf(place
										.getString("longitude"))), 13));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		mListener.beforeDrawerChanged(mDrawerAdapter);
	}

	private Bitmap getBubbleDMarker(String colorCode, String inflation) {
		int radius = 20 + ((Integer.parseInt(inflation) / 500) + 1) * 5;
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(2 * radius, 2 * radius, conf);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		try {
			paint.setColor(Color.argb(200, Color.red(Color.parseColor(colorCode)),
					Color.green(Color.parseColor(colorCode)), Color.blue(Color.parseColor(colorCode))));
		} catch (Exception e) {
			paint.setColor(Color.BLUE);
		}
		canvas.drawCircle(radius, radius, radius, paint);
		return bmp;
	}

	// Implement HTTPResponseReceiver
	@Override
	public void onSuccess(int api, String result) {
		// Log.i(getClass().getName(), result);
		try {
			switch (api) {
			case APIHelper.GET_CEHCKIN:
				JSONArray checkins = new JSONArray(result);
				updateEmotionMap(checkins);
				break;

			case APIHelper.GET_MISSION:
				JSONArray missions = new JSONArray(result);
				updateMissionMap(missions);
				break;

			case APIHelper.GET_COMPLETED_MISSION:
				// TODO
				break;

			case APIHelper.GET_COMMENT:
				JSONObject comments = new JSONObject(result);
				updateCommentDialog(comments);
				break;
			}
		} catch (JSONException e) {
			Log.i(getClass().getName(), result);
			e.printStackTrace();
		}
	}

	@Override
	public void onError(int resultCode) {
		// TODO Auto-generated method stub
	}
}