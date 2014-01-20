package tw.edu.ntu.csie.sed.CEM.activity;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.fragment.ProfileDialogFragment;
import tw.edu.ntu.csie.sed.CEM.rest.APIHelper;
import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.widget.ProfilePictureView;

public class ProfileActivity extends SherlockFragmentActivity implements HTTPResponseReceiver {
	private Context mContext;

	private TextView mUserNameTextView;
	private ProfilePictureView mProfilePictureView;
	private RadioGroup mScopeGroup;
	private Button mAnalyzeButton;
	private ProgressBar mProgressBar;
	private LinearLayout mChartLayout;
	private GraphicalView mChartView;

	private static int[] COLORS = new int[] { Color.rgb(242, 108, 133), Color.rgb(1, 178, 194),
			Color.rgb(254, 183, 29), Color.rgb(6, 231, 209), Color.rgb(8, 123, 94), Color.rgb(33, 180, 140),
			Color.rgb(128, 92, 157), Color.rgb(227, 225, 29) };
	private CategorySeries mSeries = new CategorySeries("");
	private DefaultRenderer mRenderer = new DefaultRenderer();

	private String mFBId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_profile);

		View customActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar, null);
		getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.black_transparent));
		getSupportActionBar().setCustomView(customActionBarView);

		SharedPreferences prefs = getSharedPreferences("CEM", 0);
		mFBId = prefs.getString("fb_id", "");
		mUserNameTextView = (TextView) findViewById(R.id.textview_username);
		mUserNameTextView.setText(prefs.getString("fb_name", ""));
		mProfilePictureView = (ProfilePictureView) findViewById(R.id.profilepictureview);
		mProfilePictureView.setCropped(true);
		mProfilePictureView.setProfileId(prefs.getString("fb_id", ""));

		mScopeGroup = (RadioGroup) findViewById(R.id.group_scope);
		mAnalyzeButton = (Button) findViewById(R.id.button_analyze);
		mAnalyzeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int range = 1;
				switch (mScopeGroup.getCheckedRadioButtonId()) {
				case R.id.radiobutton_day:
					range = 1;
					break;
				case R.id.radiobutton_week:
					range = 7;
					break;
				case R.id.radiobutton_month:
					range = 30;
					break;

				default:
					break;
				}
				new APIHelper(mContext, getReceiver()).getPastEmotionComposition(mFBId, String.valueOf(range));
				mProgressBar.setVisibility(View.VISIBLE);
			}
		});

		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mProgressBar.setVisibility(View.GONE);

		mChartLayout = (LinearLayout) findViewById(R.id.chart);
		mRenderer.setAntialiasing(true);
		mRenderer.setClickEnabled(true);
		mRenderer.setLabelsTextSize(24);
		mRenderer.setPanEnabled(true);
		mRenderer.setShowLabels(true);
		mRenderer.setShowLegend(false);
		mRenderer.setStartAngle(180);
		mRenderer.setZoomRate(20);
		mRenderer.setPanEnabled(false);
	}

	private HTTPResponseReceiver getReceiver() {
		return this;
	}

	@Override
	public void onSuccess(int api, String result) {
		Log.i(getClass().getName(), result);
		switch (api) {
		case APIHelper.GET_PAST_EMOTION_COMPOSITION:
			mSeries.clear();
			mRenderer.removeAllRenderers();
			String[] emotions = getResources().getStringArray(R.array.emotion_name_array);
			JSONObject composition = null;
			try {
				composition = new JSONObject(result);
				for (int i = 0; i < emotions.length; i++) {
					mSeries.add(emotions[i], Double.parseDouble(composition.getString(String.valueOf(i + 1))));
					SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
					renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
					mRenderer.addSeriesRenderer(renderer);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
			mChartView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
					if (seriesSelection != null) {
						for (int i = 0; i < mSeries.getItemCount(); i++) {
							mRenderer.getSeriesRendererAt(i).setHighlighted(
									i == seriesSelection.getPointIndex());
						}
						mChartView.repaint();
						new APIHelper(mContext, getReceiver()).getPastCheckins(mFBId,
								String.valueOf(seriesSelection.getPointIndex() + 1), "1");
					}
				}
			});
			mChartLayout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			mChartView.repaint();
			mProgressBar.setVisibility(View.GONE);
			mChartLayout.setVisibility(View.VISIBLE);
			break;

		case APIHelper.GET_PAST_CHECKIN:
			try {
				JSONArray checkins = new JSONArray(result);
				ProfileDialogFragment dialog = ProfileDialogFragment.newInstance(checkins);
				dialog.show(getSupportFragmentManager(), "profile");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	public void onError(int resultCode) {
		// TODO Auto-generated method stub
	}
}