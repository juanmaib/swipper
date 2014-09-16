package com.globant.labs.swipper2.drawer;

import com.globant.labs.swipper2.R;

public class DrawerAllItem extends DrawerItem {

	public DrawerAllItem() {
		super();
		
		setAppliedState(true);
		setChecked(true);
	}
	
	@Override
	public String getName() {
		return "All Categories";
	}

	@Override
	public int getColor() {
		return R.color.dark_grey;
	}

}
