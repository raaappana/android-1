package uk.ac.gla.route2go;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gla.route2go.pathcalc.DumbPath;
import uk.ac.gla.route2go.pathcalc.Edge;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

public class Map extends MapActivity {
	private Projection projection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);
		// Context context = getApplicationContext();
		DumbPath path = new DumbPath(this);
		path.setStart("Scotland Street School, Glasgow", "get on the bike");
		path.setEnd("University of Glasgow, G12 8QQ", "set off foot");

		MapView mapView = (MapView) findViewById(R.id.mapview);

		mapView.setBuiltInZoomControls(true);

		projection = mapView.getProjection();
		System.out.println("Projection set:" + projection);
		mapView.setBuiltInZoomControls(true);

		draw_overlays(path, mapView);
	}

	private void draw_overlays(DumbPath path, MapView mapView) {
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(
				android.R.drawable.btn_star);

		List<Edge> edges = path.getShortestPath();

		RouteOverlay itemizedoverlay = new RouteOverlay(drawable, this, edges);
		for (Edge e : edges) {
			itemizedoverlay.addOverlay(new OverlayItem(e.from.latlng, "y1",
					e.from.comment));
		}
		Edge lastEdge = edges.get(edges.size() - 1);
		itemizedoverlay.addOverlay(new OverlayItem(lastEdge.to.latlng, "y1",
				lastEdge.to.comment));

		mapOverlays.add(itemizedoverlay);
	}

	private class RouteOverlay extends ItemizedOverlay {
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		Context mContext;
		List<Edge> edges; // for drawing lines between the points

		public RouteOverlay(Drawable defaultMarker, Context context,
				List<Edge> edges) {
			super(boundCenterBottom(defaultMarker));
			this.edges = edges;
			mContext = context;
		}

		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			for (Edge e : edges) {
				Paint mPaint = new Paint();
				mPaint.setDither(true);
				mPaint.setColor(Color.RED);
				mPaint.setStrokeJoin(Paint.Join.ROUND);
				mPaint.setStrokeCap(Paint.Cap.ROUND);
				mPaint.setStrokeWidth(2);

				Point p1 = new Point(), p2 = new Point();

				projection.toPixels(e.from.latlng, p1);
				projection.toPixels(e.to.latlng, p2);

				canvas.drawLine(p2.x, p2.y, p1.x, p1.y, mPaint);
			}
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}