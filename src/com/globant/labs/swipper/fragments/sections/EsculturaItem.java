package com.globant.labs.swipper.fragments.sections;

import com.globant.labs.swipper.logic.esculturas.Autor;
import com.globant.labs.swipper.logic.esculturas.Escultura;
import com.globant.labs.swipper.logic.esculturas.Foto;
import com.google.android.gms.maps.model.LatLng;

public class EsculturaItem {
	
	private Foto foto;
	private Escultura escultura;
	private Autor autor;
	private String image;
	private LatLng ubicacion;
	
	

	public Autor getAutor() {
		return autor;
	}

	public void setAutor(Autor autor) {
		this.autor = autor;
	}

	public LatLng getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(LatLng ubicacion) {
		this.ubicacion = ubicacion;
	}

	public Foto getFoto() {
		return foto;
	}

	public void setFoto(Foto foto) {
		this.foto = foto;
	}

	public Escultura getEscultura() {
		return escultura;
	}

	public void setEscultura(Escultura escultura) {
		this.escultura = escultura;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
	

}
