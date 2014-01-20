package tw.edu.ntu.csie.sed.CEM.rest;

public interface HTTPResponseReceiver {
	void onSuccess(int api, String result);

	void onError(int resultCode);
}