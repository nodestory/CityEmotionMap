package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;

import tw.edu.ntu.csie.sed.CEM.model.Bubble;

public abstract class BubbleFilterDecorator implements BubbleSelector {
	protected BubbleSelector selector;

	@Override
	public ArrayList<Bubble> select(ArrayList<Bubble> bubbles) {
		return selector.select(bubbles);
	}
}
