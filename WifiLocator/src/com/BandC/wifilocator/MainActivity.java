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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
							
///--------------------------------------------------------------VARIABILI PER ELEMENTI XML-------------------------	

	private TextView    title;
	private TextView 	xCordView;
	private TextView 	yCordView;
	private TextView	scanResult;
	private TextView	NNtx;
	private TextView	KNNtx;
	private TextView	WKNNtx;
	private TextView	NNres;
	private TextView	KNNres;
	private TextView	WKNNres;
	private EditText    xCordText;
	private EditText	yCordText;
	private EditText 	scanNum;
	private EditText    scanIntEd;
	private Button 	    scanBtn;
	private RadioButton trainBtn;
	private RadioButton posBtn;
	
///---------------------------------------------------------------VARIABILI PER JAVA--------------------------------------
	
	private boolean buttonPress = false; 		///Permette di ignorare gli intent in broadcast di sistema
	private int scanNumber;						///Memorizza le scansioni da effettuare
	private int scanInterval;					///Intervallo tra ogni scansione
	private boolean isFirstScan;				///Permette al Receiver di sapere se è stato appena premuto il bottone
	private List<ScanResult> wifiList = null;	///Memorizza i risultati temporanei ottenuti dalle scansioni
	private int count; 							///Conta le scansioni effettuate per ogni operazione
	private boolean wifiIsDisabled;				///Controlla all'avvio se il Wifi era disabilitato
	private File radioMap,config;				///Variabili per i file   
    private int[][] Results = null;				///Contiene coordinate e distanze calcolate
    private int scancount = 1;					///Contatore scansioni effettuate
    private int K,AP;							///K definisce il valore K dei metodi K-NN e WK-NN, AP è il numero degli APs
    private View view;
	private WifiManager mWifiManager = null;
	private Context context = null;	
	private int[] APRes = null;
	private ProgressDialog pd;
	
