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
//--------------------------------------------------------------VARIABILI PER XML-------------------------	
	
	private RadioButton trainBtn;
	private RadioButton posBtn;
	private TextView 	xCordView;
	private TextView 	yCordView;
	private TextView	scanResult;
	private TextView	scanStatus;
	private EditText    xCordText;
	private EditText	yCordText;
	private EditText 	scanNum;
	private Button 	    scanBtn;
	
//--------------------------------------------------------------VARIABILI PER JAVA--------------------------------------
			
	private boolean buttonPress = false; 						//Permette di ignorare gli intent in broadcast di sistema
	private int scanNumber;										//Memorizza le scansioni da effettuare
	private boolean isFirstScan;								//Permette al Receiver di sapere se è stato appena premuto il bottone
	private List<ScanResult> wifiList = null;					//Memorizza i risultati temporanei ottenuti dalle scansioni
	private int firstAP,secondAP;								//RSSI di ciascun AP
	private int xCord,yCord;									//Variabili per le coordinate
	private int count; 											//conta le scansioni effettuate	
	private boolean wifiIsDisabled;								//Controlla all'avvio se il Wifi era disabilitato
	private WifiManager mWifiManager = null;
	private Context context = null;
	private File file;
		
//--------------------------------------------------------------------------------------------------------------
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                	                   
        xCordView  =  (TextView) 	 findViewById(R.id.x_coord_tx);
        yCordView  =  (TextView)	 findViewById(R.id.y_coord_tx);
        scanResult =  (TextView)     findViewById(R.id.lastScanVw);
        scanStatus =  (TextView)     findViewById(R.id.lastScanTx);
        xCordText  =  (EditText)     findViewById(R.id.x_coord_edtx);
        yCordText  =  (EditText)     findViewById(R.id.y_coord_edtx); 
        scanNum    =  (EditText)     findViewById(R.id.scannum_etx);
        trainBtn   =  (RadioButton)  findViewById(R.id.Trainrbtn);
        posBtn     =  (RadioButton)  findViewById(R.id.Positionrbtn);
        scanBtn    =  (Button)       findViewById(R.id.scanrbtn);
                   
        context = getApplicationContext();
                        
