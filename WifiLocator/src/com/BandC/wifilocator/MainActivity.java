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
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
//--------------------------------------------------------------VARIABILI PER ELEMENTI XML-------------------------	

    private static final int FADEIN_DELAY_MS = 100000;
    private static final int FADEOUT_DELAY_MS = 100000;

    private View root;
    
    
	private RadioButton trainBtn;
	private RadioButton posBtn;
	private TextView 	xCordView;
	private TextView 	yCordView;
	private TextView	scanResult;
	private TextView	scanStatus;
	private TextView	NNtx;
	private TextView	KNNtx;
	private TextView	WKNNtx;
	private TextView	NNres;
	private TextView	KNNres;
	private TextView	WKNNres;
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
	private int count; 											//Conta le scansioni effettuate	per ogni operazione
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
        runFadeAnimationOn(this, root, true, FADEIN_DELAY_MS);
                	                 
        xCordView  =  (TextView) 	 findViewById(R.id.x_coord_tx);
        yCordView  =  (TextView)	 findViewById(R.id.y_coord_tx);
        scanResult =  (TextView)     findViewById(R.id.lastScanVw);
        scanStatus =  (TextView)     findViewById(R.id.lastScanTx);
       	NNtx	   =  (TextView)     findViewById(R.id.NN_tx);
    	KNNtx      =  (TextView)     findViewById(R.id.K_NN_tx);
    	WKNNtx     =  (TextView)     findViewById(R.id.WK_NN_tx);
    	NNres      =  (TextView)     findViewById(R.id.NN_res);
    	KNNres     =  (TextView)     findViewById(R.id.K_NN_res);
    	WKNNres    =  (TextView)     findViewById(R.id.WK_NN_res);
        xCordText  =  (EditText)     findViewById(R.id.x_coord_edtx);
        yCordText  =  (EditText)     findViewById(R.id.y_coord_edtx); 
        scanNum    =  (EditText)     findViewById(R.id.scannum_etx);
        trainBtn   =  (RadioButton)  findViewById(R.id.Trainrbtn);
        posBtn     =  (RadioButton)  findViewById(R.id.Positionrbtn);
        scanBtn    =  (Button)       findViewById(R.id.scanrbtn);
                   
        context = getApplicationContext();      
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/radioMap.txt");  //File memorizzato in variabiles        
        CheckFile(); 							//Crea il file delle scansioni se non esiste
        
//--------------------------------------Initialize the WiFi Manager-----------------------------------------------------
        
		mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		mWifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "scanOnly");
       	
		
		if(mWifiManager.isWifiEnabled() == false) 								//Memorizza se all'avvio il wifi era abilitato per disabilitarlo in chiusura
	        	wifiIsDisabled = true;
    	
