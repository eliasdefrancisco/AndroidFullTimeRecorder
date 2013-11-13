package com.arcadexxi.androidfulltimerecorder;

import java.util.Vector;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * Clase encargada de gestionar el contenido del listView principal
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class MainListAdapter extends BaseAdapter {

	MainDataList DataList;
	Activity ActivityParent;
	
	public MainListAdapter(	Activity activity, Vector<String> tittleAddress, Vector<Integer> timeInit, 
							Vector<String> completeAddress, Vector<Integer> numFiles, Vector<Integer> moment_Sound_Group,
							Vector<Double> latitude, Vector<Double> longitude){
		super();
		ActivityParent = activity;
		DataList = new MainDataList(tittleAddress, timeInit, completeAddress, numFiles, moment_Sound_Group, latitude, longitude);	
	}
	
	@Override
	public int getCount() {
		return DataList.NumItems();
	}

	@Override
	public Object getItem(int arg0) {
		return DataList.getMoment_Sound_Group().elementAt(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return DataList.getMoment_Sound_Group().elementAt(arg0);
	}

	@Override
	public View getView(int pos, View arg1, ViewGroup arg2) {
		
		// Construye el layout con los elementos de la lista que vamos a manipular
		LayoutInflater inflater = ActivityParent.getLayoutInflater();
		View layoutElements = inflater.inflate(R.layout.main_element_list, null, true);
		
		TextView tTittleAddress = (TextView) layoutElements.findViewById(R.id.tTittleAddress);
		TextView tCompleteAddress = (TextView) layoutElements.findViewById(R.id.tCompleteAddress);
		TextView tHour = (TextView) layoutElements.findViewById(R.id.tHour);
		TextView tDate = (TextView) layoutElements.findViewById(R.id.tDate);
		TextView tFiles = (TextView) layoutElements.findViewById(R.id.tFiles);
		LinearLayout lyVerMapa = (LinearLayout) layoutElements.findViewById(R.id.lyVerMapa);
		LinearLayout lyVerGrabaciones = (LinearLayout) layoutElements.findViewById(R.id.lyVerGrabaciones);
		
		// Establece los valores para cada campo en la lista
		tTittleAddress.setText(DataList.getTittleAddress().elementAt(pos));
		tCompleteAddress.setText(DataList.getCompleteAddress().elementAt(pos));
		tHour.setText(DataList.getHour().elementAt(pos));
		tDate.setText(DataList.getDate().elementAt(pos));
		tFiles.setText(String.valueOf(DataList.getNumFiles().elementAt(pos)));
		lyVerMapa.setTag(DataList.getLatitude().elementAt(pos) + " | " + DataList.getLongitude().elementAt(pos) + " | " + DataList.getCompleteAddress().elementAt(pos));
		lyVerGrabaciones.setTag(DataList.getMoment_Sound_Group().elementAt(pos));
		
		return layoutElements;
	}

}