//------------------------------------------------------------------------------------------------------------------------	
	
	///Apertura App
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {   
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  
        
        view = findViewById(android.R.id.content);
        Animation mLoadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        mLoadAnimation.setDuration(2000);
        view.startAnimation(mLoadAnimation);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                	                 
        title      =  (TextView) 	 findViewById(R.id.textView1);
        xCordView  =  (TextView) 	 findViewById(R.id.x_coord_tx);
        yCordView  =  (TextView)	 findViewById(R.id.y_coord_tx);
        scanResult =  (TextView)     findViewById(R.id.lastScanVw);     
       	NNtx	   =  (TextView)     findViewById(R.id.NN_tx);
    	KNNtx      =  (TextView)     findViewById(R.id.K_NN_tx);
    	WKNNtx     =  (TextView)     findViewById(R.id.WK_NN_tx);
    	NNres      =  (TextView)     findViewById(R.id.NN_res);
    	KNNres     =  (TextView)     findViewById(R.id.K_NN_res);
    	WKNNres    =  (TextView)     findViewById(R.id.WK_NN_res);
        xCordText  =  (EditText)     findViewById(R.id.x_coord_edtx);
        yCordText  =  (EditText)     findViewById(R.id.y_coord_edtx); 
        scanNum    =  (EditText)     findViewById(R.id.scannum_etx);
        scanIntEd  =  (EditText)     findViewById(R.id.scanIntEd);
        trainBtn   =  (RadioButton)  findViewById(R.id.Trainrbtn);
        posBtn     =  (RadioButton)  findViewById(R.id.Positionrbtn);
        scanBtn    =  (Button)       findViewById(R.id.scanrbtn);
                   
        context = getApplicationContext();      
        
        
  /*      ///Scritta arcobaleno del title WIFI Locator
        Shader textShader=new LinearGradient(0, 0, 0, 20,
                new int[]{getResources().getColor(R.color.violet),getResources().getColor(R.color.indigo),
                getResources().getColor(R.color.blue),
                getResources().getColor(R.color.green),
                getResources().getColor(R.color.yellow),
                getResources().getColor(R.color.orange),
                getResources().getColor(R.color.red)},
                new float[]{0,0.2f,0.4f,0.6f,0.8f,0.9f,1}, TileMode.CLAMP);
        title.getPaint().setShader(textShader);
        title.setTextSize(20);
  */      
        radioMap = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/radioMap.txt");  //File memorizzato in variabiles
        config = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/config.txt");
        
        ReadConfig("config.txt");				///Prende i parametri degli APs da scansionare e il valore di K da considerare e crea il file se non esiste
        CheckFile("radioMap.txt"); 				///Crea il file delle scansioni se non esiste
        
        APRes = new int[AP];					///Inizializzo l'array dei risultati per singolo AP
        pd = new ProgressDialog(this);
        
        ///Inizializzo il WifiManager       
		mWifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		mWifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "scanOnly");       			
		if(mWifiManager.isWifiEnabled() == false) 		///Memorizza se all'avvio il wifi era abilitato per disabilitarlo in chiusura
	        	wifiIsDisabled = true;
    }
    
    ///Si avvia al premere del pulsante di scansione      
    public void StartScan (View view)
    {				
    	ProgressDial();
    	DisableButtons();   
    	isFirstScan = true;
       	buttonPress = true;
       	   	   	    	   	
    	if (mWifiManager.isWifiEnabled() == false) 			//Enable wifi if it is disabled
    	{      			
    		Toast.makeText(getApplicationContext(), "Wifi is disabled..Making it enabled", Toast.LENGTH_SHORT).show();
    		mWifiManager.setWifiEnabled(true);
    	} 
    	
    	try   
    	{
    		scanNumber = Integer.parseInt(scanNum.getText().toString()); 
    		scanInterval = Integer.parseInt(scanIntEd.getText().toString()); 
    	}
    	catch (NumberFormatException e) {e.printStackTrace();}
    	
    	scanResult.setText("Scanning...");
    	pd.setMessage("Scan number " + scancount + " of " + scanNumber);

    	registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));   		
    	mWifiManager.startScan();    		    		    
    }  

    ///Nasconde alcuni elementi come da specifica    
    public void Posclick (View view)
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
    	trainBtn.setChecked(false);
    }
    
    ///Abilita alcuni elementi come da specifica   
    public void Trainclick (View view)
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
		scanResult.setText("Wait for scan");
		posBtn.setChecked(false);  	
    } 
    
    public void ProgressDial()
    {   	
   	    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
   	    pd.setMessage("Scan initializing...");
   	    pd.setIndeterminate(true);
   	    pd.setCancelable(false);
   	    pd.setMax(100);
   	    pd.show();  	    
    }
    ///Receiver in Broadcast per le scansioni wifi    
    BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent intent) 
        {
        	
        	unregisterReceiver(wifiReceiver);					///Elimina il receiver...verrà ricreato per una prossima scansione, se necessario
    		    		
        	if (buttonPress == true)							///Determina che la scansione sia inviata dall'utente
        	{		   		   						    		   		    	    								        						   			    					   						///Controlla se e la prima scansione    		    		      				
        		if(isFirstScan == true)			 				///Controlla se è la prima scansione e inzializza il contatore per le prossime
        		{
        			isFirstScan = false;
        			count = scanNumber - 1;
        		}
        		
        		ComputeRSSI();									///Scansiona gli AP presenti e aggiunge il risultato    		    		
       			MakeDecision();			    					///Decide se è tempo di effettuare una nuova scansione oppure se dare dei risultati a seconda del bottone premuto      				     						    	  		    		
  		    }	       	       			         	
        }
    };
    
    ///Abilita tutti gli elementi editabili concluse le operazioni    
    private void EnableButtons()
    {
    	scanBtn.setClickable(true);
    	xCordText.setEnabled(true);
        yCordText.setEnabled(true);
        scanNum.setEnabled(true);
        trainBtn.setClickable(true);
        posBtn.setClickable(true);
        scanIntEd.setEnabled(true);
    }
   
    ///Disabilita tutti gli elementi editabili per completare le operazioni    
    private void DisableButtons()
    {
    	scanBtn.setClickable(false);
    	xCordText.setEnabled(false);
        yCordText.setEnabled(false);
        scanNum.setEnabled(false);
        trainBtn.setClickable(false);
        posBtn.setClickable(false);
        scanIntEd.setEnabled(false);
    }
        
    ///Computa la posizione attuale   
    private void CheckLocation()

    {
		Results = new int[K][2 + AP];
		
		for(int l=0;l<K;l++)
			for(int h=0;h<3;h++)
				Results[l][h] = -1;
		
		for (int l=0;l<K;l++)
			Results[l][2] = Integer.MAX_VALUE;
			  	
		EvaluateDistances();						
						
		if(Results[0][0] == -1) 								//Nessuna distanza memorizzata ---> File vuoto o dati nel file non validi
		{
			Toast.makeText(getApplicationContext(), "File scansioni vuoto o fingerprints < K", Toast.LENGTH_SHORT).show();
			NNres.setText("Error");
			KNNres.setText("Error");
			WKNNres.setText("Error");
			scanResult.setText("Wait for scan");
		}
		else											//Stampo a schermo i risultati
		{
			NNMethod(Results);
			KNNMethod(Results);
			WKNNMethod(Results);
			Toast.makeText(getApplicationContext(), "Position acquired", Toast.LENGTH_LONG).show();
			scanResult.setText("Wait for scan");
		}
    }
   
    ///Scrive Fingerprints sul file radioMap.txt
    private void WriteOnFile()
    {
    	try 
   		{
				CheckFile("radioMap.txt");			//Controlla se il file delle scansioni è stato erroneamente eliminato e in tal caso lo crea
				FileOutputStream fOut = new FileOutputStream("/sdcard/radioMap.txt", true); //creato nuovo stream di output per la scrittura
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(Integer.parseInt(xCordText.getText().toString()) + "          " + Integer.parseInt(yCordText.getText().toString()));
			for(int i=0;i<AP;i++)
				myOutWriter.append("          " + (APRes[i]));
			myOutWriter.append("\n");
			Toast.makeText(getApplicationContext(), "Recorded on file", Toast.LENGTH_LONG).show();
			myOutWriter.close();
			fOut.close();						//Chiuso lo stream correttamente 
    	}  		    		
		catch (IOException e) 			{e.printStackTrace();}
    }

    ///Legge i parametri AP e K dal config.txt che indicano il numero di AP e i K-Nearest AP da considerare nelle scansioni
    private void ReadConfig(String string)
    {
    	CheckFile(string);
    	try 
    	{
    		BufferedReader fileReader = new BufferedReader(new FileReader(config)); 	//Reader android per leggere stringhe da txt
    		StringBuilder line = new StringBuilder();								 	//Variabile della stringa presa dal txt		    					
    		String stringResult = fileReader.readLine();
    		line.append(stringResult);
    		K = Integer.parseInt(stringResult);
    		stringResult = fileReader.readLine();
    		line.append(stringResult);
    		AP = Integer.parseInt(stringResult);
    		fileReader.close();   			
    	}  							 
    	catch (IOException e) {e.printStackTrace();}
    }
   
    ///Controlla l'esistenza del file contenuto in /sdcard/ nominato "string" 
    private void CheckFile(String string)
    {    
    	try 
    	{
    		if (radioMap.exists() == false) 
    		{       
        			radioMap = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/radioMap.txt");
        			radioMap.createNewFile();			
        			FileOutputStream fOut = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/radioMap.txt", true);
        			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        			myOutWriter.append("X" + "          " + "Y" + "         " + "APs in order 1.....N\n");
        			/*for (int i=1;i<=AP;i++)
        			{
        				myOutWriter.append("AP-" + i + "       ");
        			}*/
        			myOutWriter.close(); 
        			fOut.close();
    		}
        	if(config.exists() == false)
        	{
        			config = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/config.txt");
        			config.createNewFile();			
        			FileOutputStream fOut = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/config.txt", true);
        			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        			myOutWriter.append("2\n" );					///----->K value
        			myOutWriter.append("2\n" );					///----->APs value
        			myOutWriter.append("00:3a:98:7d:4a:c2\n" ); ///----->genuawifi infal			
        			myOutWriter.append("44:94:fc:e2:2e:9c\n" ); ///----->miorouter infal
        			myOutWriter.append("00:3a:98:7d:4a:c1\n" ); ///----->eduroam infal
        			myOutWriter.close();        			
        		}
			}		 
        	catch (NullPointerException e)  {e.printStackTrace();} 
        	catch (FileNotFoundException e) {e.printStackTrace();} 
        	catch (IOException e) {e.printStackTrace();}						        
        }
    
    ///Metodo NN   
    private void NNMethod(int[][] results)
    {
    	NNres.setText("X --> " + results[0][0] + "  -  Y --> " + results[0][1]);
    }
    
    ///Metodo K-NN   
    private void KNNMethod(int[][] results)
    {
    	float sumX = 0;
    	for (int i=0;i<K;i++)
    	{
    		sumX = sumX + results[i][0];
    	}
    	float sumY = 0;
    	for (int i=0;i<K;i++)
    	{
    		sumY = sumY + results[i][1];
    	}
    	KNNres.setText("X --> " + String.format("%.3f", sumX/K) + "  -  Y --> " + String.format("%.3f", sumY/K));   	
    }
    
    ///Metodo WK-NN   
    private void WKNNMethod(int[][] results)
    {
    	float sumX = 0;
    	for (int i=0;i<K;i++)
    	{
    		sumX = (float) (sumX + results[i][0]*(1/(Math.pow(results[i][2], 2))));
    	}
    	
    	float sumY = 0;
    	for (int i=0;i<K;i++)
    	{
    		sumY = (float) (sumY + results[i][1]*(1/(Math.pow(results[i][2], 2))));
    	}
    	
    	float peso = 0;
    	for (int i=0;i<K;i++)
    	{
    		peso = (float) (peso + (1/(Math.pow(results[i][2], 2))));
    	}
    	  	  	
    	WKNNres.setText("X --> " + String.format("%.3f", sumX/peso) + "  -  Y --> " + String.format("%.3"
    			+ "f", sumX/peso));   	
    }
   
    ///Computa il segnale scansionato
    private void ComputeRSSI()
    {
    	try 
    	{
    		CheckFile("config.txt");
    		wifiList = mWifiManager.getScanResults();
    		int k = 0;
			boolean stopReader = false;	
			StringBuilder line = new StringBuilder();
			BufferedReader fileReader = new BufferedReader(new FileReader(config)); 		//Reader android per leggere stringhe da txt
										 			
			fileReader.mark(Integer.MAX_VALUE); ///Segnaposto a inizio file per successive riletture dall'inizio
			
    		String stringResult = fileReader.readLine();		///Salto le prime due righe del file di configurazione
    		stringResult = fileReader.readLine();
			for(int i = 0; i < wifiList.size(); i++)
			{
				if(wifiList.get(i).level < -65)
					continue;
				
				stringResult = fileReader.readLine();
				while ((stringResult != null))
				{
					line.append(stringResult);
					  		    					
						if(((wifiList.get(i).BSSID).equals(stringResult)) == false)
						{
							stringResult = fileReader.readLine();
							if(stringResult == null)
							{
								fileReader.reset();
							}
							continue;
						}
						else
						{							
							APRes[k] = APRes[k] + wifiList.get(i).level;
							if(k == AP-1)
								stopReader = true;
							k++;
							fileReader.reset();							
							break;						
						}				   		    					
				}
				
				if (stopReader == true)
					break;
			}
			fileReader.close();
    	}
    	catch (NullPointerException e)  {e.printStackTrace();} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (NumberFormatException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
    }
    
    ///Reinizializza tutte le variabili per una prossima operazione
    private void ResetVar()
    {
    	for(int i=0;i<AP;i++)
    		APRes[i] = 0;
		scancount = 1;
		scanResult.setText("Wait for scan");
		EnableButtons();
		buttonPress = false;  					///Cambio lo stato del bottone per ignorare scansioni di sistema
    }
    
    ///Legge dal file dei record e calcola la matrice delle K minori distanze mettendola in Results[][]
    private void EvaluateDistances()
    {
    	try 
		{			
			BufferedReader fileReader = new BufferedReader(new FileReader(radioMap)); 	//Reader android per leggere stringhe da txt
			StringBuilder line = new StringBuilder();								 	//Variabile della stringa presa dal txt		    
			String stringResult = fileReader.readLine(); 								//Salta la prima linea (è solo testo per indentazione)
			int control = 0;
			
		    while ((stringResult = fileReader.readLine()) != null)
			{
				line.append(stringResult);
				String[] splittedString = stringResult.split("          ");
				int[] splittedInt = new int[splittedString.length]; 					//contiene i singoli valori della stringa del .txt
				for(int i = 0; i < splittedString.length; i++)
				{
					splittedInt[i] = Integer.parseInt(splittedString[i]);				//Splitto la stringa del txt in int[]
				}																		//[0]=X, [1]=Y, [2]=AP1, [3]=AP2 ecc ecc
				
				int distance = 0;
				for(int m=0;m<AP;m++)
					distance = distance + (Math.abs(splittedInt[2+m] - APRes[m]));
				splittedInt[2] = distance;
				
				if(distance < Results[0][2])							//Elabora le K minori distanze
				{														//Results[A][B] --> A = kappesimo risultato
					for(int i=0;i<K-1;i++)								//					B = 0 ----> Cord X
					{													//						1 ----> Cord Y
						Results[K-1-i] = Results[K-2-i];				//						2 ----> Distanza
					}
					Results[0] = splittedInt;			
				}
				for(int m=0;m<K-1;m++)
				{
					if(distance > Results[m][2] && distance < Results[m+1][2])
					{
						for(int i=0;i<K-2;i++)
						{
							Results[K-1-i] = Results[K-2-i];
						}
						Results[m+1] = splittedInt;
					}
				}
				control++;
			}
		    		    
		    fileReader.close();	    												//Chiudo il reader
		    
		    if (control < K)				///se ci sono meno di K record nel file txt, invalido il risultato
		    	Results[0][0] = -1;
		} 
		catch (NullPointerException e)  {e.printStackTrace();} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (NumberFormatException e) {e.printStackTrace();}
		catch (IOException e) 			{e.printStackTrace();}
    }
    
    ///Attende l'intervallo richiesto dall'utente e fa partire una scansione
    private void StartNewScan()
    {
    	count--; 									 //Abbassa il contatore delle scansioni mancanti
		scancount++;
    	Handler handler = new Handler();
		handler.postDelayed(new Runnable() 			 //Attende lo scan interval per lanciare la nuova scansione
		{		 
		    public void run() 
		    {
		    	registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)); //Reinizializzo il receiver per la prossima scansione
    			mWifiManager.startScan();	 //Non appena i risultati sono pronti riparte la funzione OnReceive
		    }
		}, scanInterval*1000);
		scanResult.setText("Scanning...");
		pd.setMessage("Scan number " + scancount + " of " + scanNumber);

		Toast.makeText(getApplicationContext(), "New Scan", Toast.LENGTH_SHORT).show();
    }
    
    ///Decide se è tempo di effettuare una nuova scansione oppure se dare dei risultati a seconda del bottone premuto
    private void MakeDecision()
    {    		
    	if (count != 0)
   		{
   			StartNewScan();
   			return;					  	 ///Ritorna alla main activity,ma tutti i tasti sono bloccati e le scansioni di sistema sono ignorate. Aspetto i risultati della scansione appena lanciata
    	}
   		else
   		{
   			for(int i=0;i<AP;i++)
				APRes[i] = APRes[i]/scanNumber;
   			
   			if(APRes[0] == 0 || APRes[AP-1] == 0)
   			{
   				NNres.setText("");
   				scanResult.setText("Wait for scan");
   				Toast.makeText(getApplicationContext(), "No data from all APs", Toast.LENGTH_SHORT).show();
   			}
   			else
   			{
   				if(trainBtn.isChecked() == true)
   					WriteOnFile();
   				else
   					CheckLocation();   				  
   			}
   			ResetVar();    										///Resetto variabili per prossima operazione
   			pd.dismiss();
   		}
    }
    
    ///Chiusura App
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
		catch(IllegalArgumentException e){e.printStackTrace();}
	}
}