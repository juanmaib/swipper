package com.globant.labs.swipper2.drawer;


public abstract class DrawerItem {

	protected boolean checked;
	
	public abstract String getName();
	public abstract int getColor();
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
}
