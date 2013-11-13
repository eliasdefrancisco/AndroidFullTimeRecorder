package com.arcadexxi.androidfulltimerecorder;

import java.util.Vector;

/*
 * Clase con la estructura de datos a devolver por las consultas 
 * de Listas de Grabaciones de audio
 * 
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class AudioDataGrid {
	GlobalController Controller = new GlobalController();
	private Vector<Integer> MomentSound;
	private Vector<Integer> Duration;
	private Vector<String> Hour;
	private Vector<String> Seconds;

	public AudioDataGrid(Vector<Integer> momentSound, Vector<Integer> duration){
		MomentSound = momentSound;
		Duration = duration;
		
		// Crea el vector con la hora local y otro con los segundos
		Hour = new Vector<String>();
		Seconds = new Vector<String>();
		for(int timestamp : MomentSound){
			Hour.add(Controller.getLocalHour(timestamp));
		}
		for(int seg : Duration){
			Seconds.add(String.valueOf(seg));
		}
	}
	
	public int getNumItems() {
		return MomentSound.size();
	}
	
	public Vector<Integer> getMomentSound() {
		return MomentSound;
	}

	public Vector<Integer> getDuration() {
		return Duration;
	}
	
	public Vector<String> getHour() {
		return Hour;
	}

	public Vector<String> getSeconds() {
		return Seconds;
	}
	
	
}
