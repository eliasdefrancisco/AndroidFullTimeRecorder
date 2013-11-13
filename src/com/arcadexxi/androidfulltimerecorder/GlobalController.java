package com.arcadexxi.androidfulltimerecorder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;

/*
 * Esta clase pretende recoger todas las variables y funciones, 
 * usadas a nivel global por la aplicación
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class GlobalController {
	
	// ---- Variables globales -------
	static String FilePath = Environment.getExternalStorageDirectory() + "/Android/data/com.arcadexxi.androidfulltimerecorder/files/";
	static int MinNoiseVolume = 500; // Sonido mínimo de una muestra para considerar grabar el audio 
	Context ParentContext;
	Model DataAccess;
	static boolean Debug = true;
	static boolean AudioReqState = false;
	static long AutoCleanTime = 259200; // Tiempo máximo en segundos, que puede permanecer un archivo sin ser borrado, si no está marcado como importante  

	// Llamada al metodo básica
	public GlobalController(){
		DataAccess = new Model(ParentContext);
		
		// Crea la ruta de los ficheros de audio, si no está creada previamente
		File f = new File(FilePath);
		if(!f.exists()) f.mkdirs();
	
	}
	
	// Sobrecarga el metodo de llamada para poder usar metodos que requieran usar contexto
	public GlobalController(Context c){
		ParentContext = c;
		DataAccess = new Model(ParentContext);
		
		// Crea la ruta de los ficheros de audio, si no está creada previamente
		File f = new File(FilePath);
		if(!f.exists()) f.mkdirs();
	
	}
	
	// Devuelve un vector con las cadenas respectivas a la dirección de la posición actual
	// (Requiere que la clase sea llamada indicando el contexto)
	public Vector<String> GiveMeAddress(){
		Geocoder geocoder = new Geocoder(ParentContext, Locale.getDefault());
		List<Address> infoLocale = null;
		Vector<String> address = new Vector<String>();
		Location locationPoint = Situation.locationPoint;
		
		try {
			infoLocale = geocoder.getFromLocation(locationPoint.getLatitude(), locationPoint.getLongitude(), 1);
			for(int i=0;  i <= infoLocale.get(0).getMaxAddressLineIndex(); i++){
				address.add(infoLocale.get(0).getAddressLine(i));
			}
			return address;
			
		} catch (IOException e) {
			e.printStackTrace();
			address.add(ParentContext.getResources().getString(R.string.imposible_averiguar));
			address.add("Lat: " + locationPoint.getLatitude());
			address.add("Lon: " + locationPoint.getLongitude());
		}
		
		return address;
	}
	
	// Devuelve una cadena con la hora local desde un Timestamp pasado como parametro
	public String getLocalHour(int timestamp){
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
		return dateFormat.format((long) timestamp * 1000);
	}
	
	// Devuelve una cadena con la hora local desde un Timestamp pasado como parametro
	public String getLocalDate(int timestamp){
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy");
		return dateFormat.format((long) timestamp * 1000);
	}
	
	// Devuelve una cadena con la hora y fecha desde un Timestamp pasado como parametro
	public String getDateTime(int timestamp){
		SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy - HH:mm");
		return dateFormat.format((long) timestamp * 1000);
	}
	
	// Limpia los archivos que nos han sido marcados como importantes y han pasado de fecha
	public void autoClean(){
		long timeForOut = System.currentTimeMillis() / 1000;
		timeForOut -= AutoCleanTime;
		Vector<Integer> ids = DataAccess.getOutOfDate(timeForOut);
		
		for(int id : ids){
			DataAccess.deleteSound(id);
		}
		
	}
	
	// Carga las preferencias del menu en las variables pertinentes
	public void loadPreferences(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ParentContext);
		MinNoiseVolume =  Integer.valueOf(pref.getString("NoiseLevel", "500"));
		AutoCleanTime = Long.valueOf(pref.getString("OutOfDate", "259200"));
		
	}
	
	
}
