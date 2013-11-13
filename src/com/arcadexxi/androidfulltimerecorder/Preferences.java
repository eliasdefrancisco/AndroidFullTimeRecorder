package com.arcadexxi.androidfulltimerecorder;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
/*
 * Esta clase se encarga de lanzar el menu de opciones y 
 * almacenar los valores en las preferencias
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */
public class Preferences extends PreferenceActivity {
	
	
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	// Guarda las preferencias
	@Override
	protected void onStop(){
		GlobalController Controller = new GlobalController(this);
		if(GlobalController.Debug) Log.d("Preferences", "Guardando preferencias") ; 
		
		// Recarga las variables con las preferencias seleccionadas por el usuario
		Controller.loadPreferences();
		
		super.onStop();
	}
}
