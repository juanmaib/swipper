package com.globant.labs.swipper2.drawer;


public abstract class DrawerItem {

	protected boolean checked;
	protected boolean appliedState;
	
	public abstract String getName();
	public abstract int getColor();
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public boolean isAppliedState() {
		return appliedState;
	}
	
	public void setAppliedState(boolean appliedState) {
		this.appliedState = appliedState;
	}
	
	public void applyState() {
		appliedState = checked; 
	}
	
	public void resetState() {
		checked = appliedState;
	}
	
}
