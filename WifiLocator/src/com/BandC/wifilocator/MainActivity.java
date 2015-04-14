package com.BandC.wifilocator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
//--------------------------------------------------------------VARIABILI XML-------------------------	
	
	public static RadioButton trainbtn;
	public static RadioButton posbtn;
	public static TextView xCordView;
	public static TextView yCordView;
	public static EditText xCordText;
	public static EditText yCordText;
	public static EditText scanNum;
	public static Button scanbtn;
	
//--------------------------------------------------------------VARIABILI JAVA--------------------------------------
	
	public static WifiManager mWifiManager = null;		
	public static boolean buttonPress = false; 			//Permette di ignorare gli intent in broadcast di sistema
	public static int scanNumber;						//Memorizza le scansioni da effettuare
	public static boolean isFirstScan;					//Permette al Receiver di sapere se è stato appena premuto il bottone
	private List<ScanResult> wifiList = null;			//Memorizza i risultati temporanei ottenuti dalle scansioni
	private int firstAP,secondAP;						//RSSI di ciascun AP
	private int xCord,yCord;							//Variabili per le coordinate
	private int count; 									//conta le scansioni effettuate	
	private boolean wifiIsDisabled;						//Controlla all'avvio se il Wifi era disabilitato
	private Context context = null;
		
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
        scanbtn = (Button) findViewById(R.id.scanbtn);
       
               
        context = getApplicationContext();
                        
//------------------------------ Check for "scan.txt" file in /sdcard/...if doesn't exist the program creates it
        
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
             
//--------------------------------------Initialize the WiFi Manager-----------------------------------------------------
        
		mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		 if(mWifiManager.isWifiEnabled() == false) //Memorizza se all'avvio il wifi era abilitato per disabilitarlo in chiusura
	        	wifiIsDisabled = true;		 
