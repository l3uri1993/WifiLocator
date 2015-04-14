package com.BandC.wifilocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
//-------------------------------------------------------------------------------------------------------------	
	public static WifiManager mWifiManager = null;
	public static RadioButton trainbtn;
	public static RadioButton posbtn;
	public static TextView xCordView;
	public static TextView yCordView;
	public static EditText xCordText;
	public static EditText yCordText;
	public static EditText scanNum;
//--------------------------------------------------------------------------------------------------------------	
	public static boolean buttonPress = false; 			//Permette di ignorare gli intent in broadcast di sistema
	public static int scanNumber;
	public static boolean isFirstScan;					//Permette al Receiver di sapere se è stato appena premuto il bottone
	private List<ScanResult> wifiList = null;
	private int firstAP,secondAP;
	private int xCord,yCord;							//Variabili per le coordinate
	private String checkMAC; 							//utilizzo questo per escludere le reti non desiderate
	private int count; 									//conta le scansioni effettuate	
	private Context context = null;
	private boolean wifiIsDisabled;
//--------------------------------------------------------------------------------------------------------------
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
        	
        	FileOutputStream fOut;
			try 
			{
				fOut = new FileOutputStream("sdcard/scan.txt", true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);			
				myOutWriter.append("X" + "          " + "Y" + "          " +  "firstAP" + "          " + "secondAP" + "\n" );			
				myOutWriter.close(); 
				fOut.close();
			}
			 catch (IOException e) 
			{				
				e.printStackTrace();
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
	protected void onDestroy() {
		super.onDestroy();
		
		if(wifiIsDisabled == true)
			mWifiManager.setWifiEnabled(false);
			
		try
		{
			unregisterReceiver(wifiReceiver);		
		}
		catch(Exception IllegalArgumentException){}
}
    
    BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent intent) 
        {
        	unregisterReceiver(wifiReceiver);
        	
        	try 
    		{
    		    xCord = Integer.parseInt(MainActivity.xCordText.getText().toString());
    		} 
    		catch(NumberFormatException nfe) 
    		{
    		   System.out.println("No number entered " + nfe);
    		} 
    		
    		try 
    		{
    		    yCord = Integer.parseInt(MainActivity.yCordText.getText().toString());
    		} 
    		catch(NumberFormatException nfe) 
    		{
    		   System.out.println("No number entered " + nfe);
    		} 
    		
    		if (MainActivity.buttonPress == true)
    		{		
    		try 
    		{
    		    scanNumber = Integer.parseInt(MainActivity.scanNum.getText().toString());
    		} 
    		catch(NumberFormatException nfe) 
    		{
    		   System.out.println("No number entered " + nfe);
    		} 
    						
    		try {
    			FileOutputStream fOut = new FileOutputStream("sdcard/scan.txt", true); //creato nuovo stream di output per la scrittura
    			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    								
    		if(MainActivity.trainbtn.isChecked() == true)
    		{				
    			
    			if(MainActivity.isFirstScan == true)
    			{
    				MainActivity.isFirstScan = false;
    				count = scanNumber - 1;
    			}
    			
    			Toast.makeText(getApplicationContext(), "Scan number " + (scanNumber - count), Toast.LENGTH_SHORT).show();
    										
    			wifiList = MainActivity.mWifiManager.getScanResults();
    			
    			for(int i = 0; i < wifiList.size(); i++)
    			{											
    				checkMAC = wifiList.get(i).BSSID;				
        			if(checkMAC.equals("a0:f3:c1:6c:1e:49") == true)
        			{
        				firstAP = firstAP + wifiList.get(i).level;
        			}
        			if(checkMAC.equals("00:26:44:74:e9:3e") == true)
        			{
        				secondAP = secondAP + wifiList.get(i).level;    	
        			}
    			}
    			if (count != 0)
    			{
    				 count--;
    				 myOutWriter.close();
    				 fOut.close();			
    				 registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    				 MainActivity.mWifiManager.startScan();
    				 return;
    			}
    			else
    			{  				
    				firstAP = firstAP / scanNumber;
    				secondAP = secondAP / scanNumber;
    				
    				myOutWriter.append(xCord + "          " + yCord + "          " +  firstAP + "          " + secondAP + "\n" );
    				Toast.makeText(getApplicationContext(), "Recorded on file", Toast.LENGTH_LONG).show();
    				firstAP = 0;
    				secondAP = 0;
    			}			    			
    		}
    			
    			myOutWriter.close();
    			fOut.close();						
    		}
    		
    		catch (FileNotFoundException e) 
    		{			
    			e.printStackTrace();
    		} 
    		catch (IOException e) 
    		{
    			e.printStackTrace();
    		}
    		
    		if(MainActivity.posbtn.isChecked() == true)
    		{	
    			Toast.makeText(c, "No action performed", Toast.LENGTH_LONG).show();
    		}
    		MainActivity.buttonPress = false;
    		}
    		else{}
        }
    };
    	  
    public void startScan (View view) {
    	
    	isFirstScan = true;
       	buttonPress = true;
    	
    	try 
		{
		    scanNumber = Integer.parseInt(MainActivity.scanNum.getText().toString());
		} 
		catch(NumberFormatException nfe) 
		{
		   System.out.println("No number entered " + nfe);
		}
    	   	
    	if (mWifiManager.isWifiEnabled() == false) 			//Enable wifi if it is disabled
    	{      			
    		Toast.makeText(getApplicationContext(), "Wifi is disabled..Making it enabled", Toast.LENGTH_LONG).show();
    		mWifiManager.setWifiEnabled(true);
    	} 

    	registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_LONG).show();    		
    	mWifiManager.startScan();
    		    		    
    }  
}