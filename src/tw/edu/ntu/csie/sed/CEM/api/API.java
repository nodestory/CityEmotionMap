package tw.edu.ntu.csie.sed.CEM.api;

import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import tw.edu.ntu.csie.sed.CEM.rest.RESTService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public abstract class API {
	private int index;
	private int action;
	private Uri uri;
	private Context context;
	private ResultReceiver receiver;

	public API(Context context, final HTTPResponseReceiver receiver) {
		this.context = context;
		this.receiver = new ResultReceiver(new Handler()) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				if (resultCode == 200) {
					receiver.onSuccess(resultData.getInt(RESTService.REST_API_NAME),
							resultData.getString(RESTService.REST_RESULT));
				} else {
					receiver.onError(resultCode);
				}
			}
		};
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public void setAction(int action) {
		this.action = action;
	}

	abstract void init();

	abstract void prepareParams(String... params);

	public void execute(Bundle params) {
		Intent intent = new Intent(context, RESTService.class);
		intent.setData(uri);
		intent.putExtra(RESTService.EXTRA_API_NAME, index);
		intent.putExtra(RESTService.EXTRA_HTTP_VERB, action);
		intent.putExtra(RESTService.EXTRA_PARAMS, params);
		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER, receiver);
		context.startService(intent);
	}
}