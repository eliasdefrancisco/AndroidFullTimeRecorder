package com.arcadexxi.androidfulltimerecorder;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

/*
 * Esta clase se arranca como un Servicio desde un receptor de anuncios (OnBootReceiver) que la llama en el arranque del sistema.
 * Es la encargada de ir almacenando los ficheros de audio y registrarlos con sus respectivos datos en la BBDD
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class AudioReqServ extends Service {

	// ------ Variables Globales  (a nives de clase) --------------------
	
	GlobalController Controller = new GlobalController(this);
	Model DataAccess = new Model(this);
	int SampleTime = 1000; // Tiempo entre muestras de sonido (1 segundo)
	boolean UpdateMonitorLoop = false; // Controla el loop de muestreo 
	UpdateMonitorThread UpdateMonitor;
	MediaRecorder mRecorder;
	int FileMoment;
	String FileName;
	int NumAnalizedSample;
	int NumSamplesWithoutNoise;
	final int MaxSamplesWithoutNoise = 5; // Tiempo máximo que un fichero de audio puede estar sin sonido en segundos
	final int MaxSamples = 300; // Tiempo máximo que puede durar un fichero de audio
	boolean SampleWithNoise; // Indica si la mustra actual contiene ruido
	Vector<Integer> NoiseValue = new Vector<Integer>();
	Situation situation = null ;
	private NotificationManager nm;
	private static final int ID_NOTIFICACION_CREAR = 1;
	
	
	// ----- Funciones propias de la clase Service ---------
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		if(GlobalController.Debug) Log.d("AudioReqServ", "Evento onCreate del servicio AudioReqServ");

		// Comienza el bucle de muestreo
		UpdateMonitorLoop = true;
		UpdateMonitor = new UpdateMonitorThread(); 
		UpdateMonitor.start();
		
		// Instancia la clase Situation, que se encarga de mantener la localizacion del dispositivo
		situation = new Situation(this);
		situation.startSituation();
		
		// Muestra una notificacion en la barra de estado mientra el servicio esté activo
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification notificacion = new Notification(android.R.drawable.ic_notification_overlay, getResources().getString(R.string.monitor_on), System.currentTimeMillis() );
        PendingIntent intencionPendiente = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0); // En el manifest, la actividad está como singletop, para que solo pueda haber una instancia de la misma
        notificacion.setLatestEventInfo(this, getResources().getString(R.string.app_name), getResources().getString(R.string.grabacion_activa), intencionPendiente);
        nm.notify(ID_NOTIFICACION_CREAR, notificacion); 
	}
	
	@Override
	public int onStartCommand(Intent intenc, int flags, int idArranque) {
		if(GlobalController.Debug) Log.d("AudioReqServ", "Evento onStartCommand del servicio AudioReqServ");
		//return START_STICKY; // Recarga el servicio si fue destruido por el sistema
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		if(GlobalController.Debug) Log.d("AudioReqServ", "Evento onDestroy del servicio AudioReqServ");
		
		// Finaliza el bucle de muestreo
		UpdateMonitorLoop = false;
		
		// Finaliza la última grabación
		stopMonitor();
		
		// Destruye la notificación de la barra de estado
		nm.cancel(ID_NOTIFICACION_CREAR);
	}
	
	
	// --------- Hilo para la monitorizacion --------------
	
	class UpdateMonitorThread extends Thread {
		@Override
		public void run(){
			while(UpdateMonitorLoop){
				try {
					// Para el hilo un tiempo
					monitorizar();
					Thread.sleep(SampleTime);
					
				} catch (InterruptedException e) {}
			}
		}
	}
	
	
	// --------- Funciones de la clase AudioReqServ --------------
	
	// Se encarga de grabar el audio y actualizar las muestras de la gráfica
	public void monitorizar(){
		
		// Si no está grabando 
		if(mRecorder == null){
			if(GlobalController.Debug) Log.d("AudioReqServ", "Iniciando grabación");
			
			// Inicializa las varables de control del sonido
			NumAnalizedSample = 0;
			NumSamplesWithoutNoise = 0;
			NoiseValue.clear();
			SampleWithNoise = false;
			
			// Crea un nuevo archivo de sonido y comienza la grabación
			FileMoment = (int) (System.currentTimeMillis() / 1000);
			FileName = GlobalController.FilePath + String.valueOf(FileMoment) + ".3gp";
			configMediaRecorder();
			try {
				mRecorder.prepare();
				mRecorder.start();
			} catch (IOException e) {
				if(GlobalController.Debug) Log.e("AudioReqServ", "Excepcion mRecorder (prepare,start): " + e.getMessage());
				stopMonitor();
			}
			
			
			
		}
		// Si ya se está grabando algún archivo, analiza la muestra actual en busca de un silencio largo para parar la grabación
		else{
			// Si no se ha sobrepasado el número maximo de muestras a analizar NI el número maximo de muestras a analizar sin ruido minimo
			if((NumAnalizedSample < MaxSamples) && (NumSamplesWithoutNoise < MaxSamplesWithoutNoise)){
				if(GlobalController.Debug) Log.d("AudioReqServ", "Recogiendo nueva muestra de sonido");
				
				// Examina la muestra actual en busca de ruido
				int actualSample = mRecorder.getMaxAmplitude();
				if(GlobalController.Debug) Log.d("AudioReqServ", "Valor Muestra: " + actualSample);
				
				if(actualSample > GlobalController.MinNoiseVolume){
					SampleWithNoise = true;
					NumSamplesWithoutNoise = 0;
				}
				else{
					NumSamplesWithoutNoise++;
				}
				
				// Guarda el valor en la lista de valores
				NoiseValue.add(actualSample);
				NumAnalizedSample++;
		
			// Si se sobrepaso el numero de muestras sin ruido, cierra el archivo y lo borra si no hubo ruidos 
			}else{
				stopMonitor();
			}
		}
		
	}
	
	// Configura el MediaRecorder
	private void configMediaRecorder(){
		mRecorder = new MediaRecorder();
		if(GlobalController.Debug) Log.d("AudioReqServ", "configMediaRecorder FileName : " + FileName);
		mRecorder.setOutputFile(FileName);
		mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
		// Crea la ruta a los archivos si no estaba ya creada
		File file = new File(GlobalController.FilePath);
		if(!file.exists()){
			file.mkdirs();
		}
	}
	
	// Detiene la grabación, si existe
	private void stopMediaRecorder(){
		if(mRecorder != null){
			try{
				mRecorder.stop();
			}catch(RuntimeException e){
				Log.d("aviso", "Fallo mRecorder1.Stop: " + e.getMessage());
			}finally{
				mRecorder.reset();
				mRecorder.release();
				mRecorder = null;
			}
		}
	}
	
	// Decide si guarda o no el fichero y las muestras una vez finaliza la grabación
	private void stopMonitor(){
		if(GlobalController.Debug) Log.d("AudioReqServ", "Parando Monitorización");
		
		stopMediaRecorder();
	
		// Si NO hubo ruido, borra el archivo y NO guarda nada en la BBDD
		if(!SampleWithNoise){
			if(GlobalController.Debug) Log.d("AudioReqServ", "Descartando fichero de audio");
			File f = new File(FileName);
			f.delete();
		// Si hubo ruido, No borra el archivo y almacena sus datos y muestras en la BBDD
		}else{
			if(GlobalController.Debug) Log.d("AudioReqServ", "Guardando Sonido en la BBDD");
			
			// Registra el fichero de sonido en la BBDD
			int actualMoment = (int) (System.currentTimeMillis() / 1000);
			
			DataAccess.SaveSound(	FileMoment, 
									actualMoment - FileMoment, 
									Situation.locationPoint.getLatitude(),
									Situation.locationPoint.getLongitude());
			
			// Almacena los valores de la gráfica del sonido en la BBDD
			DataAccess.SaveDraw(FileMoment, NoiseValue);
			
			// Guarda la lista de cadenas con la dirección actual en un vector y lo guarda en la BBDD, 
			// asociandolo al fichero de sonido actual
			Vector<String> address = (Vector<String>) Controller.GiveMeAddress();
			DataAccess.SaveAddress(FileMoment, address);
			
		}
	}	
}
