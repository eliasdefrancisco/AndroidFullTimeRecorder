package com.arcadexxi.androidfulltimerecorder;

import java.util.Vector;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Clase encargada de gestionar el GridView con la lista de grabaciones
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class AudioGridAdapter extends BaseAdapter {
	AudioDataGrid DataGrid;
	Activity ActivityParent;
	
	public AudioGridAdapter(Activity activityParent, Vector<Integer> moment, Vector<Integer> duration){
		super();
		ActivityParent = activityParent;
		DataGrid = new AudioDataGrid(moment, duration);
	}
	
	@Override
	public int getCount() {
		return DataGrid.getNumItems();
	}

	@Override
	public Object getItem(int arg0) {
		return DataGrid.getMomentSound().elementAt(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return DataGrid.getMomentSound().elementAt(arg0);
	}

	@Override
	public View getView(int pos, View arg1, ViewGroup arg2) {
		
		// Construye el layout con los elementos del GridView que vamos a manipular
		LayoutInflater inflater = ActivityParent.getLayoutInflater();
		View layoutElements = inflater.inflate(R.layout.audio_element_grid, null, true);
		
		TextView tAudioHour = (TextView) layoutElements.findViewById(R.id.tAudioHour);
		TextView tAudioDuration = (TextView) layoutElements.findViewById(R.id.tAudioDuration);
		ImageView imgCassette = (ImageView) layoutElements.findViewById(R.id.imgCassette);
		
		// Establece los valores para cada campo en la lista
		tAudioHour.setText(DataGrid.getHour().elementAt(pos).toString());
		tAudioDuration.setText(DataGrid.getSeconds().elementAt(pos).toString());
		imgCassette.setTag(DataGrid.getMomentSound().elementAt(pos));
		
		return layoutElements;
	}

}