//-----------------------------------------------------------------------------------------------------------------------
    }
       
    BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent intent) 
        {
        	unregisterReceiver(wifiReceiver);
        	
        	try 
    		{
    		    xCord = Integer.parseInt(xCordText.getText().toString());
    		} 
    		catch(NumberFormatException nfe) 
    		{
    		   System.out.println("No number entered " + nfe);
    		} 
    		
    		try 
    		{
    		    yCord = Integer.parseInt(yCordText.getText().toString());
    		} 
    		catch(NumberFormatException nfe) 
    		{
    		   System.out.println("No number entered " + nfe);
    		} 
    		
    		if (buttonPress == true)
    		{		
    		try 
    		{
    		    scanNumber = Integer.parseInt(scanNum.getText().toString());
    		} 
    		catch(NumberFormatException nfe) 
    		{
    		   System.out.println("No number entered " + nfe);
    		} 
    						
    		try 
    		{
    			FileOutputStream fOut = new FileOutputStream("sdcard/scan.txt", true); //creato nuovo stream di output per la scrittura
    			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    								
    			if(trainbtn.isChecked() == true)
    			{				
    			
    				if(isFirstScan == true)
    				{
    					isFirstScan = false;
    					count = scanNumber - 1;
    				}
    			
    				Toast.makeText(getApplicationContext(), "Scan number " + (scanNumber - count), Toast.LENGTH_SHORT).show();
    										
    				wifiList = mWifiManager.getScanResults();
    			
    				for(int i = 0; i < wifiList.size(); i++)
    				{											    					
    					if((wifiList.get(i).BSSID).equals("a0:f3:c1:6c:1e:49") == true)    					
    						firstAP = firstAP + wifiList.get(i).level;
    					
    					if((wifiList.get(i).BSSID).equals("00:26:44:74:e9:3e") == true)    					
    						secondAP = secondAP + wifiList.get(i).level;    	   					
    				}
    				
    				if (count != 0)
    				{
    					count--;
    					myOutWriter.close();
    					fOut.close();			
    					registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    					mWifiManager.startScan();
    					return;
    				}
    				else
    				{  				  				
    					myOutWriter.append(xCord + "          " + yCord + "          " +  (firstAP/scanNumber) + "                " + (secondAP/scanNumber) + "\n" );
    					Toast.makeText(getApplicationContext(), "Recorded on file", Toast.LENGTH_LONG).show();
    					myOutWriter.close();
    					fOut.close();
    					firstAP = 0;
    					secondAP = 0;
    					EnableButtons();
    				}			    			
    			}						
    		}
    		
    		catch (FileNotFoundException e) 
    		{			
    			e.printStackTrace();
    		} 
    		catch (IOException e) 
    		{
    			e.printStackTrace();
    		}
    		
    		if(posbtn.isChecked() == true)
    		{	
    			if(isFirstScan == true)
				{
					isFirstScan = false;
					count = scanNumber - 1;
				}
    			
    			Toast.makeText(getApplicationContext(), "Scan number " + (scanNumber - count), Toast.LENGTH_SHORT).show();
				
				wifiList = mWifiManager.getScanResults();
			
				for(int i = 0; i < wifiList.size(); i++)
				{											    					
					if((wifiList.get(i).BSSID).equals("a0:f3:c1:6c:1e:49") == true)    					
						firstAP = firstAP + wifiList.get(i).level;
					
					if((wifiList.get(i).BSSID).equals("00:26:44:74:e9:3e") == true)    					
						secondAP = secondAP + wifiList.get(i).level;    	   					
				}
				
    			if (count != 0)
				{
					count--;			
					registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
					mWifiManager.startScan();
					return;
				}
				else
				{  	
					firstAP = (firstAP/scanNumber);
					secondAP = (secondAP/scanNumber);
					//
					//
					//
					//
					// Dopo le scansioni, i valori sono stati acquisiti e ci posso lavorare sopra qui in mezzo
					Toast.makeText(getApplicationContext(), "No action performed", Toast.LENGTH_LONG).show();
					//
					//
					//
					//	
					//
					firstAP = 0;
					secondAP = 0;
					EnableButtons();
				}			    	   			   			
    		}
    		buttonPress = false;
    		}
    		else{} // La scansione e stata inviata dal sistema e va ignorata
        }
    };
    	  
    public void EnableButtons()							//Abilita tutti gli elementi editabili concluse le operazioni
    {
    	scanbtn.setClickable(true);
    	xCordText.setEnabled(true);
        yCordText.setEnabled(true);
        scanNum.setEnabled(true);
        trainbtn.setClickable(true);
        posbtn.setClickable(true);
    }
    
    public void DisableButtons() 						//Disabilita tutti gli elementi editabili concluse le operazioni
    {
    	scanbtn.setClickable(false);
    	xCordText.setEnabled(false);
        yCordText.setEnabled(false);
        scanNum.setEnabled(false);
        trainbtn.setClickable(false);
        posbtn.setClickable(false);
    }
    
    public void StartScan (View view) {					//Si avvia al premere del pulsante di scansione
    	
    	DisableButtons();    	    	
    	isFirstScan = true;
       	buttonPress = true;
    	
    	try 
		{
		    scanNumber = Integer.parseInt(scanNum.getText().toString());
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

    public void Posclick (View view) {					//Nasconde alcuni elementi come da specifica
    xCordView.setVisibility(View.GONE); 
    yCordView.setVisibility(View.GONE);
    xCordText.setVisibility(View.GONE);
    yCordText.setVisibility(View.GONE);
	trainbtn.setChecked(false);
}

    public void Trainclick (View view) {				//Abilita alcuni elementi come da specifica
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
		mWifiManager.setWifiEnabled(false);			//Alla chiusura spegne il wifi poichè all'apertura era disabilitato
	try
	{
		unregisterReceiver(wifiReceiver);		
	}
	catch(Exception IllegalArgumentException){}
}
}