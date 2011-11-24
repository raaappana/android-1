package uk.ac.gla.get2gether.pathcalc;

public class Edge {
	public Point from, to;
	public int time;
	
	Edge(Point from, Point to, int time, String comment) {
		this.from = from; this.to = to;
		this.time = time;
	}
}
