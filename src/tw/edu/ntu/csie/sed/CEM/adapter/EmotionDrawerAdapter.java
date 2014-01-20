package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tw.edu.ntu.csie.sed.CEM.R;
import tw.edu.ntu.csie.sed.CEM.model.Bubble;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

public class EmotionDrawerAdapter extends BaseAdapter {
	private static final int EMOTION = 0;
	private static final int SCOPE = 1;

	private String[] names;
	private TypedArray icons;

	private LayoutInflater inflater;
	private BubbleSelector selector;

	private SharedPreferences prefs;
	private Map<String, ArrayList<Bubble>> bubbles = new HashMap<String, ArrayList<Bubble>>();
	private ArrayList<Bubble> test = new ArrayList<Bubble>();

	private boolean mFriendOnly;

	public EmotionDrawerAdapter(Activity activity) {
		icons = activity.getResources().obtainTypedArray(R.array.emotion_icon_array);
		names = activity.getResources().getStringArray(R.array.emotion_name_array);
		inflater = activity.getLayoutInflater();
		selector = new EmotionBubbleSelector(names);
		prefs = activity.getSharedPreferences("CEM", 0);
	}

	@Override
	public int getCount() {
		return names.length + 1;
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
		ViewHolder holder;
		if (convertView == null) {
			if (position == names.length) {
				convertView = inflater.inflate(R.layout.listitem_scope, null);
				convertView.setTag(SCOPE);
				holder = new ViewHolder(position + 1);
				holder.text = (CheckedTextView) convertView.findViewById(R.id.textview_text);
				convertView.setTag(holder);
			} else {
				convertView = inflater.inflate(R.layout.listitem_emotion_drawer, null);
				convertView.setTag(EMOTION);
				holder = new ViewHolder(position + 1);
				holder.icon = (ImageView) convertView.findViewById(R.id.imageview_icon);
				holder.text = (CheckedTextView) convertView.findViewById(R.id.textview_text);
				holder.icon.setImageResource(icons.getResourceId(position, 0));
				holder.text.setText(names[position]);
				holder.text.setTag(position);
				convertView.setTag(holder);
			}
			convertView.setOnClickListener(new EmotionListItemOnClickListener());
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		return convertView;
	}

	public void addBubble(Bubble bubble) {
		if (!bubbles.containsKey(bubble.getKey())) {
			bubbles.put(bubble.getKey(), new ArrayList<Bubble>());
		}
		bubbles.get(bubble.getKey()).add(bubble);
		test.add(bubble);
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

		for (Bubble bubble : test) {
			bubble.getMarker().remove();
		}
		test.clear();
	}

	static class ViewHolder {
		int position;
		ImageView icon;
		CheckedTextView text;

		public ViewHolder(int position) {
			this.position = position;
		}

		public int getType() {
			return position;
		}

		public CheckedTextView getCheckedTextView() {
			return text;
		}
	}

	class EmotionListItemOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			CheckedTextView textView = holder.getCheckedTextView();
			boolean isChecked = textView.isChecked();
			textView.setChecked(!isChecked);
			for (Bubble bubble : test) {
				bubble.getMarker().setVisible(false);
			}
			if (holder.position <= names.length) {
				((EmotionBubbleSelector) selector).setEmotionEnabled(textView.getText().toString(),
						!isChecked);
			} else {
				mFriendOnly = !isChecked;
			}
			BubbleSelector advancedFilterBubbleSelector = new ScopeBubbleSelector(mFriendOnly,
					prefs.getString("friend_list", ""), selector);
			Log.i(getClass().getName(), prefs.getString("friend_list", ""));
			for (Bubble bubble : advancedFilterBubbleSelector.select(test)) {
				bubble.getMarker().setVisible(true);
			}
		}
	}
}