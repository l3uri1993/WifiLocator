package com.BandC.wifilocator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.widget.Toast;

public class WifiActivity extends BroadcastReceiver {
	
	private Context c;
    public WifiActivity(Context context)
    {
         c= context;
    }
	private List<ScanResult> wifiList = null;
	private int scanNumber;	
	private int CASAres,OSPITEres,xCord,yCord;
	private String checkMAC; //utilizzo questo per escludere le reti non desiderate

	// This method call when number of wifi connections changed
	public void onReceive(Context c, Intent intent) {
		
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
			Toast.makeText(c, "Scanning...", Toast.LENGTH_LONG).show();
			
			wifiList = MainActivity.mWifiManager.getScanResults();
			
			for(int i = 0; i < wifiList.size(); i++){
				
/*				checkMAC = wifiList.get(i).BSSID;				
    			if(checkMAC.equals("a0:f3:c1:6c:1e:49") == true)
    				CASAres = wifiList.get(i).level;
    			if(checkMAC.equals("00:26:44:74:e9:3e") == true) 
    				OSPITEres = wifiList.get(i).level;						A CASA MIA INSERISCO QUESTO CODICE */
    								
				myOutWriter.append(wifiList.get(i).SSID + "      " + wifiList.get(i).BSSID + "      " + wifiList.get(i).level + "\n" );			
			}
//			myOutWriter.append(xCord + "   " + yCord + "   " +  CASAres + "   " + OSPITEres + "\n" ); A CASA MIA INSERISCO QUESTO CODICE
			
			myOutWriter.close();
			fOut.close();
			
			Toast.makeText(c, "Recorded on file", Toast.LENGTH_LONG).show();
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
		
		if(MainActivity.posbtn.isChecked() == true)
		{	
			Toast.makeText(c, "No action performed", Toast.LENGTH_LONG).show();
		}
		MainActivity.buttonPress = false;
		}
		else{}
    }
}