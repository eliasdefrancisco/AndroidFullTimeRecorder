package com.arcadexxi.androidfulltimerecorder;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/*
 * Clase que mantiene actualizada la posición del dispositivo
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class Situation implements LocationListener {
	private LocationManager situation;
	private String provider;
	Context ParentContext;
	public static Location locationPoint;
	   
	public Situation(Context c){
		ParentContext = c;
		situation = (LocationManager) ParentContext.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = situation.getBestProvider(criteria, true);
		locationPoint = situation.getLastKnownLocation(provider); 

	}
	
	@Override
	public void onLocationChanged(Location location) {
		locationPoint = location;

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
	
	// ---------
	
	public void stopSituation(){
		// Desactivamos notificaciones para ahorrar batería
		situation.removeUpdates(this);
	}
	
	public void startSituation(){
		// Activamos notificaciones de localización (cada 15 minutos (900 segundos) y 300 metros)
        situation.requestLocationUpdates(provider, 900000, 300, this);
	}

}
