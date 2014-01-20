package tw.edu.ntu.csie.sed.CEM.rest;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class APIHelper {
	public static final int POST_LOGIN = 0;
	public static final int POST_ADD_CHECKIN = 1; 
	public static final int POST_ADD_COMMENT = 2; 
	public static final int POST_COMPLETE_MISSION = 3; 
	public static final int GET_CEHCKIN = 4;
	public static final int GET_MISSION = 5; 
	public static final int GET_COMPLETED_MISSION = 6; 
	public static final int GET_COMMENT = 7;
	public static final int GET_PAST_EMOTION_COMPOSITION = 8;
	public static final int GET_PAST_CHECKIN = 9;
	
	private Context mContext;
	private ResultReceiver mResultReceiver;

	public APIHelper(Context context, final HTTPResponseReceiver receiver) {
		mContext = context;
		mResultReceiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode == 200) {
					receiver.onSuccess(resultData.getInt(RESTService.REST_API_NAME), resultData.getString(RESTService.REST_RESULT));
				} else {
					receiver.onError(resultCode);
				}
			}
		};
	}

	public void login(String fbId, String name) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/loginAction.php"));
		Bundle params = new Bundle();
		params.putString("FBID", fbId);
		params.putString("name", name);
		intent.putExtra(RESTService.EXTRA_API_NAME, POST_LOGIN);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_HTTP_VERB, RESTService.POST);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}

	public void checkin(String latitude, String longitude, String bubbleID, String dateTime, String message,
			String emotionType, String color, String userID) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/checkinAction.php"));
		Bundle params = new Bundle();
		params.putString("latitude", latitude);
		params.putString("longitude", longitude);
		params.putString("BubbleID", bubbleID);
		params.putString("date", dateTime);
		params.putString("message", message);
		params.putString("emotionType", emotionType);
		params.putString("color", color);
		params.putString("userID", userID);
		intent.putExtra(RESTService.EXTRA_API_NAME, POST_ADD_CHECKIN);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_HTTP_VERB, RESTService.POST);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}

	public void addComment(String bubbleId, String message, String userId, String inflation, String timestamp) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/addCommentAction.php"));
		Bundle params = new Bundle();
		params.putString("BubbleID", bubbleId);
		params.putString("message", message);
		params.putString("userID", userId);
		params.putString("inflation", inflation);
		params.putString("timestamp", timestamp);
		intent.putExtra(RESTService.EXTRA_API_NAME, POST_ADD_COMMENT);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_HTTP_VERB, RESTService.POST);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}

	public void completeMission() {

	}

	// GET
	public void getCheckins(String latitude, String longitude, String range) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/getCheckins.php"));
		Bundle params = new Bundle();
		params.putString("latitude", latitude);
		params.putString("longitude", longitude);
		params.putString("range", range);
		intent.putExtra(RESTService.EXTRA_API_NAME, GET_CEHCKIN);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}

	public void getMissionList() {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/getMissionList.php"));
		intent.putExtra(RESTService.EXTRA_API_NAME, GET_MISSION);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}
	
	public void getCompletedMissions(String userId) {
		Intent intent = new Intent(mContext, RESTService.class);
		Bundle params = new Bundle();
		params.putString("userID", userId);
		intent.setData(Uri.parse("http://140.112.20.121/getCompletedMissions.php"));
		intent.putExtra(RESTService.EXTRA_API_NAME, GET_COMPLETED_MISSION);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}
	
	public void getCommments(String buubleId) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/getComments.php"));
		Bundle params = new Bundle();
		params.putString("BubbleID", buubleId);
		intent.putExtra(RESTService.EXTRA_API_NAME, GET_COMMENT);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}
	
	public void getPastEmotionComposition(String userId, String range) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/getComposition.php"));
		Bundle params = new Bundle();
		params.putString("userID", userId);
		params.putString("constraint", range);
		intent.putExtra(RESTService.EXTRA_API_NAME, GET_PAST_EMOTION_COMPOSITION);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}
	
	public void getPastCheckins(String userId, String emotion, String startTime) {
		Intent intent = new Intent(mContext, RESTService.class);
		intent.setData(Uri.parse("http://140.112.20.121/getPastCheckins.php"));
		Bundle params = new Bundle();
		params.putString("userID", userId);
		params.putString("emotionType", emotion);
		params.putString("constraint", startTime);
		intent.putExtra(RESTService.EXTRA_API_NAME, GET_PAST_CHECKIN);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, mResultReceiver);
		mContext.startService(intent);
	}
}