package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.model.Bubble;
import tw.edu.ntu.csie.sed.CEM.model.MissionBubble;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MissionDrawerAdapter extends BaseAdapter {
	private String[] names;
	private LayoutInflater inflater;
//	private BubbleSelector selector;
	private Builder builder;
	private SharedPreferences prefs;

	private Map<String, ArrayList<Bubble>> bubbles = new HashMap<String, ArrayList<Bubble>>();

	private ViewHolder prevSelectedMission = null;
	private ImageButton clickedStartButton;

	public MissionDrawerAdapter(Activity activity, String[] missionNames) {
		names = missionNames;
		inflater = activity.getLayoutInflater();
		builder = new AlertDialog.Builder(activity);
		prefs = activity.getSharedPreferences("CEM", 0);
		for (String mission : missionNames) {
			bubbles.put(mission, new ArrayList<Bubble>());
		}
	}

	@Override
	public int getCount() {
		return names.length;
	}

	@Override
	public Object getItem(int position) {
		return names[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_mission_drawer, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.imageview_icon);
			holder.text = (TextView) convertView.findViewById(R.id.textview_text);
			holder.text.setText(names[position]);
			holder.button = (ImageButton) convertView.findViewById(R.id.button_start);
			holder.button.setTag(holder.text.getText().toString());
			holder.button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					clickedStartButton = (ImageButton) v;
					builder.setTitle("開啟任務");
					builder.setMessage("確定要開始挑戰 " + holder.text.getText().toString() + " 嗎？");
					builder.setPositiveButton("確認", new DialogOnClickListener());
					builder.setNegativeButton("取消", new DialogOnClickListener());
					builder.show();
				}
			});
			convertView.setTag(holder);
			convertView.setOnClickListener(new DrawerListItemOnClickListener());
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		return convertView;
	}

	public void addBubble(Bubble bubble) {
		Log.i(getClass().getName(), bubble.getKey());
		if (!bubbles.containsKey(bubble.getKey())) {
			bubbles.put(bubble.getKey(), new ArrayList<Bubble>());
		}
		bubbles.get(bubble.getKey()).add(bubble);
	}

	public void reset() {
		for (int i = 0; i < names.length; i++) {
			if (bubbles.containsKey(names[i])) {
				for (Bubble bubble : bubbles.get(names[i])) {
					bubble.getMarker().remove();
				}
			}
		}
		bubbles.clear();
	}

	static class ViewHolder {
		ImageView icon;
		TextView text;
		ImageButton button;

		public TextView getTextView() {
			return text;
		}
	}

	class DrawerListItemOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (prevSelectedMission != null) {
				prevSelectedMission.text.setTextColor(Color.parseColor("#FFFFFF"));
				Log.i(getClass().getName(), prevSelectedMission.text.getText().toString());
				for (Bubble bubble : bubbles.get(prevSelectedMission.text.getText().toString())) {
					bubble.setVisible(false);
				}
			}
			ViewHolder holder = (ViewHolder) v.getTag();
			holder.text.setTextColor(Color.parseColor("#27B290"));
			for (Bubble bubble : bubbles.get(holder.text.getText().toString())) {
				bubble.setVisible(true);
			}
			prevSelectedMission = holder;
		}
	}

	class DialogOnClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				clickedStartButton.setImageResource(R.drawable.ic_unlock);
				clickedStartButton.setClickable(false);
				SharedPreferences.Editor editor = prefs.edit();
				String placeIds = "";
				String missionId = "";
				for (Bubble bubble : bubbles.get((String) clickedStartButton.getTag())) {
					missionId = ((MissionBubble) bubble).getMissionId();
					placeIds += ";" + ((MissionBubble) bubble).getPlaceId();
				}
				editor.putString(missionId, placeIds);
				if (prefs.contains("missions")) {
					editor.putString("missions", prefs.getString("mission", "") + ";" + missionId);
				} else {
					editor.putString("missions", missionId);
				}
				Log.i(getClass().getName(), prefs.getString("mission", ""));
				editor.commit();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				dialog.dismiss();
				break;
			}
		}
	}
}