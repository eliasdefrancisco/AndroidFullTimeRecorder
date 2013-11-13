package com.arcadexxi.androidfulltimerecorder;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

/*
 * Clase que arranca la música y animaciones con creditos y agradecimientos
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class About extends Activity {
	MediaPlayer mPlayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);		
	}
	
	@Override
	protected void onResume(){
		super.onResume();

		// Comienza el bucle de música
		mPlayer = MediaPlayer.create(this, R.raw.pop_up);
		mPlayer.setVolume(1, 1); // Maximo volumen (Left Right)	
		mPlayer.setLooping(true);
		mPlayer.start();
		
		// Inicia la animación del texto
		RelativeLayout lyTexto = (RelativeLayout) findViewById(R.id.lyAboutText);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.about_animation_01);
		lyTexto.startAnimation(anim);
	}
	
	@Override
	protected void onPause(){
		// Finaliza la reproducción
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
		
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

}
