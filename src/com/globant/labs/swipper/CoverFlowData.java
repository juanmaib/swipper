package com.globant.labs.swipper;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class CoverFlowData {

	private ArrayList<Bitmap> bm;
	private ArrayList<String> strURL;
	private ArrayList<Integer> obraID;
	private ArrayList<String> str;
	
	
	public ArrayList<Bitmap> getBm() {
		return bm;
	}
	public void setBm(ArrayList<Bitmap> bm) {
		this.bm = bm;
	}
	public ArrayList<String> getStr() {
		return str;
	}
	public void setStr(ArrayList<String> str) {
		this.str = str;
	}
	public ArrayList<String> getStrURL() {
		return strURL;
	}
	public void setStrURL(ArrayList<String> strURL) {
		this.strURL = strURL;
	}
	public ArrayList<Integer> getObraID() {
		return obraID;
	}
	public void setObraID(ArrayList<Integer> obraID) {
		this.obraID = obraID;
	}
	
	
}
