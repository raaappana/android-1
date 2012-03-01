package uk.ac.gla.get2gether;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.idansof.otp.client.Itinerary;
import org.idansof.otp.client.Leg;
import org.idansof.otp.client.Location;
import org.idansof.otp.client.PlanRequest;
import org.idansof.otp.client.Planner;
import org.idansof.otp.client.TripPlan;
import org.idansof.otp.client.WalkStep;
import org.mapsforge.android.maps.ArrayCircleOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.ItemizedOverlay;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.OverlayCircle;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.Projection;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import uk.ac.gla.get2gether.map.OTP;
import uk.ac.gla.get2gether.map.WalkSteps;

public class Map extends MapActivity implements Observer {

	// Route calculation
	// private DumbPath path;
	private ProgressDialog mSpinner;
	public static Itinerary itinerary = null; // Android Tutorial recommended :)
	private boolean routeCalcDone = false;

	private View infoView;

	// Current location stuff
	private LocationManager locationManager;
	private G2GLocationListener locationListener;
	ArrayCircleOverlay circleOverlay;
	G2GItemizedOverlay itemizedOverlay;
	OverlayCircle overlayCircle;
	List<OverlayCircle> friendsLocations;
	OverlayItem overlayItem;
	Paint circleOverlayFill;
	private Paint circleOverlayOutline;
	android.location.Location currentLocation = null; // coupled to listener!
	private Location start, end;
	private RouteOverlay itemizedoverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventChat ec = new EventChat(this);
		System.out.println(ec);
		setContentView(R.layout.map);
		// Context context = getApplicationContext();
		MapView mapView = (MapView) findViewById(R.id.mapview);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mSpinner = new ProgressDialog(mapView.getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading..");

		if (currentLocation != null)
			start = new Location(currentLocation.getProvider(),
					currentLocation.getLatitude(),
					currentLocation.getLongitude());
		else
			start = new Location("fallback starting point", 55.866521,
					-4.261803);

		mapView.setMapViewMode(MapViewMode.MAPNIK_TILE_DOWNLOAD);
		// mapView.mapView.setMapViewMode(MapViewMode.CANVAS_RENDERER);
		// mapView.setMapFile("/sdcard/great_britain-0.2.4.map");
		mapView.setBuiltInZoomControls(true);

		// Center the map on Glasgow
		GeoPoint gla = new GeoPoint(55.866521, -4.261803);
		mapView.getController().setCenter(gla);
		// projection = mapView.getProjection();

		mapView.setBuiltInZoomControls(true);
		// infoView = (LocationView)findViewById(R.id.location_view);

		// Current location marker
		itemizedOverlay = new G2GItemizedOverlay(null, this);
		overlayItem = new OverlayItem();
		overlayItem.setMarker(ItemizedOverlay.boundCenter(getResources()
				.getDrawable(R.drawable.my_location)));
		itemizedOverlay.addItem(overlayItem);
		mapView.getOverlays().add(itemizedOverlay);

		// Current location accuracy radius
		circleOverlayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		circleOverlayFill.setStyle(Paint.Style.FILL);
		circleOverlayFill.setColor(Color.YELLOW);
		circleOverlayFill.setAlpha(48);

		circleOverlayOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		circleOverlayOutline.setStyle(Paint.Style.STROKE);
		circleOverlayOutline.setColor(Color.BLUE);
		circleOverlayOutline.setAlpha(128);
		circleOverlayOutline.setStrokeWidth(4);

		circleOverlay = new ArrayCircleOverlay(this.circleOverlayFill,
				this.circleOverlayOutline, this);

		overlayCircle = new OverlayCircle();
		friendsLocations = new ArrayList<OverlayCircle>();
		circleOverlay.addCircle(this.overlayCircle);
		mapView.getOverlays().add(this.circleOverlay);

		// Get location service
		for (String provider : this.locationManager.getProviders(true)) {
			android.location.Location cursor = this.locationManager
					.getLastKnownLocation(provider);
			if (currentLocation == null || cursor == null
					|| cursor.getAccuracy() < currentLocation.getAccuracy()) {
				currentLocation = cursor;
			}
		}
		if (currentLocation != null) {
			GeoPoint point = new GeoPoint(currentLocation.getLatitude(),
					currentLocation.getLongitude());
			mapView.getController().setCenter(point); // re-center if possible
			showToast("Last location acquired!");
			overlayCircle.setCircleData(point, currentLocation.getAccuracy());
			circleOverlayFill.setColor(Color.YELLOW);
			overlayItem.setPoint(point);
			circleOverlay.requestRedraw();
			itemizedOverlay.requestRedraw();
		} else {
			showToast("Last location unknown, sorry!");
		}

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		String bestProvider = locationManager.getBestProvider(criteria, true);
		if (bestProvider == null) {
			showToast("Location service not available");
			return;
		} else {
			locationListener = new G2GLocationListener(this);
			// locationListener.setCenterAtFirstFix(centerAtFirstFix);
			locationManager.requestLocationUpdates(bestProvider, 1000, 0,
					locationListener);
		}
		/*
		final String address = this.getIntent().getExtras()
				.getString("address");
		
		new Thread(new Runnable() {
			public void run() {

				if (address != null) {
					System.err.println(address);
					Location d = OTP.geocode(address).get(0);
					GeoPoint p = new GeoPoint(d.getLatitude(), d.getLongitude());
					setDestination(p);
					startRouting();
				}

			}
		}).start();
*/
	}