//------------------------------ Check for "radioMap.txt" file in /sdcard/...if doesn't exist the program creates it
        
        File extStore = Environment.getExternalStorageDirectory();
        File myFile = new File(extStore.getAbsolutePath() + "/radioMap.txt");
        
        if (myFile.exists() == false) 
        {       
        	try 
        	{
                File results = new File(extStore.getAbsolutePath() + "/radioMap.txt");
                results.createNewFile();			
                FileOutputStream fOut = new FileOutputStream(extStore.getAbsolutePath() + "/radioMap.txt", true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.append("X" + "          " + "Y" + "         " +  "AP-1" + "       " + "AP-2" + "\n" );
				myOutWriter.close(); 
				fOut.close();				
			}		 
        	catch (NullPointerException e)  {e.printStackTrace();} 
        	catch (FileNotFoundException e) {e.printStackTrace();} 
        	catch (IOException e) {e.printStackTrace();}						        
        }
        
        file = new File(extStore.getAbsolutePath() + "/radioMap.txt");			 //File memorizzato in variabile
             
//--------------------------------------Initialize the WiFi Manager-----------------------------------------------------
        
		mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		
		if(mWifiManager.isWifiEnabled() == false) 								//Memorizza se all'avvio il wifi era abilitato per disabilitarlo in chiusura
	        	wifiIsDisabled = true;
    	
//-----------------------------------------------------------------------------------------------------------------------
    }
       
    BroadcastReceiver wifiReceiver = new BroadcastReceiver()	//Receiver in Broadcast per le scansioni wifi
    {
        @Override
        public void onReceive(Context c, Intent intent) 

        {
        	unregisterReceiver(wifiReceiver);
        	
        	try 
    		{
    		    xCord = Integer.parseInt(xCordText.getText().toString());   		     		   		
    		    yCord = Integer.parseInt(yCordText.getText().toString());
    		    		
    		    if (buttonPress == true)
    		    {		   		   						    		
    		    	FileOutputStream fOut = new FileOutputStream("/sdcard/radioMap.txt", true); //creato nuovo stream di output per la scrittura
    		    	OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    								
    		    	if(trainBtn.isChecked() == true)
    		    	{				   			
    		    		if(isFirstScan == true)
    		    		{
    		    			isFirstScan = false;
    		    			count = scanNumber - 1;
    		    		}
    				   				
    		    		wifiList = mWifiManager.getScanResults();   			
    		    		for(int i = 0; i < wifiList.size(); i++)
    		    		{											    					
    		    			if((wifiList.get(i).BSSID).equals("a0:f3:c1:6c:1e:49") == true || (wifiList.get(i).BSSID).equals("00:3a:98:7d:4a:c1") == true)    					
    		    				firstAP = firstAP + wifiList.get(i).level;
    					
    		    			if((wifiList.get(i).BSSID).equals("00:26:44:74:e9:3e") == true || (wifiList.get(i).BSSID).equals("84:80:2d:c3:a0:72") == true)    					
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
    		    			if(firstAP == 0 || secondAP == 0)    					
    		    				Toast.makeText(getApplicationContext(), "Error while scanning APs...No result found", Toast.LENGTH_LONG).show();    					
    		    			else
    		    			{			
    		    				myOutWriter.append(xCord + "          " + yCord + "          " +  (firstAP/scanNumber) + "          " + (secondAP/scanNumber) + "\n" );
    		    				Toast.makeText(getApplicationContext(), "Recorded on file", Toast.LENGTH_LONG).show();  						
    		    			}
    		    			firstAP = 0;
    		    			secondAP = 0;
    		    			myOutWriter.close();
    		    			fOut.close();
    		    			scanResult.setText("Wait for scan");
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
    			
    		    		scanResult.setText("Evaluating...");
				
    		    		wifiList = mWifiManager.getScanResults();
			
    		    		for(int i = 0; i < wifiList.size(); i++)
    		    		{											    					
    		    			if((wifiList.get(i).BSSID).equals("a0:f3:c1:6c:1e:49") == true || (wifiList.get(i).BSSID).equals("00:3a:98:7d:4a:c1") == true)    					
    		    				firstAP = firstAP + wifiList.get(i).level;
    		    			
    		    			if((wifiList.get(i).BSSID).equals("00:26:44:74:e9:3e") == true || (wifiList.get(i).BSSID).equals("84:80:2d:c3:a0:72") == true)    					
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
    		    			
    		    			if(firstAP == 0 || secondAP == 0)
    		    				scanResult.setText("No APs data");					
    		    			else
    		    				CheckLocation(); 
    		    			
    		    			firstAP = 0;
    		    			secondAP = 0;
    		    			EnableButtons();
    		    		}			    	   			   			
    		    	}   		
    		    	buttonPress = false;    		    		
    		    }
    		    else{}//------------------------------------------------>La scansione è stata inviata dal sistema e va ignorata
    		}
    		catch (NullPointerException e)  {e.printStackTrace();} 
    		catch (FileNotFoundException e) {e.printStackTrace();} 
    		catch (NumberFormatException e) {e.printStackTrace();}
    		catch (IOException e) {e.printStackTrace();}
        }
    };
    	  
    public void EnableButtons()								    //Abilita tutti gli elementi editabili concluse le operazioni
    {
    	scanBtn.setClickable(true);
    	xCordText.setEnabled(true);
        yCordText.setEnabled(true);
        scanNum.setEnabled(true);
        trainBtn.setClickable(true);
        posBtn.setClickable(true);
    }
    
    public void DisableButtons() 								//Disabilita tutti gli elementi editabili per completare le operazioni
    {
    	scanBtn.setClickable(false);
    	xCordText.setEnabled(false);
        yCordText.setEnabled(false);
        scanNum.setEnabled(false);
        trainBtn.setClickable(false);
        posBtn.setClickable(false);
    }
    
    public void StartScan (View view) 							//Si avvia al premere del pulsante di scansione
    {					   	
    	DisableButtons();   
    	scanResult.setText("Scanning...");
    	isFirstScan = true;
       	buttonPress = true;
    	   	    	   	
    	if (mWifiManager.isWifiEnabled() == false) 			//Enable wifi if it is disabled
    	{      			
    		Toast.makeText(getApplicationContext(), "Wifi is disabled..Making it enabled", Toast.LENGTH_SHORT).show();
    		mWifiManager.setWifiEnabled(true);
    	} 
    	
    	try   {scanNumber = Integer.parseInt(scanNum.getText().toString());}
    	catch (NumberFormatException e) {e.printStackTrace();}

    	registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));   		
    	mWifiManager.startScan();    		    		    
    }  

    public void Posclick (View view) 							//Nasconde alcuni elementi come da specifica
    {							
    	xCordView.setVisibility(View.GONE); 
    	yCordView.setVisibility(View.GONE);
    	xCordText.setVisibility(View.GONE);
    	yCordText.setVisibility(View.GONE);
    	scanStatus.setText("Posizione rilevata: ");
    	scanResult.setVisibility(View.VISIBLE);
    	trainBtn.setChecked(false);
    }

    public void Trainclick (View view) 							//Abilita alcuni elementi come da specifica
    {																
    	xCordView.setVisibility(View.VISIBLE);
    	yCordView.setVisibility(View.VISIBLE);
    	xCordText.setVisibility(View.VISIBLE);
    	yCordText.setVisibility(View.VISIBLE);		
		scanStatus.setText("Stato scansione: ");
		scanResult.setText("Wait for scan");
		posBtn.setChecked(false);  	
}
    
    public void CheckLocation()									//Mostra a schermo la posizione attuale
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
				
				if((Math.abs(splitted[2] - firstAP) + Math.abs(splitted[3] - secondAP)) < distance) // Metodo NN (da paper)
				{
					xCord = splitted[0];
					yCord = splitted[1];
					distance = (Math.abs(splitted[2] - firstAP) + Math.abs(splitted[3] - secondAP));					
				}
			}
		    		    
		    fileReader.close();		    												//Chiudo il reader
		} 
		catch (NullPointerException e)  {e.printStackTrace();} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (NumberFormatException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		if(xCord == -1)
		{
			Toast.makeText(getApplicationContext(), "File scansioni vuoto", Toast.LENGTH_SHORT).show();
			scanResult.setText("Error");
		}
		else
		{
			scanResult.setText("X --> " + xCord + "  -  Y --> " + yCord);
			Toast.makeText(getApplicationContext(), "Position acquired", Toast.LENGTH_SHORT).show(); 
		}
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