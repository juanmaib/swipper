package com.globant.labs.swipper.logic.esculturas;


public class GeoEscultura implements Comparable<GeoEscultura> {

	private int nid;
	private String node_title;
	private Double node_latitude;
	private Double node_longitude;
	private Double distance;
	
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
	
}
