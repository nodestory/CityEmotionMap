package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;

import tw.edu.ntu.csie.sed.CEM.model.Bubble;
import tw.edu.ntu.csie.sed.CEM.model.EmotionBubble;
import android.util.Log;

public class ScopeBubbleSelector extends BubbleFilterDecorator {
	private boolean friendOnly;
	private String friendIds;
	private BubbleSelector selector;

	public ScopeBubbleSelector(boolean friendOnly, String friendIds, BubbleSelector selector) {
		this.friendOnly = friendOnly;
		this.friendIds = friendIds;
		this.selector = selector;
	}

	@Override
	public ArrayList<Bubble> select(ArrayList<Bubble> bubbles) {
		Log.i(getClass().getName(), friendIds);
		ArrayList<Bubble> selectedBubbles = new ArrayList<Bubble>(selector.select(bubbles));
		for (Bubble bubble : bubbles) {
			if (isRemovalbe(((EmotionBubble) bubble).getAuthorId())) {
				selectedBubbles.remove(bubble);
			}
		}
		return selectedBubbles;
	}

	private boolean isRemovalbe(String friendId) {
		String[] ids = friendIds.split(";");
		Log.i(getClass().getName(), friendIds);
		if (friendOnly) {
			for (String id : ids) {
				if (friendId.equals(id)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}