	void launchDestinationDialog(final GeoPoint p) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Navigate to the chosen location?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								setDestination(p);
								startRouting();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		runOnUiThread(new Runnable() {
			public void run() {
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	private void setDestination(GeoPoint p) {
		end = new Location("Destination", p.getLatitude(), p.getLongitude());
	}

	private void startRouting() {
		mSpinner.show();
		OTP.route(start, end, new Date(System.currentTimeMillis()), this);
	}

	public void update(Observable caller, Object ob) {
		mSpinner.dismiss();
		if (ob instanceof List<?>) {
			if (((List<?>) ob).size() == 0) {
				showToast("Sorry, no route found ;(");
				return;
			}
			Object o = ((List<?>) ob).get(0);
			if (o instanceof Itinerary)
				itinerary = (Itinerary) o;
		}
		SharedPreferences settings = getSharedPreferences("get2gether", 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putInt("startLatitude", (int) (start.getLatitude() * 1e6));
		editor.putInt("startLongitude", (int) (start.getLongitude() * 1e6));
		editor.putInt("endLatitude", (int) (end.getLatitude() * 1e6));
		editor.putInt("endLongitude", (int) (end.getLongitude() * 1e6));
		editor.putString("endTime", itinerary.getEndTime().toString());
		editor.commit();

		routeCalcDone = true;
		draw_overlays((MapView) findViewById(R.id.mapview));
	}

	private void draw_overlays(MapView mapView) {
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable dest_icon = this.getResources().getDrawable(
				R.drawable.ic_menu_flag_red);

		Drawable src_icon = this.getResources().getDrawable(
				R.drawable.ic_menu_flag_green);

		// List<Edge> edges = path.getShortestPath();

		itemizedoverlay = new RouteOverlay(dest_icon, this);
		ItemizedOverlay.boundCenterBottom(src_icon);
		/*
		 * for (Edge e : edges) { itemizedoverlay.addOverlay(new
		 * OverlayItem(e.from.latlng, "y1", e.from.comment, src_icon)); } Edge
		 * lastEdge = edges.get(edges.size() - 1);
		 * itemizedoverlay.addOverlay(new OverlayItem(lastEdge.to.latlng, "y1",
		 * lastEdge.to.comment));
		 */

		mapOverlays.add(itemizedoverlay);
	}

	private class RouteOverlay extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		Context mContext;

		public RouteOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
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
			mPaint.setStrokeWidth(4);

			for (Leg leg : itinerary.getLegs()) {

				List<Location> geometry = leg.getGeometry();
				switch (leg.getMode()) {
				case WALK:
					mPaint.setColor(Color.RED);
					break;
				case BUS:
					mPaint.setColor(Color.BLUE);
					break;
				case SUBWAY:
					mPaint.setColor(Color.rgb(255, 165, 0));
					break;
				}
				for (int i = 0; i + 1 < geometry.size(); i++) {
					Point p1 = new Point(), p2 = new Point();
					projection
							.toPixels(new GeoPoint(geometry.get(i)
									.getLatitude(), geometry.get(i)
									.getLongitude()), p1);
					projection.toPixels(
							new GeoPoint(geometry.get(i + 1).getLatitude(),
									geometry.get(i + 1).getLongitude()), p2);
					canvas.drawLine(p2.x, p2.y, p1.x, p1.y, mPaint);
				}
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
		// MenuItem calcrouteItem = menu.findItem(R.id.calc_route);
		MenuItem routeInfoMenuItem = menu.findItem(R.id.route_info);
		// MenuItem inputLocationItem = menu.findItem(R.id.input_locations);
		MenuItem walkStepsItem = menu.findItem(R.id.walk_steps);
		routeInfoMenuItem.setEnabled(routeCalcDone);
		walkStepsItem.setEnabled(routeCalcDone);
		return super.onPrepareOptionsMenu(menu);
	}

	public void restoreMap(View view) {
		// setContentView(R.layout.map);
		// MapView mapView = (MapView) findViewById(R.id.mapview);
		onCreate(null);
	}

	public void onSetLocation(View view) {
		final EditText start = (EditText) findViewById(R.id.start_entry);
		final EditText end = (EditText) findViewById(R.id.end_entry);
		mSpinner.show();
		// path.setStart(start.getText().toString(), "start");
		// path.setEnd(end.getText().toString(), "end");
		mSpinner.dismiss();
		restoreMap(view);
	}

	@Override
	public void onBackPressed() {
		// do not exit if not mapview
		super.onBackPressed();
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
			startRouting();
			break;
		case R.id.walk_steps:
			Intent in = new Intent();
			in.setClass(this, WalkSteps.class);
			startActivityForResult(in, 1);
			break;
		case R.id.route_info:
			/*
			 * infoView.layout(10, 10, infoView.getWidth(),
			 * infoView.getHeight()); infoView.setVisibility(View.VISIBLE);
			 * infoView.invalidate();
			 */
			final Toast tag = Toast
					.makeText(
							getBaseContext(),
							String.format(
									"Time remaining: %.1f minutes\nWalking distance remaining: %.1f metres",
									itinerary.getDuration() / 60000.0,
									itinerary.getWalkDistance()),
							Toast.LENGTH_LONG);
			tag.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
			tag.show();
			new CountDownTimer(9000, 1000) {
				public void onTick(long millisUntilFinished) {
					tag.show();
				}

				public void onFinish() {
					tag.show();
				}
			}.start();
			break;
		case R.id.input_locations:
			setContentView(R.layout.editloc);
		default:
			return false;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case (1): {
			if (resultCode == Activity.RESULT_OK) {
				double lat = data.getDoubleExtra("walkStepLatitude", 3.0);
				double lon = data.getDoubleExtra("walkStepLongitude", 50.0);
				String desc = data.getStringExtra("walkStepDescription");
				System.out.println("grr " + lat + " " + lon);

				itemizedoverlay.addOverlay(new OverlayItem(new GeoPoint(lat,
						lon), "WalkStep", desc));
				itemizedoverlay.requestRedraw();

			}
			break;
		}
		}
	}

	void showToast(final String text) {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
			toast.show();
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast toast = Toast.makeText(Map.this, text,
							Toast.LENGTH_LONG);
					toast.show();
				}
			});
		}
	}
}
