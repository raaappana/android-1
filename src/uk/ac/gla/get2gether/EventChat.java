package uk.ac.gla.get2gether;

import org.idansof.otp.client.Location;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class EventChat {
    public int state = 0;
    private static final String TAG = "get2gether XMPP"; 
    
    public EventChat(final Map m) {  
        new Thread(new Runnable() {
            SharedPreferences settings = m.getSharedPreferences("get2gether", 0);
            final String facebookID = settings.getString("facebookID", "unknown");
            final String eventID = settings.getString("eventID", "defaultEvent");
          public void run() {
          	ConnectionConfiguration config = new ConnectionConfiguration("openfire.dcs.gla.ac.uk",
          			5222, "openfire.dcs.gla.ac.uk");
          			        	config.setTruststorePath("/system/etc/security/cacerts.bks");
          			        	config.setTruststorePassword("changeit");
          			        	config.setTruststoreType("bks");
          			        	
            XMPPConnection xmpp = new XMPPConnection(config);
            try {
            	//XMPPConnection.DEBUG_ENABLED = true;
              xmpp.connect();
              xmpp.login("get2gether", "malpka", facebookID);
            } catch (XMPPException e) {
              Log.v(TAG, "Failed to connect to " + xmpp.getHost());
              e.printStackTrace();
            }
            
            MultiUserChat muc = new MultiUserChat(xmpp, eventID+"@conference.openfire");
            
            try {
				muc.join(facebookID);
				muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
			} catch (XMPPException e1) {
				e1.printStackTrace();
			}
            
            /*Chat newChat = chatmanager.createChat("woody@openfire", new MessageListener() {
              // THIS CODE NEVER GETS CALLED FOR SOME REASON
              public void processMessage(Chat chat, Message message) {
                try {
                  Log.v(TAG, "Got:" + message.getBody());
                  chat.sendMessage(message.getBody());
                } catch (XMPPException e) {
                  Log.v(TAG, "Couldn't respond:" + e);
                }
                Log.v(TAG, message.toXML());
              }
            });*/
                   
           
            PacketFilter filter 
                = new AndFilter(new PacketTypeFilter(Message.class));

            // Collect these messages
            PacketCollector collector = xmpp.createPacketCollector(filter);
            
            while(true) {
                try {
                	if (m.currentLocation != null)
                		muc.sendMessage(m.currentLocation.getLatitude()+" "+m.currentLocation.getLongitude());
                	else
                		muc.sendMessage("don't know where I am");
                    Thread.sleep(4000);
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
                Log.v(TAG, "Got message:" + msg.getBody());
              }
            }
              
          }
          
        }).start();
        
    }
}


