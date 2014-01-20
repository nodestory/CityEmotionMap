package tw.edu.ntu.csie.sed.CEM.model;

import com.google.android.gms.maps.model.Marker;

public class EmotionBubble extends Bubble {
	private String messageId;
	private String authorId;
	private String emotion;
	private String message;
	private String place;
	private int inflation;

	public EmotionBubble(Marker marker) {
		super(marker);
	}

	@Override
	public String getKey() {
		return emotion;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getEmotion() {
		return emotion;
	}

	public void setEmotion(String emotion) {
		this.emotion = emotion;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String placeName) {
		this.place = placeName;
	}

	public void init(String authorId, String messageId, String emotion, String message, String place, int inflation) {
		setAuthorId(authorId);
		setMessageId(messageId);
		setEmotion(emotion);
		setMessage(message);
		setPlace(place);
		setInflation(inflation);
	}

	public String getAuthorId() {
		return authorId;
	}

	public void setAuthorId(String authorId) {
		this.authorId = authorId;
	}

	public int getInflation() {
		return inflation;
	}

	public void setInflation(int inflation) {
		this.inflation = inflation;
	}
}