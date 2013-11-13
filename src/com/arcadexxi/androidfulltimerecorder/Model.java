package com.arcadexxi.androidfulltimerecorder;

import java.io.File;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/*
 * Esta clase pretende recoger todas las funciones relativas al acceso a datos.
 * Creación de tablas, Consutas SQL, ...
 * 
 * Autor: Elías de Francisco Javaloyes
 * Fecha: 29/10/2013
 * Creado para el curso de aplicaciones en Android http://www.androidcurso.com/
 * 
 * 
 */

public class Model extends SQLiteOpenHelper{
	
	// ------ Funciones de creación de la base de datos -------------------
	
	public Model(Context context){
		super(context, "AFTR_DB", null, 3);
		
		if(GlobalController.Debug) Log.d("Model", "Clase Model Iniciada");
		
	}

	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Crea las tablas, si no estan creadas
		if(GlobalController.Debug) Log.d("Model", "Creando Base de Datos");
		
		db.execSQL(	"CREATE TABLE SoundReq(" +
						"moment INTEGER PRIMARY KEY, " +
						"duration INTEGER, " +
						"marked INTEGER DEFAULT 0, " +
						"comment TEXT," +
						"latitude REAL DEFAULT 36.69," +
						"longitude REAL DEFAULT -6 " +
						")");
		
		db.execSQL(	"CREATE TABLE SoundDraw(" +
						"moment_Sound INTEGER, " +
						"secuence INTEGER, " +
						"valDraw INTEGER, " +
						"PRIMARY KEY (moment_Sound, secuence) " +
						"FOREIGN KEY (moment_Sound) REFERENCES SoundReq (moment) ON DELETE CASCADE" +
					")");
		
		db.execSQL(	"CREATE TABLE Address(" +
						"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"tittleAddress TEXT, " +
						"completeAddress TEXT" +
					")");
		
		db.execSQL(	"CREATE TABLE Sounds_Addresses(" +
						"moment_Sound INTEGER, " +
						"id_Address INTEGER, " +
						"moment_Sound_Group INTEGER, " +
						"PRIMARY KEY (moment_Sound, id_Address) " +
						"FOREIGN KEY (moment_Sound) REFERENCES SoundReq (moment) ON DELETE CASCADE " +
						"FOREIGN KEY (id_Address) REFERENCES Address (id) ON DELETE CASCADE" +
					")");
			
