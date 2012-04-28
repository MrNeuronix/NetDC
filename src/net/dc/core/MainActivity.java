package net.dc.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {
    
    ///////////////////////////////
	///////////////////////////////
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.first);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        Button quickconnect = (Button) findViewById(R.id.quick);
        quickconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	
            	// Dialog
            	
            	AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

            	alert.setTitle("Quck Connect");
            	alert.setMessage("Enter parameters of hub below:");

            	// Set an EditText view to get user input 
 
            	LayoutInflater inflater = getLayoutInflater(); 
				final View layout = inflater.inflate(R.layout.quickconnect, (ViewGroup)findViewById(R.id.quickconnectlist));
            	alert.setView(layout);

            	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
            	  //Editable value = address.getText();
            	  // Do something with value!
            		
            		EditText hubname = (EditText) layout.findViewById(R.id.hubaddres);
            		EditText hubport = (EditText) layout.findViewById(R.id.hubport);
            		EditText nickname = (EditText) layout.findViewById(R.id.nick);
            		
            		Bundle bundle = new Bundle();
            		bundle.putString("hubname",hubname.getText().toString());
            		bundle.putString("hubport",hubport.getText().toString());
            		bundle.putString("nick",nickname.getText().toString());
            		
            		//Log.d("NET", "ADDR: "+hubname.getText().toString());

            		Intent newIntent = new Intent(getApplicationContext(), DCActivity.class);
            		newIntent.putExtras(bundle);
            		startActivityForResult(newIntent, 0);
            		
            	  }
            	});

            	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	  public void onClick(DialogInterface dialog, int whichButton) {
            	    // Canceled.
            	  }
            	});

            	alert.show();

            	
            	
            	/////////////////////////////////////
            	
            	
               // Intent myIntent = new Intent(view.getContext(), DCActivity.class);
               // startActivityForResult(myIntent, 0);
            }
        
        });
        
        Button exit = (Button) findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	int pid = android.os.Process.myPid();
            	android.os.Process.killProcess(pid); 
            }
        
        });
   }
}