//-----------------------------------------------------------------------------------------------------------------------
    }
       
    BroadcastReceiver wifiReceiver = new BroadcastReceiver()	//Receiver in Broadcast per le scansioni wifiz
    {
        @Override
        public void onReceive(Context c, Intent intent) 
        {
        	try 
    		{
        		unregisterReceiver(wifiReceiver);					//Elimina il receiver...verrà ricreato per una prossima scansione, se necessario
    		    		
    		    if (buttonPress == true)							//Determina che la scansione sia inviata dall'utente
    		    {		   		   						    		   		    	    								
    		    	if(trainBtn.isChecked() == true)//---------------------------->Modalità TRAIN
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
    		    			count--; 					 //Abbassa il contatore delle scansioni mancanti		
    		    			registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); //Reinizializzo il receiver per la prossima scansione
    		    			mWifiManager.startScan();	 //Non appena i risultati sono pronti riparte la funzione OnReceive
    		    			return;						 //Ritorna alla main activity,ma tutti i tasti sono bloccati e le scansioni di sistema sono ignorate. Aspetto i risultati della scansione appena lanciata
    		    		}
    		    		else
    		    		{
    		    			if(firstAP == 0 || secondAP == 0)    					
    		    				Toast.makeText(getApplicationContext(), "Error while scanning APs...No result found", Toast.LENGTH_LONG).show();    					
    		    			else
    		    			{
    		    				CheckFile();			//Controlla se il file delle scansioni è stato erroneamente eliminato e in tal caso lo crea
    		    				FileOutputStream fOut = new FileOutputStream("/sdcard/radioMap.txt", true); //creato nuovo stream di output per la scrittura
    		    		    	OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
    		    				myOutWriter.append(Integer.parseInt(xCordText.getText().toString()) + "          " + Integer.parseInt(yCordText.getText().toString()) + "          " +  (firstAP/scanNumber) + "          " + (secondAP/scanNumber) + "\n" );
    		    				Toast.makeText(getApplicationContext(), "Recorded on file", Toast.LENGTH_LONG).show();
        		    			myOutWriter.close();
        		    			fOut.close();																//Chiuso lo stream correttamente
    		    			}
    		    			firstAP = 0;
    		    			secondAP = 0;
    		    			scanResult.setText("Wait for scan");
    		    			EnableButtons();							//Risultati computati, posso riabilito tutte le funzioni
    		    		}			    			
    		    	}
    		    	
    		    	if(posBtn.isChecked() == true)//---------------------------->Modalità POSITION
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
    		    			count--; 					 //Abbassa il contatore delle scansioni mancanti		
    		    			registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); //Reinizializzo il receiver per la prossima scansione
    		    			mWifiManager.startScan();	 //Non appena i risultati sono pronti riparte la funzione OnReceive
    		    			return;						 //Ritorna alla main activity,ma tutti i tasti sono bloccati e le scansioni di sistema sono ignorate. Aspetto i risultati della scansione appena lanciata
    		    		}
    		    		else
    		    		{  	
    		    			firstAP = (firstAP/scanNumber);
    		    			secondAP = (secondAP/scanNumber);
    		    			
    		    			if(firstAP == 0 || secondAP == 0)		//Non ho ricevuto alcun dato (sono rimaste a 0 le variabili)
    		    				NNres.setText("No APs data");					
    		    			else
    		    				CheckLocation();		//Funzione che computa la posizione e la stampa a schermo 
    		    			
    		    			firstAP = 0;				//Computata la posizione, reinizializzo le variabili e abilito tutte le funzioni
    		    			secondAP = 0;
    		    			EnableButtons();
    		    		}			    	   			   			
    		    	}   		
    		    	buttonPress = false;  				//Cambio lo stato del bottone per ignorare scansioni di sistema  		    		
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
    	NNtx.setVisibility(View.VISIBLE);
    	KNNtx.setVisibility(View.VISIBLE);
    	WKNNtx.setVisibility(View.VISIBLE);
    	NNres.setVisibility(View.VISIBLE);
    	KNNres.setVisibility(View.VISIBLE);
    	WKNNres.setVisibility(View.VISIBLE);
    	scanResult.setVisibility(View.VISIBLE);
    	scanStatus.setText("Posizione rilevata: ");
    	trainBtn.setChecked(false);
    }

    public void Trainclick (View view) 							//Abilita alcuni elementi come da specifica
    {																
    	xCordView.setVisibility(View.VISIBLE);
    	yCordView.setVisibility(View.VISIBLE);
    	xCordText.setVisibility(View.VISIBLE);
    	yCordText.setVisibility(View.VISIBLE);
    	NNtx.setVisibility(View.GONE);
    	KNNtx.setVisibility(View.GONE);
    	WKNNtx.setVisibility(View.GONE);
    	NNres.setVisibility(View.GONE);
    	KNNres.setVisibility(View.GONE);
    	WKNNres.setVisibility(View.GONE);
		scanStatus.setText("Stato scansione: ");
		scanResult.setText("Wait for scan");
		posBtn.setChecked(false);  	
}
    
    public void CheckLocation()									//Mostra a schermo la posizione attuale
    {   	
    	int distance = Integer.MAX_VALUE;						
    	int NNxCord = -1;
		int NNyCord = -1;
    	
		try 
		{			
			BufferedReader fileReader = new BufferedReader(new FileReader(file)); 		//Reader android per leggere stringhe da txt
			StringBuilder line = new StringBuilder();								 	//Variabile della stringa presa dal txt		    
			String stringResult = fileReader.readLine(); 								//Salta la prima linea (è solo testo per indentazione)
			
		    while ((stringResult = fileReader.readLine()) != null)
			{
				line.append(stringResult);
				String[] splittedString = stringResult.split("          ");
				int[] splittedInt = new int[splittedString.length]; 					//contiene i singoli valori della stringa del .txt
				for(int i = 0; i < splittedString.length; i++)
				{
					splittedInt[i] = Integer.parseInt(splittedString[i]);				//Splitto la stringa del txt in int[]
				}																		//[0]=X, [1]=Y, [2]=AP1, [3]=AP2

				if((Math.abs(splittedInt[2] - firstAP) + Math.abs(splittedInt[3] - secondAP)) < distance) // Metodo NN
				{
					NNxCord = splittedInt[0];
					NNyCord = splittedInt[1];
					distance = (Math.abs(splittedInt[2] - firstAP) + Math.abs(splittedInt[3] - secondAP));					
				}
				
			}
		    		    
		    fileReader.close();		    												//Chiudo il reader
		} 
		catch (NullPointerException e)  {e.printStackTrace();} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (NumberFormatException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		if(NNxCord == -1) 								//Nessuna distanza memorizzata ---> File vuoto o dati nel file non validi
		{
			Toast.makeText(getApplicationContext(), "File scansioni vuoto", Toast.LENGTH_SHORT).show();
			NNres.setText("Error");
			scanResult.setText("Wait for scan");
		}
		else											//Stampo a schermo i risultati
		{
			NNres.setText("X --> " + NNxCord + "  -  Y --> " + NNyCord);
			Toast.makeText(getApplicationContext(), "Position acquired", Toast.LENGTH_SHORT).show();
			scanResult.setText("Wait for scan");
		}
    }
    
    public void CheckFile()										//Controlla l'esistenza del file radioMap.txt
    {        
        if (file.exists() == false) 
        {       
        	try 
        	{
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/radioMap.txt");
                file.createNewFile();			
                FileOutputStream fOut = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/radioMap.txt", true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.append("X" + "          " + "Y" + "         " +  "AP-1" + "       " + "AP-2" + "\n" );
				myOutWriter.close(); 
				fOut.close();				
			}		 
        	catch (NullPointerException e)  {e.printStackTrace();} 
        	catch (FileNotFoundException e) {e.printStackTrace();} 
        	catch (IOException e) {e.printStackTrace();}						        
        }
        else{}
    }
    
    private void runFadeAnimationOn(Activity ctx, View target, boolean in, int delay) {
        int start, finish;
        if (in) {
            start = 0;
            finish = 1;
        } else {
            start = 1;
            finish = 0;
        }
        try
        {
        AlphaAnimation fade = new AlphaAnimation(start, finish); 
        fade.setDuration(delay); 
        fade.setFillAfter(true); 
        target.startAnimation(fade);
        }
        catch(NullPointerException e){}
    }    
    
    public void finishFade() {
        final int delay = FADEOUT_DELAY_MS;
        runFadeAnimationOn(this, root, false, delay);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivity.super.finish();
            }
        }).start();
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