		// Reserva el registro 0 para el sistema
		//db.execSQL(	"INSERT INTO Address VALUES(0, 'Reserva el primer registro para el sistema', 0)" );

	}

	// Actaliza la bbdd en caso de version antigua
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		
		if(oldVersion == 1){
			db.execSQL(	"ALTER TABLE SoundReq ADD COLUMN marked INTEGER DEFAULT 0");
			db.execSQL(	"ALTER TABLE SoundReq ADD COLUMN comment TEXT");
			oldVersion++;
		}
		
		if(oldVersion == 2){
			db.execSQL(	"ALTER TABLE SoundReq ADD COLUMN latitude REAL DEFAULT 36.69");
			db.execSQL(	"ALTER TABLE SoundReq ADD COLUMN longitude REAL DEFAULT -6");
			oldVersion++;
		}
	}
	
	
	// ------ Funciones de acceso a datos (Consultas, Inserciones, Actualizaciones, ...) -----------
	
	// Registra el fichero de sonido en la BBDD
	public void SaveSound(int moment, int duration, double latitude, double longitude){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(	"INSERT INTO SoundReq VALUES(" + moment + ", " + duration + ", 0, '', " + latitude + ", " + longitude + ")" );
	}
	
	// Alamcena los valores de la gráfica del sonido en la BBDD
	public void SaveDraw(int moment_Sound, Vector<Integer> valsDraw){
		SQLiteDatabase db = getWritableDatabase();
		int sec=0;
		for(Integer valDraw : valsDraw){
			db.execSQL(	"INSERT INTO SoundDraw VALUES(" + 
							moment_Sound + ", " + 
							sec + ", " +
							valDraw.intValue() + 
							")" );
			sec++;
		}
		
	}
	
	// Alacena la dirección actual, asociandola con el fichero de sonido, 
	// comprobando previamente su existencia
	static int moment_Sound_Group = -1; // Referencia para agrupar los sonidos en un deteminado lugar
	static long last_idAddress = -1; // Referencia de la dirección anterior, acorde con el grupo 
	public void SaveAddress(int moment_Sound, Vector<String> address){
		
		SQLiteDatabase db = getWritableDatabase();
		String tittleAddress = "";
		String completeAddress = "";
		int line = 0;
		// Rellena las variables con las cadenas de las direcciones
		for(String addressLine : address){
			if(line == 0){
				tittleAddress = completeAddress = addressLine;
			}else{
				completeAddress += " | " + addressLine;
			}
			line ++;
		}	
		
		Cursor res = db.rawQuery(	"SELECT id FROM Address WHERE " +
										"tittleAddress LIKE '" + tittleAddress + "' AND " + 
										"completeAddress LIKE '" + completeAddress + "'", 
									null);
		
		// Si la dirección ya estaba registrada, extrae el idAddress
		long idAddress;
		if(res.getCount() > 0){
			res.moveToFirst();
			idAddress = res.getLong(0);
		}
		// Si la dirección aun NO había sido registrada, la crea y extrae el idAddress
		else{
			ContentValues values = new ContentValues();
			values.put("tittleAddress", tittleAddress);
			values.put("completeAddress", completeAddress);
			// Uso el metodo "insert" en vez de "rawQuery", porque "insert" me devuelve el id
			idAddress = db.insert(	"Address", 
									"tittleAddress, completeAddress", 
									values);
		}
		
		// Se comprueba si el sonido pertenece al grupo anterior (se ha producido en la misma dirección)
		if(idAddress != last_idAddress){
			last_idAddress = idAddress;
			moment_Sound_Group = moment_Sound;
		}
		
		// Establece la relación entre el Sound y la Address
		db.execSQL(	"INSERT INTO Sounds_Addresses VALUES(" 	
						+ moment_Sound + ", " + idAddress + ", " + moment_Sound_Group + ")" );

	}
	
	// Devuelve los elementos del ListView de la actividad principal ordenados
	// en un tipo de dato especifico para este proposito (MainDataList)
	public MainDataList getMainList(){
		SQLiteDatabase db = getWritableDatabase();
		Vector<String> tittleAddress = new Vector<String>();
		Vector<Integer> timeInit = new Vector<Integer>();
		Vector<String> completeAddress = new Vector<String>();
		Vector<Integer> numFiles = new Vector<Integer>();
		Vector<Integer> moment_Sound_Group = new Vector<Integer>();
		Vector<Double> latitude = new Vector<Double>();
		Vector<Double> longitude = new Vector<Double>();
		
		
		Cursor res = db.rawQuery(	"SELECT tittleAddress, moment_Sound, completeAddress, COUNT(*) numFiles, moment_Sound_Group, latitude, longitude " +
										"FROM Sounds_Addresses " +
										"INNER JOIN Address ON Sounds_Addresses.id_Address = Address.id " +
										"INNER JOIN SoundReq ON Sounds_Addresses.moment_Sound = SoundReq.moment " +
										"GROUP BY moment_Sound_Group " +
										"ORDER BY moment_Sound DESC ", 
									null);
		
		while(res.moveToNext()){
			tittleAddress.add(res.getString(0));
			timeInit.add(res.getInt(1));
			completeAddress.add(res.getString(2));
			numFiles.add(res.getInt(3));
			moment_Sound_Group.add(res.getInt(4));
			latitude.add(res.getDouble(5));
			longitude.add(res.getDouble(6));
		}
		
		MainDataList lista = new MainDataList(tittleAddress, timeInit, completeAddress, numFiles, moment_Sound_Group, latitude, longitude);
		return lista;
	}
	
	// Devuelve los elementos del GridView que muestra las grabaciones,
	// en un tipo de dato especifico para este proposito (AudioDataGrid)
	public AudioDataGrid getAudioGrid(int group){
		SQLiteDatabase db = getWritableDatabase();
		Vector<Integer> MomentSound = new Vector<Integer>();
		Vector<Integer> Duration = new Vector<Integer>();
		
		Cursor res = db.rawQuery(	"SELECT moment, duration  " +
										"FROM SoundReq " +
										"INNER JOIN Sounds_Addresses ON Sounds_Addresses.moment_Sound = SoundReq.moment " +
										"WHERE moment_Sound_Group = " + group + " " +
										"GROUP BY moment_Sound " +
										"ORDER BY moment_Sound DESC ", 
									null);
		
		while(res.moveToNext()){
			MomentSound.add(res.getInt(0));
			Duration.add(res.getInt(1));
		}
		
		AudioDataGrid lista = new AudioDataGrid(MomentSound, Duration);
		return lista;
	}
	
	// Devuelve el titulo de la dirección, pasandole como parametro el ID del grupo
	public String getAddressTittle(int group){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT tittleAddress  " +
										"FROM Address " +
										"INNER JOIN Sounds_Addresses ON Sounds_Addresses.id_Address = Address.id " +
										"WHERE moment_Sound_Group = " + group + " " +
										"GROUP BY moment_Sound ", 
									null);	
		res.moveToFirst();
		return res.getString(0);
	}
	
	// Devuelve la hora de inicio de la grabación, pasandole como parametro el ID del grupo
	public int getAudioMoment(int group){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT moment_Sound  " +
										"FROM Sounds_Addresses " +
										"WHERE moment_Sound_Group = " + group + " " +
										"GROUP BY moment_Sound ", 
									null);	
		res.moveToFirst();
		return res.getInt(0);
	}
	
	// Devuelve el número de grabaciones, pasandole como parametro el ID del grupo
	public int getNumAudios(int group){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT COUNT(*) numFiles " +
										"FROM Sounds_Addresses " +
										"WHERE moment_Sound_Group = " + group, 
									null);
		res.moveToFirst();
		return res.getInt(0);
	}
	
	// Devuelve la dirección completa, pasandole como parametro el ID del grupo
	public String getCompleteAddress(int group){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT completeAddress  " +
										"FROM Address " +
										"INNER JOIN Sounds_Addresses ON Sounds_Addresses.id_Address = Address.id " +
										"WHERE moment_Sound_Group = " + group + " " +
										"GROUP BY moment_Sound ", 
									null);	
		res.moveToFirst();
		return res.getString(0);
	}
	
	// Devuelve los valores de la grafica para el fichero pasado como parametro
	public Vector<Integer> getValsDraw(int idMoment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT valDraw  " +
										"FROM SoundDraw " +
										"WHERE moment_Sound = " + idMoment + " " +
										"ORDER BY secuence ", 
									null);	
		Vector<Integer> vals = new Vector<Integer>();
		while(res.moveToNext()){
			vals.add(res.getInt(0));
		}
		
		return vals;
	}
	
	// Devuelve el titulo de la dirección de un solo fichero de audio, pasado como parametro
	public String getAddressTittleSound(int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT tittleAddress  " +
										"FROM Address " +
										"INNER JOIN Sounds_Addresses ON Sounds_Addresses.id_Address = Address.id " +
										"WHERE moment_Sound = " + moment + " " +
										"GROUP BY moment_Sound ", 
									null);	
		res.moveToFirst();
		return res.getString(0);
	}
	
	// Devuelve la hora de inicio de la grabación de un solo fichero de audio, pasado como parametro
	public int getAudioMomentSound(int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT moment_Sound  " +
										"FROM Sounds_Addresses " +
										"WHERE moment_Sound = " + moment + " " +
										"GROUP BY moment_Sound ", 
									null);	
		res.moveToFirst();
		return res.getInt(0);
	}
	
	// Devuelve la duración de un solo fichero de audio, pasado como parametro
	public int getDurationSound(int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT duration " +
										"FROM SoundReq " +
										"WHERE moment = " + moment, 
									null);
		res.moveToFirst();
		return res.getInt(0);
	}
	
	// Devuelve la dirección completa de un solo fichero de audio, pasado como parametro
	public String getCompleteAddressSound(int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT completeAddress  " +
										"FROM Address " +
										"INNER JOIN Sounds_Addresses ON Sounds_Addresses.id_Address = Address.id " +
										"WHERE moment_Sound = " + moment + " " +
										"GROUP BY moment_Sound ", 
									null);	
		res.moveToFirst();
		return res.getString(0);
	}
	
	// Marca un archivo como Importante (1) o Prescindible (0)
	public void setAudioMarked (int moment, int mark){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(	"UPDATE SoundReq SET marked = " + mark + " WHERE moment = " + moment);
	}
	
	// Actualiza el comentario de un fichero de audio
	public void setAudioComment (int moment, String comment){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(	"UPDATE SoundReq SET comment = '" + comment + "' WHERE moment = " + moment);
	}
	
	// Devuelve el marcado de un archivo, Importante (1) o Prescindible (0)
	public int getAudioMarked (int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT marked  " +
										"FROM SoundReq " +
										"WHERE moment = " + moment, 
									null);	
		res.moveToFirst();
		return res.getInt(0);
	}
	
	// Devuelve el cometario de un archivo
	public String getAudioComment (int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT comment  " +
										"FROM SoundReq " +
										"WHERE moment = " + moment, 
									null);	
		res.moveToFirst();
		return res.getString(0);
	}
	
	// Devuelve el Identificador del fichero anterior al pasado como parametro
	public int getPrev (int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT MAX(moment)  " +
										"FROM SoundReq " +
										"WHERE moment < " + moment, 
									null);	
		res.moveToFirst();
		if(res.getInt(0) > 0){
			return res.getInt(0);
		}
		else{
			return -1; // No hay mas registros anteriores
		}	
	}
	
	// Devuelve el Identificador del siguiente fichero, al pasado como parametro
	public int getNext (int moment){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT MIN(moment)  " +
										"FROM SoundReq " +
										"WHERE moment > " + moment, 
									null);	
		res.moveToFirst();
		if(res.getInt(0) > 0){
			return res.getInt(0);
		}
		else{
			return -1; // No hay mas registros anteriores
		}
		
	}
	
	// Borra toda la estructura de un fichero en la BBDD y lo elimina del sistema de archivos
	public void deleteSound (int moment){
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(	"DELETE FROM SoundDraw WHERE moment_Sound = " + moment);	
		db.execSQL(	"DELETE FROM Sounds_Addresses WHERE moment_Sound = " + moment);
		db.execSQL(	"DELETE FROM SoundReq WHERE moment = " + moment);
		
		File f = new File(GlobalController.FilePath + moment + ".3gp");
		f.delete();
	}
	
	// Devuelve todos los Ids (moment) de los ficheros mas antiguos
	// que la fecha pasada como parametro, que no esten marcados como importantes
	public Vector<Integer> getOutOfDate(long dateForOut){
		SQLiteDatabase db = getWritableDatabase();
		Cursor res = db.rawQuery(	"SELECT moment " +
										"FROM SoundReq " +
										"WHERE moment < " + dateForOut + " " +
										"AND marked <> 1 ", 
									null);	
		
		Vector<Integer> ids = new Vector<Integer>();
		while(res.moveToNext()){
			ids.add(res.getInt(0));
		}
		return ids;
	}
}








