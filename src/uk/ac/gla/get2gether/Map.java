package uk.ac.gla.get2gether;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.idansof.otp.client.Itinerary;
import org.idansof.otp.client.Leg;
import org.idansof.otp.client.Location;
import org.idansof.otp.client.PlanRequest;
import org.idansof.otp.client.Planner;
import org.idansof.otp.client.TripPlan;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.Projection;
import org.xmlpull.v1.XmlPullParserException;

import uk.ac.gla.get2gether.pathcalc.DumbPath;
import uk.ac.gla.get2gether.pathcalc.Edge;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;

public class Map extends MapActivity {
	private Projection projection;
	private DumbPath path;
	private List<Location> geometry;
	private Thread router_t;
	private ProgressDialog mSpinner;
	private Object lock = new Object();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.map);
		// Context context = getApplicationContext();
		path = new DumbPath(this);
		// path.setStart("Scotland Street School, Glasgow", "get on the bike");
		// path.setEnd("University of Glasgow, G12 8QQ", "set off foot");
		path.setStart("Laurelhurst Park, Portland", "get on the bike");
		path.setEnd("Mount Tabor City Park, Portland", "set off foot");

		Runnable router = new Runnable() {
			public void run() {
				Planner planner = new Planner("spurga.numeris.lt:8080",
						"opentripplanner-api-webapp/ws/plan");
				PlanRequest req = new PlanRequest();
				req.setFrom(new Location(path.getStart().comment, path
						.getStart().latlng.getLatitude(),
						path.getStart().latlng.getLongitude()));
				req.setTo(new Location(path.getEnd().comment,
						path.getEnd().latlng.getLatitude(),
						path.getEnd().latlng.getLongitude()));
				req.setDate(new Date(System.currentTimeMillis()));

				TripPlan plan = null;

				try {
					plan = planner.generatePlan(req).getTripPlan();
					List<Itinerary> its = plan.getItineraries();
					geometry = new ArrayList<Location>();
					for (Leg l : its.get(0).getLegs()) {
						// nodes.add(new OverlayItem(new
						// GeoPoint(l.getFrom().getLatitude(),
						// l.getFrom().getLongitude()), "from",
						// l.getFrom().getAddress()));
						geometry.addAll(l.getGeometry());
					}
					// Leg l =
					// its.get(0).getLegs().get(its.get(0).getLegs().size()-1);
					// nodes.add(new OverlayItem(new
					// GeoPoint(l.getTo().getLatitude(),
					// l.getTo().getLongitude()), "to",
					// l.getTo().getAddress()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (java.text.ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				onRouteCalcFinished();
			}
		};

		router_t = new Thread(router);

		MapView mapView = (MapView) findViewById(R.id.mapview);

		mSpinner = new ProgressDialog(mapView.getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading..");

		mapView.setMapViewMode(MapViewMode.MAPNIK_TILE_DOWNLOAD);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setCenter(path.getStart().latlng);
		projection = mapView.getProjection();
		// System.out.println("Projection set:" + projection);

		mapView.setBuiltInZoomControls(true);

	}
	
	private void draw_overlays(DumbPath path, MapView mapView) {
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable dest_icon = this.getResources().getDrawable(
				R.drawable.ic_menu_flag_red);

		// dest_icon.
		List<Edge> edges = path.getShortestPath();

		RouteOverlay itemizedoverlay = new RouteOverlay(dest_icon, this, edges);
		for (Edge e : edges) {
			itemizedoverlay.addOverlay(new OverlayItem(e.from.latlng, "y1",
					e.from.comment));
		}
		Edge lastEdge = edges.get(edges.size() - 1);
		itemizedoverlay.addOverlay(new OverlayItem(lastEdge.to.latlng, "y1",
				lastEdge.to.comment));
		/*
		 * if(nodes != null){ for(OverlayItem oi : nodes)
		 * itemizedoverlay.addOverlay(oi); }
		 */

		mapOverlays.add(itemizedoverlay);
	}

	private class RouteOverlay extends ItemizedOverlay<OverlayItem> {
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
		public void drawOverlayBitmap(Canvas canvas, Point drawPosition,
				Projection projection, byte drawZoomLevel) {
			super.drawOverlayBitmap(canvas, drawPosition, projection,
					drawZoomLevel);

			Paint mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(Color.RED);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.SQUARE);
			mPaint.setStrokeWidth(2);

			for (int i = 0; i + 1 < geometry.size(); i++) {
				Point p1 = new Point(), p2 = new Point();
				projection.toPixels(new GeoPoint(geometry.get(i).getLatitude(),
						geometry.get(i).getLongitude()), p1);
				projection
						.toPixels(new GeoPoint(geometry.get(i + 1)
								.getLatitude(), geometry.get(i + 1)
								.getLongitude()), p2);
				canvas.drawLine(p2.x, p2.y, p1.x, p1.y, mPaint);
			}

		}
	}

	/**
	 * Invoked at the time to create the menu
	 * 
	 * @param menu
	 *            is the menu to create
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}

	/**
	 * Invoked when preparing to display the menu
	 * 
	 * @param menu
	 *            is the menu to prepare
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem calcrouteItem = menu.findItem(R.id.calc_route);

		return super.onPrepareOptionsMenu(menu);
	}

	
	private void onRouteCalcFinished() {
		mSpinner.dismiss();
		draw_overlays(path, (MapView) findViewById(R.id.mapview));
	}
	
	/**
	 * Invoked when a menu item has been selected
	 * 
	 * @param item
	 *            is the selected menu items
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.calc_route:
			router_t.start();
			mSpinner.show();
			break;
		default:
			return false;
		}
		return true;
	}

}
