package uk.ac.gla.get2gether;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Event implements Comparable{

	public String id;
	public String locationName;
	public String name;
	// public String start_time;
	public Date startTime;

	public String ownerID;
	public String description;
	public double latitude;
	public double longitude;
	
	public List<Friend> invitedList;
	public String address;
	

	// Map of invited people <id, name> who haven't responded
	// yet to their invitation to this event
	public HashMap<String, String> invitedMap;
	public HashMap<String, String> confirmedMap; // likewise for confirmed

	public Event(String id, String locationName, String name,
			String start_time, String description) {//, String ownerID
		this.id = id;

		int atCursor = locationName.lastIndexOf('@');
		this.address = locationName.substring(0, atCursor - 1);

		int commaCursor = locationName.lastIndexOf(',');
		this.latitude = Double.valueOf(locationName.substring(atCursor + 2,
				commaCursor));
		this.longitude = Double.valueOf(locationName.substring(
				commaCursor + 2, locationName.length()));

		this.name = name.substring(6);
		int newLine = description.indexOf('\n');
		if (newLine != -1) {
			this.locationName = description.substring(0, newLine);
			this.description = description.substring(newLine + 1, description.length());
		}  
		

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			this.startTime = sdf.parse(start_time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		

	}
	
	public boolean isOld() {
		Date now = new Date();
		if (startTime.before(now))
			return true;
		else
			return false;
		
	}

	public String toString() {
		return id + ", " + name + ", " + locationName + ", " + address
				+ ", " + latitude + ", " + longitude + ", "
				+ startTime.toGMTString();
	}

	@Override
	public int compareTo(Object arg0) {
		if (this == arg0)
			return 0;
		else if (this == null)
			return 1;
		else if (arg0 == null)
			return -1;
		else
			return this.startTime.compareTo(((Event) arg0).startTime);
	}

}
