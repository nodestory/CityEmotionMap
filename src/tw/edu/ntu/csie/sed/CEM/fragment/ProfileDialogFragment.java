package tw.edu.ntu.csie.sed.CEM.fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.edu.ntu.csie.sed.CEM.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProfileDialogFragment extends DialogFragment {
	private ListView mListView;
	private ProgressBar mProgressBar;

	private static JSONArray mPastCheckins;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_profile, null);
		builder.setView(view);
		builder.setTitle("情緒檔案");
		builder.setPositiveButton("確認", null);
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		AlertDialog dialog = builder.create();
		mListView = (ListView) view.findViewById(R.id.listview_message);
		// mListView.setVisibility(View.INVISIBLE);
		mListView.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				return mPastCheckins.length();
			}

			@Override
			public Object getItem(int position) {
				return position;
			}

			@Override
			public long getItemId(int id) {
				return id;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				JSONObject object;
				String message = "";
				String color = "#F3ADCA";
				try {
					object = mPastCheckins.getJSONObject(position);
					message = object.getString("message");
					color = object.getString("color");
					Log.i(getClass().getName(), color);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				ViewHolder holder;
				if (convertView == null) {
					convertView = getActivity().getLayoutInflater().inflate(R.layout.listitem_past_checkin,
							null);
					holder = new ViewHolder();
					holder.text = (TextView) convertView.findViewById(R.id.textview_message);
//					holder.text.setBackgroundColor(Color.parseColor(color));
					holder.text.setText(message);
					convertView.setBackgroundColor(Color.parseColor(color));
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				return convertView;
			}
		});
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);
		return dialog;
	}

	public static ProfileDialogFragment newInstance(JSONArray checkins) {
		mPastCheckins = checkins;
		ProfileDialogFragment fragment = new ProfileDialogFragment();
		return fragment;
	}

	public void update(String[] messages) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, messages);
		mListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		mListView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
	}

	static class ViewHolder {
		TextView text;
	}
}
