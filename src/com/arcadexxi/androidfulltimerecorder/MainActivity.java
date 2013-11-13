package com.arcadexxi.androidfulltimerecorder;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Clase principal de la aplicación, muestra una lista personalizada, 
 * con las grabaciones agrupadas por las zonas donde se han creado.
 * También puede arrancar el servicio de monitorización y configurar las opciones
 * generales de la aplicación 
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class MainActivity extends ListActivity {
	
	GlobalController Controller;
	Model DataAccess;
	ImageView imgGrabadora; 
	TextView tIniciarGrabadora;
	int ScrollPosition = 0; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Instancia las clases necesarias
		Controller = new GlobalController(this);
		DataAccess = new Model(this);
		
		// Limpia los archivos sin marcar como importantes, que han pasado de fecha
		Controller.autoClean();
		
		// Carga las preferencias
		Controller.loadPreferences();
		
		// Carga los componentes del layout necesarios
		imgGrabadora = (ImageView) findViewById(R.id.imgGrabadora); 
		tIniciarGrabadora = (TextView) findViewById(R.id.tIniciarGrabadora);
		
		if(GlobalController.Debug) Log.d("MainActivity", "Evento onCreate del MainActivity");
		
	}
	
	// Al visualizar la actividad
	@Override
	public void onResume(){
		super.onResume();
	
		// Recupera los datos necesarios para la lista ya ordenados por fecha
		MainDataList dataList = DataAccess.getMainList();
		
		// Si hay registros que mostrar
		if(dataList.NumItems() > 0){
			// Crea la lista mediante el adaptador con los datos ya ordenados por fecha
			setListAdapter(new MainListAdapter(	this,
												dataList.getTittleAddress(), 
												dataList.getTimeInit(),
												dataList.getCompleteAddress(),
												dataList.getNumFiles(),
												dataList.getMoment_Sound_Group(),
												dataList.getLatitude(),
												dataList.getLongitude() ));
			
			// Reposiciona la posicion de Scroll de la lista donde estaba la ultima vez
			getListView().setSelectionFromTop(ScrollPosition, 0);
		}
		
		// Recupera el estado del boton que arranca el servicio de grabación
		if(GlobalController.AudioReqState == false){
			imgGrabadora.setImageResource(android.R.drawable.ic_btn_speak_now);
			tIniciarGrabadora.setText("Iniciar Grabación");
		}
		else{
			imgGrabadora.setImageResource(android.R.drawable.ic_media_pause);
			tIniciarGrabadora.setText("Parar Grabación");
		}
	}
	
	// Al pasar la actividad a segundo plano
	@Override
	protected void onPause() {
		
		// Guarda la posicion actual de scroll de la lista, para despues recuperarla
		ScrollPosition = this.getListView().getFirstVisiblePosition();
		
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
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
	
	
	// Arranca o para el servicio de monitorización
	public void onClickMonitor(View v){
		
		// Si el servicio está apagado, intenta encenderlo
		if(GlobalController.AudioReqState == false){
			startService(new Intent(this, AudioReqServ.class));
			GlobalController.AudioReqState = true;
			imgGrabadora.setImageResource(android.R.drawable.ic_media_pause);
			tIniciarGrabadora.setText("Parar Grabación");
		}
		// Si el servicio esta encendido, intenta apagarlo
		else{
			stopService(new Intent(MainActivity.this, AudioReqServ.class));
			GlobalController.AudioReqState = false;
			imgGrabadora.setImageResource(android.R.drawable.ic_btn_speak_now);
			tIniciarGrabadora.setText("Iniciar Grabación");
		}
	}
	
	
	// Actualiza el Main List View
	public void onClickUpdate(View v){
		this.onResume();
	}
	
	// Muestra la actividad con el mapa de google maps
	public void onClickVerMapa(View v){
		Toast.makeText(this, "Ver en mapa la direccion: " + v.getTag(), Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent(this, MapView.class);
		intent.putExtra("LocationStr", v.getTag().toString());
		startActivity(intent);
	}
	
	// Muestra la lista con las grabaciones del item
	public void onClickVerGrabaciones(View v){
		
		int group = (Integer)v.getTag();
		Toast.makeText(this, "Ver grabaciones del grupo: " + group, Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent(this, AudioGrid.class);
		intent.putExtra("group", group);
		startActivity(intent);
	}

}
