package com.globant.labs.swipper.logic.esculturas;


public class GeoEscultura implements Comparable<GeoEscultura> {

	private int nid;
	private String node_title;
	private String node_category;
	private Double node_latitude;
	private Double node_longitude;
	private Double distance;
	
	private String ciudad;
	private String direccion;
	private String provincia;
	private String telefono;
	
	public int getNid() {
		return nid;
	}
	public void setNid(int nid) {
		this.nid = nid;
	}
	public String getNode_title() {
		return node_title;
	}
	public void setNode_title(String node_title) {
		this.node_title = node_title;
	}
	public String getNode_category() {
		return node_category;
	}
	public void setNode_category(String node_category) {
		this.node_category = node_category;
	}
	public Double getNode_latitude() {
		return node_latitude;
	}
	public void setNode_latitude(Double node_latitude) {
		this.node_latitude = node_latitude;
	}
	public Double getNode_longitude() {
		return node_longitude;
	}
	public void setNode_longitude(Double node_longitude) {
		this.node_longitude = node_longitude;
	}
	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	}
	@Override
	public int compareTo(GeoEscultura another) {
		if (another.getDistance() == this.distance){
			return 0;
		} else if (another.getDistance() > this.distance){
			return -1;
		} else {
			return 1;
		}
	}
	public String getCiudad() {
		return ciudad;
	}
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public String getProvincia() {
		return provincia;
	}
	public void setProvincia(String provincia) {
		this.provincia = provincia;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	
}
