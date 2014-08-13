package com.globant.labs.swipper.logic.esculturas;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Evento implements Comparable<Evento>{

	public String date;
	public String nombre;
	public String hora_inicio;
	public String hora_fin;
	public String lugar;
	public String organizador;
	
	public Evento(String date, String nombre, String hora_inicio,
			String hora_fin, String lugar, String organizador) {
		super();
		this.date = date;
		this.nombre = nombre;
		this.hora_inicio = hora_inicio;
		this.hora_fin = hora_fin;
		this.lugar = lugar;
		this.organizador = organizador;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getHora_inicio() {
		return hora_inicio;
	}
	public void setHora_inicio(String hora_inicio) {
		this.hora_inicio = hora_inicio;
	}
	public String getHora_fin() {
		return hora_fin;
	}
	public void setHora_fin(String hora_fin) {
		this.hora_fin = hora_fin;
	}
	public String getLugar() {
		return lugar;
	}
	public void setLugar(String lugar) {
		this.lugar = lugar;
	}
	public String getOrganizador() {
		return organizador;
	}
	public void setOrganizador(String organizador) {
		this.organizador = organizador;
	}
	@Override
	public int compareTo(Evento another) {
				
		DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm");
		DateTime time = dateStringFormat.parseDateTime(String.valueOf(date.replaceAll("\\s+", "")+" "+hora_inicio.replaceAll("\\s+", "")).trim());
		DateTime theirTime = dateStringFormat.parseDateTime(String.valueOf(another.getDate().replaceAll("\\s+", "")+" "+another.getHora_inicio().replaceAll("\\s+", "")).trim());
		
		return time.compareTo(theirTime);	
				
	}
	@Override
	public String toString() {
		return "Evento Bienal - " + nombre + " \nLugar: " + lugar
				+ " \nOrganiza: " + organizador;
	}
	
	
	
}
