package tw.edu.ntu.csie.sed.CEM.model;

import com.google.android.gms.maps.model.Marker;

public class MissionBubble extends Bubble {
	private String missionId;
	private String name;
	private String placeId;

	public MissionBubble(Marker marker) {
		super(marker);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getKey() {
		return name;
	}

	public String getMissionName() {
		return name;
	}

	public void setMissionName(String name) {
		this.name = name;
	}

	public void init(String missionId, String name, String placeId) {
		setMissionId(missionId);
		setMissionName(name);
		setPlaceId(placeId);
	}

	public String getMissionId() {
		return missionId;
	}

	public void setMissionId(String id) {
		this.missionId = id;
	}

	public String getPlaceId() {
		return placeId;
	}

	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
}
