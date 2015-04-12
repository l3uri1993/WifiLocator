package com.BandC.wifilocator;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static WifiManager mWifiManager = null;
	public static RadioButton trainbtn;
	public static RadioButton posbtn;
	public static TextView xCordView;
	public static TextView yCordView;
	public static EditText xCordText;
	public static EditText yCordText;
	public static EditText scanNum;
	public static boolean buttonPress = false; //Permette di ignorare gli intent in broadcast di sistema
	
	
	private Context context = null;
	public static WifiActivity receiverWifi = null;
	private boolean wifiIsDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                	                   
        xCordView = (TextView) findViewById(R.id.x_coord_tx);
        yCordView = (TextView) findViewById(R.id.y_coord_tx);
        xCordText = (EditText) findViewById(R.id.x_coord_edtx);
        yCordText = (EditText) findViewById(R.id.y_coord_edtx);       
        trainbtn = (RadioButton) findViewById(R.id.Trainrbtn);
        posbtn = (RadioButton) findViewById(R.id.Positionrbtn);
        scanNum = (EditText) findViewById(R.id.scannum_etx);
        
        
        context = getApplicationContext();
        
        //Hiding components
        xCordView.setVisibility(View.GONE); 
        yCordView.setVisibility(View.GONE);
        xCordText.setVisibility(View.GONE);
        yCordText.setVisibility(View.GONE);
                        
// Check for "scan.txt" file in /sdcard/...if doesn't exist the program creates it
        
        File extStore = Environment.getExternalStorageDirectory();
        File myFile = new File(extStore.getAbsolutePath() + "/scan.txt");
        if (myFile.exists()) {
        	Toast.makeText(getApplicationContext(), "Scan.txt already exists", Toast.LENGTH_LONG).show();
        }
        else
        {
        	try {
                File results = new File("/sdcard/scan.txt");
                results.createNewFile();
                Toast.makeText(getApplicationContext(), "File Scan.txt created on /sdcard/", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }           

 //-----------------------------------------------------------------------------------------------------------------------
             
        //Initialize the WiFi Manager
        
		mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		 if(mWifiManager.isWifiEnabled() == false) //Memorizza se all'avvio il wifi era abilitato per disabilitarlo in chiusura
	        	wifiIsDisabled = true;
		 
    }
    
    public void Posclick (View view) {
        xCordView.setVisibility(View.GONE); 
        yCordView.setVisibility(View.GONE);
        xCordText.setVisibility(View.GONE);
        yCordText.setVisibility(View.GONE);
    	trainbtn.setChecked(false);
    }
    
    public void Trainclick (View view) {
        xCordView.setVisibility(View.VISIBLE); 
        yCordView.setVisibility(View.VISIBLE);
        xCordText.setVisibility(View.VISIBLE);
        yCordText.setVisibility(View.VISIBLE);
    	posbtn.setChecked(false);
    	
    }
    
    @Override
    protected void onPause() {
    	if(receiverWifi != null)
    		unregisterReceiver(receiverWifi);
        super.onPause();
    }
    
    @Override
    protected void onResume() {
    	if(receiverWifi != null)
    		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(wifiIsDisabled == true)
			mWifiManager.setWifiEnabled(false);
			
		try
		{
			unregisterReceiver(receiverWifi);		
		}
		catch(Exception IllegalArgumentException){}
}
    	  
    public void startScan (View view) {
    	
    	buttonPress = true;
    	
    	if(posbtn.isChecked() == false && trainbtn.isChecked() == false)
    		Toast.makeText(getApplicationContext(), "No selection detected", Toast.LENGTH_LONG).show();
    	
    	else
    	{
		// Check for wifi is disabled
    		if (mWifiManager.isWifiEnabled() == false)
    		{   
    			// If wifi disabled then enable it
    			Toast.makeText(getApplicationContext(), "Wifi is disabled..Making it enabled", Toast.LENGTH_LONG).show();
    			// Enable WiFi
    			mWifiManager.setWifiEnabled(true);
    		} 

    		// wifi scanned value broadcast receiver 
    		receiverWifi = new WifiActivity(context);

    		// Register broadcast receiver 
    		// Broadcast receiver will automatically call when number of wifi connections changed
    		registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    		mWifiManager.startScan();    		   		
    	}
	}      
}