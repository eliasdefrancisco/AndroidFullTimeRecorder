package com.arcadexxi.androidfulltimerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * Clase para la visualización de direcciones en el mapa de google
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class MapView extends FragmentActivity {
	
	private LatLng Position;
	private GoogleMap mapa;
	String Address = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_view);
		
		// Recibe el identificador del fichero de audio
		Bundle extras = getIntent().getExtras();
		String locationStr = extras.getString("LocationStr");
		
		// Separa la cadena recibida en lineas, 
		//las dos primeras corresponden a la Latitud y Longitud, el resto a la direccion traducida
		String[] locationAux = locationStr.split(" \\| ");
		double latitud=0, longitud=0;
		int i=0;
		for(String line : locationAux){
			if(i==0) latitud = Double.valueOf(line); 
			else if(i==1) longitud = Double.valueOf(line); 
			else Address += line + "\n";
			i++;
		}
		Position = new LatLng(latitud, longitud);
		//Toast.makeText(this, 	"Latitud: " + latitud + " Longitud: " + longitud + " Address: " + Address.firstElement().toString(), Toast.LENGTH_SHORT).show();
	
		// Muestra el google map, indicando la posición
		viewMap();
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Intent i;
		
		switch(item.getItemId()){
		case R.id.about:
			i = new Intent(this, About.class);
			startActivity(i);
			break;
		case R.id.config:
			i = new Intent(this, Preferences.class);
			startActivity(i);
			break;
		
		}
		return true;
	}
	
	// Muestra el google map, indicando la posición
	public void viewMap(){
		mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
		mapa.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(Position, 15));
		mapa.setMyLocationEnabled(true);
		mapa.getUiSettings().setZoomControlsEnabled(true);
		mapa.getUiSettings().setCompassEnabled(true);
		
		mapa.addMarker(new MarkerOptions()
			.position(Position)
			.title("Dirección")
			.snippet(Address.toString())
			.icon(BitmapDescriptorFactory.fromResource(android.R.drawable.ic_menu_mylocation))
			.anchor(0.5f, 0.5f));
		
	}

}
