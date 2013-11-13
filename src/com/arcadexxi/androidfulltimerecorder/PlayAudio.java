package com.arcadexxi.androidfulltimerecorder;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

/*
 * Esta clase muestra la información del fichero de audio, y permite
 * reproducirlos, comentarlos y manipularlos en general
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

// Para evitar el teclado al comenzar la actividad:  Manifest -> Activity -> android:windowSoftInputMode="stateHidden"


public class PlayAudio extends Activity {
	int Moment_Sound;
	GraphView graphView;
	Model DataAccess;
	GlobalController Controller;
	LinearLayout lyDrawGraph;
	TextView 	tTittle,
				tHour,
				tDate,
				tDuration,
				tCompleteAddress;
	SeekBar SeekBar01;
	MediaPlayer mPlayer;
	String FileName;
	AsyncProgressBar AsyncProgressBar;
	ToggleButton bMark;
	EditText eComment;
	
	
	// Clase AsyncTask, encargada de ir avanzando la barra de progreso en un bucle,
	// mientras avanza el estado de la reproducción ---
	class AsyncProgressBar extends AsyncTask<Integer, Integer, Void>{
		
		public boolean Looping = false;
				
		@Override
		protected Void doInBackground(Integer... arg0) {
			while(Looping){
				try {
					Thread.sleep(250);
					if(mPlayer != null){
						publishProgress(mPlayer.getCurrentPosition());
					}
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override 
		protected void onProgressUpdate(Integer... porc) {
            SeekBar01.setProgress(porc[0]);
		}
		
	} 
	
	// --- Fin del AsincTask
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_audio);
		if(GlobalController.Debug == true) Log.d("PlayAudio", "OnCreate");
		
		// Clases globales de la app
		DataAccess = new Model(this);
		Controller = new GlobalController();
		
		// Recibe el identificador del fichero de audio
		Bundle extras = getIntent().getExtras();
		Moment_Sound = extras.getInt("moment");
		
		// Establece la ruta de acceso al fichero
		FileName = GlobalController.FilePath + Moment_Sound + ".3gp";		
				
		// Carga los objetos del layout
		lyDrawGraph = (LinearLayout) findViewById(R.id.lyDrawGraph);
		tTittle = (TextView) findViewById(R.id.tTittlePlay);
		tHour = (TextView) findViewById(R.id.tHourPlay);
		tDate = (TextView) findViewById(R.id.tDatePlay);
		tDuration = (TextView) findViewById(R.id.tNumAudiosPlay);
		tCompleteAddress = (TextView) findViewById(R.id.tCompleteAddressPlay);
		bMark = (ToggleButton) findViewById(R.id.tbMark);
		eComment = (EditText) findViewById(R.id.tComentario); 
		
		// Muestra la grafica de sonido
		drawGraphic();
	}
	
	// Al visualizar la actividad
	@Override
	public void onResume(){
		super.onResume();
		if(GlobalController.Debug == true) Log.d("PlayAudio", "OnResume");
		
		// Muestra la información de la cabecera
		tTittle.setText(DataAccess.getAddressTittleSound(Moment_Sound));
		tHour.setText(Controller.getLocalHour(DataAccess.getAudioMomentSound(Moment_Sound)));
		tDate.setText(Controller.getLocalDate(DataAccess.getAudioMomentSound(Moment_Sound)));
		tDuration.setText(String.valueOf(DataAccess.getDurationSound(Moment_Sound)));
		tCompleteAddress.setText(DataAccess.getCompleteAddressSound(Moment_Sound));
		
		// Recupera el estado del boton de marcaje de arcivos
		if(DataAccess.getAudioMarked(Moment_Sound) == 1){
			bMark.setChecked(true);
			bMark.setCompoundDrawablesWithIntrinsicBounds(0, android.R.drawable.btn_star_big_on, 0, 0);
		}else{
			bMark.setChecked(false);
			bMark.setCompoundDrawablesWithIntrinsicBounds(0, android.R.drawable.btn_star_big_off, 0, 0);
		}
		
		// Recupera el comentario del archivo
		eComment.setText(DataAccess.getAudioComment(Moment_Sound));
		
		// Crea el objeto Media Player
		mPlayerCreate();
		
		// Crea la barra usada para mostrar el avance de la reproducción
		seekBarCreate();
		
		// Instancia el AsincTask y comienza la ejecucion
		AsyncProgressBar = new AsyncProgressBar();
		AsyncProgressBar.Looping = true;
		AsyncProgressBar.execute(mPlayer.getCurrentPosition());
	}
	
	// Cuando la aplicacion pasa a segundo plano...
	@Override
	public void onPause() {
		if(GlobalController.Debug == true) Log.d("PlayAudio", "OnPause");
		
		// Temina el AsincTask
		AsyncProgressBar.Looping = false;
		
		// Termina el Media Player
		mPlayerStop();
		
		// Guarda el comentario del archivo
		DataAccess.setAudioComment(Moment_Sound, eComment.getText().toString());
		
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
	
	// Muestra la grafica de sonido en el layout
	private void drawGraphic(){
		// Rellena los datos de la grafica 
		graphView = new LineGraphView(this , "" );
		GraphViewData[] data = new GraphViewData[DataAccess.getValsDraw(Moment_Sound).size()];
		
		int i = 0, max = 0;
		for(int val : DataAccess.getValsDraw(Moment_Sound)){
			// Imprime los valores en texto plano
			data[i] = new GraphViewData(i, val);
			i++;
			// Busca el valor máximo de la gráfica
			if(val > max) 
				max = val;
		}
		GraphViewSeries datos = new GraphViewSeries("Muestras de sonido", new GraphViewSeriesStyle(Color.BLUE, 3), data);
	    
	    // Borra los valores antiguos si los habia
		graphView.removeAllSeries();
		
		// Carga los valores de la grafica
	    graphView.addSeries(datos);
	    
	    // Elimina las etiquetas de la grafica
	    graphView.setHorizontalLabels(new String[] {""});  
	    graphView.setVerticalLabels(new String[] {""});  
	    graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
	    
	    // Estilo la grafica
	    graphView.getLayoutParams().height = 300;
	    graphView.setPadding(25, 0, 25, 0);
	    //graphView.setBackground(getResources().getDrawable(R.drawable.backgroung_list_item));
	    
	    
	    // Instancia la vista de la grafica en el layout
	    lyDrawGraph.addView(graphView,0);
	}
	
	
	// Crea el objeto mPlayer
	public void mPlayerCreate(){
		mPlayer = new MediaPlayer();
		
		// Establece el evento del mPlayer para cuando se termina de cargar el archivo de audio
		mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() 
	    {           
	        public void onPrepared(MediaPlayer mp) 
	        {
	        	// Establece la barra multimedia al tamaño del fichero de audio
	    		SeekBar01.setMax(mPlayer.getDuration());
	        }           
	    });
		
		// Establece el evento del mPlayer para cuando finaliza la reproduccion
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() 
	    {           
	        public void onCompletion(MediaPlayer mp) 
	        {
	        	// Ejecuta las tareas que deben ejecutarse al parar la reproduccion
	        	mPlayerStop();
	        }           
	    });
			
		// Prepara la reproducción
		try{
			mPlayer.setDataSource(FileName);
			mPlayer.setVolume(1, 1); // Maximo volumen (Left Right)
			mPlayer.prepare();
			
		}catch(IOException e){
			Log.e("PlayAudio","Fallo en la reproducción");
		}
	}
	
	// Para la reproduccion del fichero de audio
	public void mPlayerStop(){
		if(mPlayer != null){
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		// Pasa la barra de posicionamiento del audio al inicio (Rebobina)
		SeekBar01.setProgress(0);
	}

	// Inicia la reproduccion del fichero de audio
	public void mPlayerPlay(View v){
		if(mPlayer == null){
			mPlayerCreate();
		}
		// TODO: ActualizaThread = true;
		mPlayer.seekTo(SeekBar01.getProgress());
		mPlayer.start();	
		
	}
	
	// Pausa la reproduccion del fichero de audio
	public void mPlayerPause(View v){
		// TODO: ActualizaThread = false;
		if(mPlayer != null){
			mPlayer.pause();
		}
	}
	
	
	// Crea la barra multimedia para el control de la reproducción, estableciendo sus eventos
	public void seekBarCreate(){
		SeekBar01 = (SeekBar) findViewById(R.id.seekBar01);
		
		// Establece el evento para cuando se pulse sobre la barra multimedia
		SeekBar01.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Cambia la posicion de la reproducción, si esta está activa
				if(mPlayer != null){
					mPlayer.seekTo(SeekBar01.getProgress());
				}
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	// Cambia es estado de marcado de un archivo (Importante 1 , Prescindible 0)
	public void onClickMark(View v){
		
		// Si NO es importante
		if(DataAccess.getAudioMarked(Moment_Sound) != 1){
			DataAccess.setAudioMarked(Moment_Sound, 1);
			bMark.setChecked(true);
			bMark.setCompoundDrawablesWithIntrinsicBounds(0, android.R.drawable.btn_star_big_on, 0, 0);
		}else{
			DataAccess.setAudioMarked(Moment_Sound, 0);
			bMark.setChecked(false);
			bMark.setCompoundDrawablesWithIntrinsicBounds(0, android.R.drawable.btn_star_big_off, 0, 0);
		}
	}
	
	// Visualiza el fichero previo al actual
	public void onClickPrev(View v){
		int momentPrev = DataAccess.getPrev(Moment_Sound);
		if(GlobalController.Debug == true) Log.d("PlayAudio", "momentPrev: " + momentPrev);
		
		// Si existen ficheros previos, pasa al siguiente fichero
		if(momentPrev > -1){
			Intent intent = new Intent(this, PlayAudio.class);
			intent.putExtra("moment", momentPrev);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Aseguramos que solo hay una actividad igual abierta
			startActivity(intent);
			finish();
		}else{
			Toast.makeText(this, "No existen mas ficheros anteriores", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Visualiza el fichero previo al actual
	public void onClickNext(View v){
		int momentNext = DataAccess.getNext(Moment_Sound);
		if(GlobalController.Debug == true) Log.d("PlayAudio", "momentPrev: " + momentNext);
		
		// Si existen ficheros previos, pasa al siguiente fichero
		if(momentNext > -1){
			Intent intent = new Intent(this, PlayAudio.class);
			intent.putExtra("moment", momentNext);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Aseguramos que solo hay una actividad igual abierta
			startActivity(intent);
			finish();
		}else{
			Toast.makeText(this, "No existen mas ficheros posteriores", Toast.LENGTH_SHORT).show();
		}
	}
	
	// Borra un fichero pidiendo confirmación
	public void onClickDel(View v) {
		
		new AlertDialog.Builder(this)
	    .setTitle("Confirmacion de borrado")
	    .setMessage("Realmente desea borrar el archivo?")
	    .setPositiveButton(	getResources().getString(android.R.string.ok), 
	    					new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // Borra y cierra la actividad
	        	DataAccess.deleteSound(Moment_Sound);
	    		finish();
	        }
	     })
	    .setNegativeButton(	getResources().getString(android.R.string.no), 
	    					new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
		
	}
	
	// Envia el fichero de audio por los canales disponibles para compartir
	public void onClickShare(View v) {
		File F = new File(FileName);
		Uri uri = Uri.fromFile(F);
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("audio/3gpp");
		i.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(i,"Audio File " + Controller.getDateTime(DataAccess.getAudioMomentSound(Moment_Sound))));
	}

}
