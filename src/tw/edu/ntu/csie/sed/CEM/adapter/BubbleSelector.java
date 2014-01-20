package tw.edu.ntu.csie.sed.CEM.adapter;

import java.util.ArrayList;

import tw.edu.ntu.csie.sed.CEM.model.Bubble;

public interface BubbleSelector {
	ArrayList<Bubble> select(ArrayList<Bubble> bubbles);
}
