package com.arcadexxi.androidfulltimerecorder;

import java.util.Vector;

/*
 * Clase con la estructura de datos a devolver por las consultas 
 * de Listas de la actividad principal
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

//
public class MainDataList{
	GlobalController Controller = new GlobalController();
	private Vector<String> TittleAddress;
	private Vector<Integer> TimeInit;
	private Vector<String> CompleteAddress;
	private Vector<Integer> NumFiles;
	private Vector<Integer> Moment_Sound_Group;
	private Vector<String> Hour;
	private Vector<String> Date;
	private Vector<Double> Latitude;
	private Vector<Double> Longitude;
	
	public MainDataList(	Vector<String> tittleAddress, Vector<Integer> timeInit, 
							Vector<String> completeAddress, Vector<Integer> numFiles, Vector<Integer> moment_Sound_Group, 
							Vector<Double> latitude, Vector<Double> longitude){
		
		TittleAddress = tittleAddress;
		TimeInit = timeInit;
		CompleteAddress = completeAddress;
		NumFiles = numFiles;
		Moment_Sound_Group = moment_Sound_Group;
		Latitude = latitude;
		Longitude = longitude;
		
		// Crea los vectores con la hora local y fecha
		Hour = new Vector<String>();
		Date = new Vector<String>();
		for(int timestamp : timeInit){
			Hour.add(Controller.getLocalHour(timestamp));
			Date.add(Controller.getLocalDate(timestamp));
		}
	}
	
	
	public Vector<Double> getLatitude() {
		return Latitude;
	}

	public Vector<Double> getLongitude() {
		return Longitude;
	}
	
	public Vector<String> getTittleAddress() {
		return TittleAddress;
	}

	public Vector<Integer> getTimeInit() {
		return TimeInit;
	}

	public Vector<String> getCompleteAddress() {
		return CompleteAddress;
	}

	public Vector<Integer> getNumFiles() {
		return NumFiles;
	}
	
	public Vector<Integer> getMoment_Sound_Group() {
		return Moment_Sound_Group;
	}
	
	public int NumItems() {
		return Moment_Sound_Group.size();
	}
	
	public Vector<String> getHour() {
		return Hour;
	}
	
	public Vector<String> getDate() {
		return Date;
	}
	
	
}
