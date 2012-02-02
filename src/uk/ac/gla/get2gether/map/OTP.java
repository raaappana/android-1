package uk.ac.gla.get2gether.map;

import java.io.IOException;
import java.util.List;

import org.idansof.otp.client.Geocoder;
import org.idansof.otp.client.GeocoderResult;
import org.idansof.otp.client.Location;
import org.xmlpull.v1.XmlPullParserException;

public class OTP {
	private static final String host = "dcs.gla.ac.uk";

	public static List<Location> geocode(final String address) {

		class Decoder extends Thread {
			List<Location> locs;

			public void run() {
				Geocoder g = new Geocoder(host,
						"L311_D/opentripplanner-geocoder/geocode");
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
