package tw.edu.ntu.csie.sed.CEM.api;

import tw.edu.ntu.csie.sed.CEM.rest.HTTPResponseReceiver;
import android.content.Context;
import android.os.Bundle;

public class Login extends API {

	public Login(Context context, HTTPResponseReceiver receiver) {
		super(context, receiver);
	}

	@Override
	void init() {
		// TODO Auto-generated method stub

	}

	@Override
	void prepareParams(String... params) {
		Bundle bundle = new Bundle();
		bundle.putString("BubbleID", params[0]);
	}

}
