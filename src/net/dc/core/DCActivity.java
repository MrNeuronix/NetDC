package net.dc.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calyriumdc.*;
import calyriumdc.event.*;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class DCActivity extends Activity implements OnTouchListener {
	
    private PowerManager.WakeLock wl;
    private float fromPosition;
    
    private String addr = "dc.azeroth.su";
    private int port = Integer.parseInt(SettingsManager.getInstance().getHubPort());
    private String nick = SettingsManager.getInstance().getNick();
    //private String nick ="nix";
    private String password = SettingsManager.getInstance().getPassword();
    private int id;
    
	ListView		msgList;
	ListView		usersList;
	static ArrayAdapter	<Spanned>receivedMessages;
	ArrayAdapter	<String>usersCount;
    
    ///////////////////////////////
	///////////////////////////////


    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        Bundle bundle = this.getIntent().getExtras();
        
        if(!bundle.getString("hubname").isEmpty())
        {
        	Log.d("NET", "GET QUICK!");
        	
        	addr = bundle.getString("hubname");
        	port = Integer.parseInt(bundle.getString("hubport"));
        	nick = bundle.getString("nick");
        	
        	Log.d("NET", "GET QUICK! NICK: "+bundle.getString("nick"));
        }
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);  
        
        // Не даем засыпать экрану
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

        //////////////////////////////////
        //////////////////////////////////
        
        final EditText toHub = (EditText) this.findViewById(R.id.hubchat_entry);
        TextView nameHub = (TextView) this.findViewById(R.id.hub_hubName);
        Button btnSendMsg = (Button) findViewById(R.id.sent_msg);

        id = ConnectionManager.getInstance().createClient2Server(addr, port, nick, password);
        
        Log.d("NET","Connecting to "+addr+":"+port);
        Log.d("NET","Connect ID: "+id);

        ConnectionManager.getInstance().addChatListenerToClient2Server(id, getChatListener());
        ConnectionManager.getInstance().addNickListenerToClient2Server(id, getNickListener());

        ConnectionManager.getInstance().startConnection(id);
        Log.d("NET","Connect ID "+id+" started!");
        
        //SettingsManager.getInstance().loadDefault();
        //SettingsManager.getInstance().saveFile();
        
        
        //////////////////////////////////
        //////////////////////////////////
        
        OnClickListener oclbtnSendMsg = new OnClickListener() {
            @Override
            public void onClick(View v) {
             
            	ConnectionManager.getInstance().sendText(id, toHub.getText().toString());
            	toHub.setText("");

            }
          };
          
          btnSendMsg.setOnClickListener(oclbtnSendMsg);

        //////////////////////////////////
        //////////////////////////////////
          
          // Устанавливаем listener касаний, для последующего перехвата жестов
          LinearLayout mainLayout = (LinearLayout) findViewById(R.id.hub_mainLayout);
          mainLayout.setOnTouchListener(this);

          // Получаем объект ViewFlipper
          ViewFlipper flipper = (ViewFlipper) findViewById(R.id.hub_flipper);

          // Создаем View и добавляем их в уже готовый flipper
          LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          int layouts[] = new int[]{ R.layout.hubchat, R.layout.userlist };
          for (int layout : layouts)
              flipper.addView(inflater.inflate(layout, null));
          
          
          //////////////////////////////////
          //////////////////////////////////   
          
          msgList = (ListView)findViewById(R.id.msgList);
          usersList = (ListView)findViewById(R.id.usersRow);

          receivedMessages = new ArrayAdapter<Spanned>(this, R.layout.message);
          usersCount = new ArrayAdapter<String>(this, R.layout.message);
          msgList.setAdapter(receivedMessages);    
          usersList.setAdapter(usersCount); 
          
       ////////
       ////////
    
        nameHub.setText("Hub: "+addr);
        receivedMessages.add(Html.fromHtml("<b><i>*** Соединяемся ***</b></i>"));
        usersCount.add(nick);
   
    }
    
    ///////////////////////////////////
    ///////////////////////////////////
    ///// Методы, приватные классы
    ///////////////////////////////////
    ///////////////////////////////////
    
    public boolean onTouch(View view, MotionEvent event)
    {
        ViewFlipper flipper = (ViewFlipper) findViewById(R.id.hub_flipper);
        flipper.getDisplayedChild();
    	
        switch (event.getAction())
        {
        
        case MotionEvent.ACTION_DOWN:
            fromPosition = event.getX();
            Log.d("NET", "ACT DWN");
            break;
        case MotionEvent.ACTION_UP:
            float toPosition = event.getX();
            Log.d("NET", "ACT UP");

            if (fromPosition > toPosition)
            {
		        Log.d("NET", "onTouch SHOW NEXT! "+flipper.toString());
                flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.go_next_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.go_next_out));
                flipper.showNext();
            }
            else if (fromPosition < toPosition)
            {
		        Log.d("NET", "onTouch SHOW PREV!"+flipper.toString());
                flipper.setInAnimation(AnimationUtils.loadAnimation(this,R.anim.go_prev_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(this,R.anim.go_prev_out));
                flipper.showPrevious();
            }
        default:
            break;
        }
        
        return true;
    }
	  
		///////////////
		///////////////
  
    
    @Override
    protected void onPause() {
            super.onPause();
            wl.release();
    }

    @Override
    protected void onResume() {
            super.onResume();
            wl.acquire();
    }
    

	protected NickListener getNickListener() {
	
		Log.d("NET","AT getNickListener!");
	
			return new NickListener() {
			
			    public void nickChanged(final NickEvent evt) {
			    	runOnUiThread(new Runnable() {
			            public void run() {
			            	
			            	
			    		      ViewFlipper flipper = (ViewFlipper) findViewById(R.id.hub_flipper);
			    		  	  View v1 = flipper.getChildAt(1);
			    		  	  final ListView users = (ListView) v1.findViewById(R.id.usersRow);
		
				    	    	users.post(new Runnable()
				    	        { 
				    	            public void run()
				    	            { 
				    	            	if(!evt.getNick().matches(nick))
				    	            	      usersCount.add(evt.getNick());
				    	            } 
				    	        });
			            	
			              
//			                    String[] row = new String[7];
//			                    row[0] = evt.getNick();
//			                    row[1] = Crypto.getBytes(Long.parseLong(evt.getShare()));
//			                    row[2] = evt.getDescription();
//			                    row[3] = evt.getTag();
//			                    row[4] = evt.getSpeed();
//			                    row[5] = evt.getEmail();
//			                    row[6] = evt.getIp();
			
			                    Log.d("NET","User added: "+evt.getNick());
			
			            }
			        });
			    }
			
			    public void removeNick(final NickEvent evt) {
			    	runOnUiThread(new Runnable() {
			
			            public void run() {
			            	
	    	            	int pos = usersCount.getPosition(evt.getNick());
	    	            	
	    	            	usersCount.remove(usersCount.getItem(pos));
	    	            	usersList.invalidateViews();
	    	            	
			            	Log.d("NET","User removed: "+evt.getNick());
    
			            }
			    	});
			    }
			};
	}
	
	/////////////////////////////
	
	protected ChatListener getChatListener() {
	return new ChatListener() {
	
	    public void chatEvent(final ChatEvent evt) {
	    	
	    	
		     runOnUiThread(new Runnable() {
		    	    public void run() {
		    	    	
		    	    	String data = evt.getData().replace("<", "&lt;").replace(">", "&gt;");
		    	    	
		    	        // Create a pattern to match cat
		    	        Pattern p = Pattern.compile("(&lt;.*?&gt;):");
		    	        Matcher m = p.matcher(data);
		    	        StringBuffer sb = new StringBuffer();
		    	        boolean result = m.find();

		    	        while(result) {
		    	            m.appendReplacement(sb, "<b><i><font color=blue>"+m.group(1).toString()+"</font></b></i>");
		    	            result = m.find();
		    	        }

		    	        m.appendTail(sb);
		    	        data = sb.toString();	
		    	    	
		    	    	final java.sql.Time time = new java.sql.Time( System.currentTimeMillis() );
		    	    	receivedMessages.add(Html.fromHtml("<b><font color=red>"+time.toString() + "</font></b> " + data));
		    	    }
		    	});
	    }
	
	    public void privateMessage(final PrivateMessageEvent evt) {
		      runOnUiThread(new Runnable() {
		    	    public void run() {
		    	    	
		    	    	  java.sql.Time time = new java.sql.Time( System.currentTimeMillis() );	    	    	
		    			  receivedMessages.add(Html.fromHtml(time.toString() + " >>> " + evt.getData()));
		    	    }
		    	});
	    }
	};
	}
}