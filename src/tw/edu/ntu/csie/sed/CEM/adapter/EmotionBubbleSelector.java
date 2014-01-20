package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tw.edu.ntu.csie.sed.CEM.model.Bubble;
import tw.edu.ntu.csie.sed.CEM.model.EmotionBubble;

public class EmotionBubbleSelector implements BubbleSelector {
	private String[] emotions;
	private Map<String, Boolean> emotionStatus = new HashMap<String, Boolean>();

	public EmotionBubbleSelector(String[] emotions) {
		this.emotions = emotions;
		for (String emotion : emotions) {
			emotionStatus.put(emotion, true);
		}
	}

	public void setEmotionEnabled(String emotion, boolean selected) {
		emotionStatus.put(emotion, selected);
	}

	@Override
	public ArrayList<Bubble> select(ArrayList<Bubble> bubbles) {
		ArrayList<Bubble> selectedBubbles = new ArrayList<Bubble>(bubbles);
		for (Bubble bubble : bubbles) {
			int type = Integer.parseInt(((EmotionBubble) bubble).getEmotion()) - 1;
			if (type < 0) type = 0;
			if (!emotionStatus.get(emotions[type])) {
				selectedBubbles.remove(bubble);
			}
		}
		return selectedBubbles;
	}
}
