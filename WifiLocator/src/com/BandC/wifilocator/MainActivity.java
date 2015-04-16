package com.BandC.wifilocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
	
	private RadioButton trainBtn;
	private RadioButton posBtn;
	private TextView xCordView;
	private TextView yCordView;
	private EditText xCordText;
	private EditText yCordText;
	private EditText scanNum;
	private Button scanBtn;
	
//--------------------------------------------------------------VARIABILI JAVA--------------------------------------
			
	private boolean buttonPress = false; 				//Permette di ignorare gli intent in broadcast di sistema
	private int scanNumber;								//Memorizza le scansioni da effettuare
	private boolean isFirstScan;						//Permette al Receiver di sapere se è stato appena premuto il bottone
	private List<ScanResult> wifiList = null;			//Memorizza i risultati temporanei ottenuti dalle scansioni
	private int firstAP,secondAP;						//RSSI di ciascun AP
	private int xCord,yCord;							//Variabili per le coordinate
	private int count; 									//conta le scansioni effettuate	
	private boolean wifiIsDisabled;						//Controlla all'avvio se il Wifi era disabilitato
	private WifiManager mWifiManager = null;
	private Context context = null;
	private File file;
		
//--------------------------------------------------------------------------------------------------------------
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                	                   
        xCordView = (TextView) findViewById(R.id.x_coord_tx);
        yCordView = (TextView) findViewById(R.id.y_coord_tx);
        xCordText = (EditText) findViewById(R.id.x_coord_edtx);
        yCordText = (EditText) findViewById(R.id.y_coord_edtx);       
        trainBtn = (RadioButton) findViewById(R.id.Trainrbtn);
        posBtn = (RadioButton) findViewById(R.id.Positionrbtn);
        scanNum = (EditText) findViewById(R.id.scannum_etx);
        scanBtn = (Button) findViewById(R.id.scanrbtn);
       
               
        context = getApplicationContext();
                        
