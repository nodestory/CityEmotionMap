package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;

import tw.edu.ntu.csie.sed.CEM.model.Bubble;
import tw.edu.ntu.csie.sed.CEM.model.MissionBubble;

public class MissionBubbleSelector implements BubbleSelector {
	private String mission;

	public MissionBubbleSelector(String mission) {
		this.mission = mission;
	}

	@Override
	public ArrayList<Bubble> select(ArrayList<Bubble> bubbles) {
		ArrayList<Bubble> selectedBubbles = new ArrayList<Bubble>(bubbles);
		for (Bubble bubble : bubbles) {
			if (!((MissionBubble) bubble).getMissionName().equals(mission)) {
				selectedBubbles.remove(bubble);
			}
		}
		return selectedBubbles;
	}
}