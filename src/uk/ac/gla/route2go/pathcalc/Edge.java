package uk.ac.gla.route2go.pathcalc;

import com.google.android.maps.GeoPoint;

public class Edge {
	public Point from, to;
	public int time;
	
	Edge(Point from, Point to, int time, String comment) {
		this.from = from; this.to = to;
		this.time = time;
	}
}