//------------------------------ Check for "radioMap.txt" file in /sdcard/...if doesn't exist the program creates it
        
        File extStore = Environment.getExternalStorageDirectory();
        File myFile = new File(extStore.getAbsolutePath() + "/radioMap.txt");
        if (myFile.exists()) {
        	Toast.makeText(getApplicationContext(), "radioMap.txt already exists", Toast.LENGTH_LONG).show();
        }
        else
        {
        	try 
        	{
                File results = new File("/sdcard/radioMap.txt");
                results.createNewFile();
                Toast.makeText(getApplicationContext(), "File radioMap.txt created on /sdcard/", Toast.LENGTH_LONG).show();			
                FileOutputStream fOut = new FileOutputStream("sdcard/radioMap.txt", true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.append("X" + "          " + "Y" + "         " +  "AP-1" + "       " + "AP-2" + "\n" );
				myOutWriter.close(); 
				fOut.close();
				file = new File("/sdcard/radioMap.txt");
			}		 
        	catch (NullPointerException e)  {e.printStackTrace();} 
        	catch (FileNotFoundException e) {e.printStackTrace();} 
        	catch (NumberFormatException e) {e.printStackTrace();}
        	catch (IOException e) {e.printStackTrace();}						        
        }
             
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
    		    yCord = Integer.parseInt(yCordText.getText().toString());
    		} 
    		catch(NumberFormatException nfe) {System.out.println("No number entered " + nfe);} 
    		
    		if (buttonPress == true)
    		{		
    		try 
    		{    						    		
    			FileOutputStream fOut = new FileOutputStream("sdcard/radioMap.txt", true); //creato nuovo stream di output per la scrittura
    			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    								
    			if(trainBtn.isChecked() == true)
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
    					myOutWriter.append(xCord + "          " + yCord + "          " +  (firstAP/scanNumber) + "          " + (secondAP/scanNumber) + "\n" );
    					Toast.makeText(getApplicationContext(), "Recorded on file", Toast.LENGTH_LONG).show();
    					myOutWriter.close();
    					fOut.close();
    					firstAP = 0;
    					secondAP = 0;
    					EnableButtons();
    				}			    			
    			}						   		    		   		
    		if(posBtn.isChecked() == true)
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
					firstAP = -50;								//LINEA PER TEST
					secondAP = -50;								//LINEA PER TEST		
					CheckLocation();			
					EnableButtons();
				}			    	   			   			
    		}
    		buttonPress = false;
    		}
    		catch (NullPointerException e)  {e.printStackTrace();} 
    		catch (FileNotFoundException e) {e.printStackTrace();} 
    		catch (NumberFormatException e) {e.printStackTrace();}
    		catch (IOException e) {e.printStackTrace();}
    		}
    		else{} // La scansione e stata inviata dal sistema e va ignorata
        }
    };
    	  
    public void EnableButtons()							//Abilita tutti gli elementi editabili concluse le operazioni
    {
    	scanBtn.setClickable(true);
    	xCordText.setEnabled(true);
        yCordText.setEnabled(true);
        scanNum.setEnabled(true);
        trainBtn.setClickable(true);
        posBtn.setClickable(true);
    }
    
    public void DisableButtons() 						//Disabilita tutti gli elementi editabili concluse le operazioni
    {
    	scanBtn.setClickable(false);
    	xCordText.setEnabled(false);
        yCordText.setEnabled(false);
        scanNum.setEnabled(false);
        trainBtn.setClickable(false);
        posBtn.setClickable(false);
    }
    
    public void StartScan (View view) 					//Si avvia al premere del pulsante di scansione
    {					   	
    	DisableButtons();    	    	
    	isFirstScan = true;
       	buttonPress = true;
    	
    	try 
		{
		    scanNumber = Integer.parseInt(scanNum.getText().toString());
		} 
    	catch (NumberFormatException e) {e.printStackTrace();}
    	   	
    	if (mWifiManager.isWifiEnabled() == false) 			//Enable wifi if it is disabled
    	{      			
    		Toast.makeText(getApplicationContext(), "Wifi is disabled..Making it enabled", Toast.LENGTH_LONG).show();
    		mWifiManager.setWifiEnabled(true);
    	} 

    	registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_LONG).show();    		
    	mWifiManager.startScan();    		    		    
    }  

    public void Posclick (View view) 					//Nasconde alcuni elementi come da specifica
    {							
    	xCordView.setVisibility(View.GONE); 
    	yCordView.setVisibility(View.GONE);
    	xCordText.setVisibility(View.GONE);
    	yCordText.setVisibility(View.GONE);
    	trainBtn.setChecked(false);
    }

    public void Trainclick (View view) 					//Abilita alcuni elementi come da specifica
    {																
    	xCordView.setVisibility(View.VISIBLE);
    	yCordView.setVisibility(View.VISIBLE);
    	xCordText.setVisibility(View.VISIBLE);
    	yCordText.setVisibility(View.VISIBLE);
		posBtn.setChecked(false);  	
}
    
    public void CheckLocation()							//Mostra a schermo la posizione attuale
    {   	
    	int distance = 9999;
    	xCord = -1;
		yCord = -1;

		try 
		{			
			BufferedReader fileReader = new BufferedReader(new FileReader(file)); 		//Reader android per leggere stringhe da txt
			StringBuilder line = new StringBuilder();								 	//Variabile della stringa presa dal txt		    
			String stringResult = fileReader.readLine(); 								//Salta la prima linea (è solo testo per indentazione)
			
		    while ((stringResult = fileReader.readLine()) != null)
			{
				line.append(stringResult);
				String[] readResult = stringResult.split("          ");
				int[] splitted = new int[readResult.length]; 							//contiene i singoli valori della stringa [0]=X, [1]=Y, [2]=AP1, [3]=AP2
				for(int i = 0;i < readResult.length;i++)
				{
					splitted[i] = Integer.parseInt(readResult[i]);				
				}				
				
				if((Math.abs(splitted[2] - firstAP) + Math.abs(splitted[3] - secondAP)) < distance) // Metodo NN
				{
					xCord = splitted[0];
					yCord = splitted[1];
					distance = (Math.abs(splitted[2] - firstAP) + Math.abs(splitted[3] - secondAP)); 
				}
			}
		    
		    fileReader.close();		    
		} 
		catch (NullPointerException e)  {e.printStackTrace();} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (NumberFormatException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		if(xCord == -1)
			Toast.makeText(getApplicationContext(), "Something was wrong...", Toast.LENGTH_LONG).show();
		else		
			Toast.makeText(getApplicationContext(), "Position scanned at ----> X: " + xCord + " Y: " + yCord, Toast.LENGTH_LONG).show();
		
		firstAP = 0;										//Re-inizializzo le variabili
		secondAP = 0;    	
    }
    
@Override
	protected void onDestroy() 
{
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