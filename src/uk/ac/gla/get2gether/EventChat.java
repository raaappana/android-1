package uk.ac.gla.get2gether;

import java.util.InputMismatchException;
import java.util.Scanner;

import org.idansof.otp.client.Location;
import org.jivesoftware.smack.ChatManager;
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

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class EventChat {
	private static boolean started = false;
	private static final String TAG = "get2gether XMPP";

	public static void start(final Map m) {
		if (!started) {
			started = true;
			new Thread(new Runnable() {
				SharedPreferences settings = m.getSharedPreferences(
						"get2gether", 0);
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
					try {
						// XMPPConnection.DEBUG_ENABLED = true;
						xmpp.connect();
						xmpp.login("get2gether", "malpka", facebookID);
					} catch (XMPPException e) {
						Log.v(TAG, "Failed to connect to " + xmpp.getHost());
						e.printStackTrace();
						return;
					}

					MultiUserChat muc = new MultiUserChat(xmpp, eventID
							+ "@conference.openfire");

					try {
						muc.join(facebookID);
						muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
					} catch (XMPPException e1) {
						e1.printStackTrace();
					}

					/*
					 * Chat newChat = chatmanager.createChat("woody@openfire",
					 * new MessageListener() { // THIS CODE NEVER GETS CALLED
					 * FOR SOME REASON public void processMessage(Chat chat,
					 * Message message) { try { Log.v(TAG, "Got:" +
					 * message.getBody()); chat.sendMessage(message.getBody());
					 * } catch (XMPPException e) { Log.v(TAG,
					 * "Couldn't respond:" + e); } Log.v(TAG, message.toXML());
					 * } });
					 */

					PacketFilter filter = new AndFilter(new PacketTypeFilter(
							Message.class), new NotFilter(
							new FromMatchesFilter(eventID
									+ "@conference.openfire/" + facebookID)));

					// Collect these messages
					PacketCollector collector = xmpp
							.createPacketCollector(filter);

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
							// TODO Auto-generated catch block
							e.printStackTrace();
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
								if (c.getTitle() == msg.getFrom()) {
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
								Log.v(TAG, "Adding new circle");
							}

							m.circleOverlay.addCircles(m.friendsLocations);
							m.circleOverlayFill.setColor(Color.GREEN);
							m.circleOverlayFill.setAlpha(48);
							m.circleOverlay.requestRedraw();
						}
					}

				}

			}).start();
		}

	}
}
