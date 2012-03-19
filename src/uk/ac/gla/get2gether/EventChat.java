package uk.ac.gla.get2gether;

import java.util.InputMismatchException;
import java.util.Scanner;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromMatchesFilter;
import org.jivesoftware.smack.filter.NotFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayCircle;

import android.content.SharedPreferences;
import android.util.Log;

public class EventChat {
	private static boolean started = false;
	private static final String TAG = "get2gether XMPP";
	private static Thread t;

	public static void start(final Map m) {
		if (started) {
			if (t != null)
				t.interrupt();
			started = false;
			start(m);
		}
		else {
			started = true;
			t = new Thread(new Runnable() {
				boolean works = false;
				SharedPreferences settings = m.getSharedPreferences(
						"get2gether", 0);
				MultiUserChat muc;
				final String facebookID = settings.getString("facebookID",
						"unknown");
				final String eventID = settings.getString("eventID",
						"defaultEvent");

				public void run() {
					ConnectionConfiguration config = new ConnectionConfiguration(
							"openfire.dcs.gla.ac.uk", 5222,
							"openfire.dcs.gla.ac.uk");
					config.setTruststorePath("/system/etc/security/cacerts.bks");
					config.setTruststorePassword("changeit");
					config.setTruststoreType("bks");

					XMPPConnection xmpp = new XMPPConnection(config);

					while (!works) {
						try {
							Thread.sleep(10000);
							m.showToast("Trying to connect to friends...");
						} catch (InterruptedException e2) {
							e2.printStackTrace();
						}
						try {
							// XMPPConnection.DEBUG_ENABLED = true;
							xmpp.connect();
							xmpp.login("get2gether", "malpka", facebookID);
						} catch (XMPPException e) {
							Log.v(TAG, "Failed to connect to " + xmpp.getHost());
							e.printStackTrace();
							continue;
						} catch (IllegalStateException e) {
							continue;
						}

						muc = new MultiUserChat(xmpp, eventID
								+ "@conference.openfire");

						try {
							muc.join(facebookID);
							muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
						} catch (XMPPException e1) {
							e1.printStackTrace();
							continue;
						}

						works = true;
					}

					m.showToast("Connected!");

					PacketFilter filter = new AndFilter(new PacketTypeFilter(
							Message.class), new NotFilter(
							new FromMatchesFilter(eventID
									+ "@conference.openfire/" + facebookID)));

					// Collect these messages
					PacketCollector collector = xmpp
							.createPacketCollector(filter);

					try {
						muc.sendMessage("I'm in!");
					} catch (XMPPException e) {
						Log.v(TAG, "couldn't send:" + e.toString());
					}

					while (true) {
						try {
							if (m.currentLocation != null)
								muc.sendMessage("Location: "
										+ m.currentLocation.getLatitude() + " "
										+ m.currentLocation.getLongitude()
										+ " " + m.currentLocation.getAccuracy());
							m.route();
							Thread.sleep(10000);
						} catch (XMPPException e) {
							Log.v(TAG, "couldn't send:" + e.toString());
						} catch (InterruptedException e) {
							muc.leave();
							xmpp.disconnect();
							return;
						}
						Packet packet = collector.nextResult();

						if (packet instanceof Message) {
							Message msg = (Message) packet;
							// Process message
							if (!msg.getBody().startsWith("Location: "))
								continue;

							double lat, lon;
							float accuracy;

							try {
								Scanner s = new Scanner(msg.getBody()
										.substring(10));
								lat = s.nextFloat();
								lon = s.nextFloat();
								accuracy = s.nextFloat();
							} catch (InputMismatchException e) {
								Log.v(TAG, "Location input mismatch");
								continue;
							}
							Log.v(TAG,
									"Got message:" + msg.getBody()
											+ msg.getFrom());

							boolean found = false;

							for (OverlayCircle c : m.friendsLocations) {
								if (c.getTitle().equals(msg.getFrom())) {
									c.setCircleData(new GeoPoint(lat, lon),
											accuracy);
									found = true;
									break;
								}
							}

							if (!found) {
								OverlayCircle c = new OverlayCircle(
										new GeoPoint(lat, lon), accuracy, msg
												.getFrom());
								m.friendsLocations.add(c);
								m.friendCircleOverlay.addCircle(c);
								Log.v(TAG, "Adding new circle");
							}

							m.friendCircleOverlay.requestRedraw();
						}
					}

				}

			});
			t.start();
		}

	}
}
