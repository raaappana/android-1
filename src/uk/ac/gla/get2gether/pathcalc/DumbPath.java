package uk.ac.gla.get2gether.pathcalc;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class DumbPath {
	Context context;
	Point start, end;
	List<Edge> edges;
	int shortest_time;
	
	public List<Edge> getShortestPath() {
		// always go through home
		Point home = new Point(55866706, -4260432, "M&P home");
		
		edges = new ArrayList<Edge>();
		edges.add(new Edge(start, home, 600, "bike"));
		edges.add(new Edge(home, end, 400, "foot"));
		shortest_time = 1000;
		
		return edges;
	}

	public int getShortestTime() {
		return shortest_time;
	}

	public void setStart(String addr, String comment) {
		start = new Point(context, addr, comment);
	}

	public void setEnd(String addr, String comment) {
		end = new Point(context, addr, comment);
	}

	public Point getStart() { return start; }
	public Point getEnd() { return end; }
	
	public DumbPath(Context c) {
		context = c;
	}
}
