package uk.ac.gla.get2gether;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class XMPPActivity extends Activity {
    public int state = 0;
    private static final String TAG = "get2gether XMPP"; 
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    
        
        SharedPreferences settings = getSharedPreferences("get2gether", 0);
        final String facebookID = settings.getString("facebookID", "unknown");
        
        new Thread(new Runnable() {
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
            ChatManager chatmanager = xmpp.getChatManager();
            Chat newChat = chatmanager.createChat("woody@openfire", new MessageListener() {
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
            });
                   
            // Send something to friend@gmail.com
            try {
              newChat.sendMessage("OMNOMNOM");
            } catch (XMPPException e) {
              Log.v(TAG, "couldn't send:" + e.toString());
            }
           
            // Accept only messages from friend@gmail.com
            PacketFilter filter 
                = new AndFilter(new PacketTypeFilter(Message.class), 
                                new FromContainsFilter("woody@openfire"));

            // Collect these messages
            PacketCollector collector = xmpp.createPacketCollector(filter);
            
            while(true) {
              Packet packet = collector.nextResult();
                
              if (packet instanceof Message) {
                Message msg = (Message) packet;
                // Process message
                Log.v(TAG, "Got message:" + msg.getBody());
              }
            }
              
          }
          
        }).start();
        
        //setContentView(this);
    }
}


