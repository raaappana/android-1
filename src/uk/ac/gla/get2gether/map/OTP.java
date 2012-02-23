package uk.ac.gla.get2gether.map;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import org.idansof.otp.client.Geocoder;
import org.idansof.otp.client.GeocoderResult;
import org.idansof.otp.client.Itinerary;
import org.idansof.otp.client.Location;
import org.idansof.otp.client.PlanRequest;
import org.idansof.otp.client.Planner;
import org.idansof.otp.client.TripPlan;
import org.xmlpull.v1.XmlPullParserException;

public class OTP {
	private static final String host = "buddha.src.gla.ac.uk";

	
	public static void route(final Location from, final Location to, final Date when, final Observer ob) {	
		class Router extends Thread {
			List<Itinerary> its;
			
			public void run() {
				Planner planner = new Planner(host,
						"opentripplanner-api-webapp/ws/plan", Locale.US);
				PlanRequest req = new PlanRequest();
				req.setFrom(from);
				req.setTo(to);
				req.setDate(when);

				TripPlan plan = null;

				try {
					plan = planner.generatePlan(req).getTripPlan();
					its = plan.getItineraries();
					ob.update(null, its);
				} catch (IOException e) {
					System.err.println("Error in communicating with OTP.");
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					System.err.println("Speak to Motiejus. Now!");
					e.printStackTrace();
				} catch (java.text.ParseException e) {
					System.err.println("WTF in Routing");
					e.printStackTrace();
				}
			}
		};
		Router r = new Router();
		r.start();
		/*
		 try {
			r.join();
		} catch (InterruptedException e) {
			System.err.println("Routing thread interrupted - WTF?!");
			e.printStackTrace();
		}
		return r.its;*/
	}

	public static List<Location> geocode(final String address) {

		class Decoder extends Thread {
			List<Location> locs;

			public void run() {
				Geocoder g = new Geocoder(host,
						"opentripplanner-geocoder/geocode");
				try {
					GeocoderResult res = g.geodecode(address);
					locs = res.getLocations();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Could not decode address");
				} catch (XmlPullParserException e) {
					System.err
							.println("Something went terribly wrong - ask Motiejus.");
					e.printStackTrace();
				}
			}
		}

		Decoder d = new Decoder();
		d.start();
		try {
			d.join();
		} catch (InterruptedException e) {
			System.err.println("Geocoding thread interrupted - WTF?!");
			e.printStackTrace();
		}
		return d.locs;
	}
}
