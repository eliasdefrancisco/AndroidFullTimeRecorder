package com.arcadexxi.androidfulltimerecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Esta clase se encarga de mostrar los ficheros de audio anexos a cada grupo en un Grid View
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class AudioGrid extends Activity {
	Model DataAccess;
	GlobalController Controller;
	int Group;
	GridView Gridview1;
	TextView 	tTittleAudioGrid, 
				tHourAudioGrid, 
				tDateAudioGrid, 
				tNumFilesAudioGrid, 
				tCompleteAddressAudioGrid;
	int ScrollPosition = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_grid);
		
		// Recibe el identificador del grupo
		Bundle extras = getIntent().getExtras();
		Group = extras.getInt("group");
		
		// Instancia las clases necesarias
		DataAccess = new Model(this);
		Controller = new GlobalController();
		
		// Carga los objetos del layout
		Gridview1 = (GridView) findViewById(R.id.gridView1);
		tTittleAudioGrid = (TextView) findViewById(R.id.tTittleAudioGrid);
		tHourAudioGrid = (TextView) findViewById(R.id.tHourAudioGrid);
		tDateAudioGrid = (TextView) findViewById(R.id.tDateAudioGrid);
		tNumFilesAudioGrid = (TextView) findViewById(R.id.tNumAudiosAudioGrid);
		tCompleteAddressAudioGrid = (TextView) findViewById(R.id.tCompleteAddressAudioGrid);
		
		
	}

	// Al visualizar la actividad
	@Override
	public void onResume(){
		super.onResume();
	
		// Recupera los datos necesarios para la lista ya ordenados por fecha
		AudioDataGrid dataGrid = DataAccess.getAudioGrid(Group);
		
		// Si hay registros que mostrar
		if(dataGrid.getNumItems() > 0){
			
			// Crea la lista mediante el adaptador con los datos ya ordenados por fecha
			Gridview1.setAdapter(new AudioGridAdapter(	this,
														dataGrid.getMomentSound(), 
														dataGrid.getDuration() ));
			
			// Carga los datos de la cabecera
			tTittleAudioGrid.setText(DataAccess.getAddressTittle(Group));
			tHourAudioGrid.setText(Controller.getLocalHour(DataAccess.getAudioMoment(Group)));
			tDateAudioGrid.setText(Controller.getLocalDate(DataAccess.getAudioMoment(Group)));
			tNumFilesAudioGrid.setText(String.valueOf(DataAccess.getNumAudios(Group)));
			tCompleteAddressAudioGrid.setText(DataAccess.getCompleteAddress(Group));
			
			// Reposiciona la posicion de Scroll de la lista donde estaba la ultima vez
			Gridview1.setSelection(ScrollPosition);
		}
		else{
			// Si no hay registros, termina la actividad
			finish();
		}
		
	}
	
	// Al pasar la actividad a segundo plano
	@Override
	protected void onPause() {
		
		// Guarda la posicion actual de scroll de la lista, para despues recuperarla
		ScrollPosition = Gridview1.getFirstVisiblePosition();
		
		super.onPause();
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
	
	public void onClickCassette(View v){
		int moment = (Integer)v.getTag();
		Toast.makeText(this, "Reproducir Audio número: " + moment, Toast.LENGTH_SHORT).show();
		
		Intent intent = new Intent(this, PlayAudio.class);
		intent.putExtra("moment", moment);
		startActivity(intent);
	}

}
