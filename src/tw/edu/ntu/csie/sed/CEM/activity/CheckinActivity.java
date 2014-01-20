package tw.edu.ntu.csie.sed.CEM.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener;

import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.rest.APIHelper;
import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

public class CheckinActivity extends SherlockActivity implements OnClickListener, HTTPResponseReceiver {
	private static final String TAG = "CheckinActivity";

	private static final int SELECT_PLACE_CODE = 0;

	// UI elements
	private Context mContext;
	private ActionBar mActionBar;
	private LinearLayout mBackground;
	private Spinner mEmotionSpinner;
	private LinearLayout mColorCard;
	private EditText mMessagEditText;
	private LinearLayout mPlaceCard;
	private TextView mPlaceText;
	private Spinner mScopeSpinner;

	private String mEmotion;
	private int mColor = 1;
	private String mMessage;
	private String mPlaceId;
	private String mPlace;
	private Double mLatitude;
	private Double mLongitude;
	private int mScope;

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String POST_ACTION_PATH = "me/feed";
	private static final String PENDING_POST_KEY = "isPendingPost";
	private boolean mPendingPost;
	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state, final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private APIHelper mHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_checkin);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mBackground = (LinearLayout) findViewById(R.id.background);
		mEmotionSpinner = (Spinner) findViewById(R.id.spinner_emotion);
		mEmotionSpinner.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(
						R.array.emotion_name_array)));
		mColorCard = (LinearLayout) findViewById(R.id.card_color);
		mColorCard.setOnClickListener(this);
		mMessagEditText = (EditText) findViewById(R.id.text_message);
		mPlaceCard = (LinearLayout) findViewById(R.id.card_place);
		mPlaceCard.setOnClickListener(this);
		mPlaceText = (TextView) findViewById(R.id.text_place);

		mScopeSpinner = (Spinner) findViewById(R.id.spinner_scope);
		mScopeSpinner.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(
						R.array.scope_array)));

		if (savedInstanceState != null) {
			mPendingPost = savedInstanceState.getBoolean(PENDING_POST_KEY, false);
		}
		mHelper = new APIHelper(this, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == SherlockActivity.RESULT_OK) {
			switch (requestCode) {
			case SELECT_PLACE_CODE:
				mPlace = (String) data.getCharSequenceExtra("placeName");
				mPlaceId = (String) data.getCharSequenceExtra("placeId");
				mLatitude = data.getDoubleExtra("latitude", 120);
				mLongitude = data.getDoubleExtra("longitude", 25);
				mPlaceText.setText(mPlace);
				break;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
		bundle.putBoolean(PENDING_POST_KEY, mPendingPost);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_ckeckin, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_share:
			mEmotion = String.valueOf(mEmotionSpinner.getSelectedItemPosition() + 1);
			mMessage = mMessagEditText.getText().toString();
			mScope = mScopeSpinner.getSelectedItemPosition();
//			TODO
			SharedPreferences prefs = getSharedPreferences("CEM", 0);
			SharedPreferences.Editor editor = prefs.edit();
			String missions = prefs.getString("missions", "");
			if (missions.length() > 0) {
				ArrayList<String> ongoingMissionIds = new ArrayList<String>();
				Collections.addAll(ongoingMissionIds, missions.split(";"));
				for (String missionId : ongoingMissionIds) {
					ArrayList<String> placeIds = new ArrayList<String>();
					Collections.addAll(placeIds, prefs.getString(missionId, "").split(";"));
					for (int i = 0; i < placeIds.size(); i++) {
						if (mPlaceId.equals(placeIds.get(i))) {
							placeIds.remove(i);
							String newPlaceIds = "";
							for (String s : placeIds) {
								newPlaceIds += s + ";";
							}
							editor.putString(missionId, newPlaceIds);
						}
					}
				}
			}
			postToFacebook();
			Intent intent = new Intent();
			setResult(Activity.RESULT_OK, intent);
			finish();
			break;
		case R.id.menuitem_cancel:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startPickerActivity(Uri data, int requestCode) {
		Intent intent = new Intent();
		intent.setData(data);
		intent.setClass(this, SelectActivity.class);
		startActivityForResult(intent, requestCode);
	}

	protected void onSessionStateChange(final Session session, SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				if (mPendingPost) {
					postToFacebook();
				}
			} else {
				Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {
							if (user != null) {
								SharedPreferences prefs = getSharedPreferences("CEM", 0);
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString("fb_id", user.getId());
								editor.putString("fb_name", user.getName());
								editor.commit();
							}
						}
						if (response.getError() != null) {
							Toast.makeText(mContext, "Please try again later!", Toast.LENGTH_SHORT).show();
						}
					}
				});
				request.executeAsync();
			}
		}
	}

	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
		for (String string : subset) {
			if (!superset.contains(string)) {
				return false;
			}
		}
		return true;
	}

	private void postToFacebook() {
		mPendingPost = false;
		Session session = Session.getActiveSession();
		if (session != null) {
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				mPendingPost = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this,
						PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			Bundle postParams = new Bundle();
			postParams.putString("message", mMessage);
			postParams.putString("place", mPlaceId);
			if (mScope == 1) {
				// postParams.putString("privacy", "{'value': 'ALL_FRIENDS'}");
			}
			// TODO
			postParams.putString("privacy", "{'value': 'SELF'}");

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
					try {
						Log.i(TAG, graphResponse.getString("id"));
						postToCEM(graphResponse.getString("id").replace("_", ""));
					} catch (JSONException e) {
						Log.i(TAG, "JSON error " + e.getMessage());
					}
					FacebookRequestError error = response.getError();
					if (error != null) {
						Toast.makeText(mContext.getApplicationContext(), error.getErrorMessage(),
								Toast.LENGTH_SHORT).show();
					} else {
					}
				}
			};
			Request request = new Request(session, POST_ACTION_PATH, postParams, HttpMethod.POST, callback);
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}

	private void postToCEM(String messageId) {
		mHelper.checkin(String.valueOf(mLatitude), String.valueOf(mLongitude), messageId,
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN).format(new Date()), mMessage,
				mEmotion, String.format("#%X", mColor), getSharedPreferences("CEM", 0).getString("fb_id", ""));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.card_color:
			ColorPickerDialog dialog = new ColorPickerDialog(this, Color.rgb(225, 184, 3));
			dialog.setOnColorChangedListener(new OnColorChangedListener() {
				@Override
				public void onColorChanged(int color) {
					mColor = color;
				}
			});
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mBackground.setBackgroundColor(mColor);
				}
			});
			dialog.show();
			break;
		case R.id.card_place:
			startPickerActivity(SelectActivity.PLACE_PICKER, SELECT_PLACE_CODE);
			break;
		}
	}

	@Override
	public void onSuccess(int api, String result) {
		Log.i(TAG, result);
	}

	@Override
	public void onError(int resultCode) {
	}
}