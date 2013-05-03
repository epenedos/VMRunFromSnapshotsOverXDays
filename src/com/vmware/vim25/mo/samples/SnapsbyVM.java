package com.vmware.vim25.mo.samples;

import java.util.Date;

public class SnapsbyVM {
	String VMname;
	String SnapName;
	String SnapDescription;
	Date SnapDate;
	int SnapRunningDays;
	String SnapState;
	String SnapBackupManifest;	
	
	
	public SnapsbyVM(String vMname, String snapName, String snapDescription,
			Date snapDate, int snapRunningDays,String snapState,String snapBackupManifest) {
		super();
		VMname = vMname;
		SnapName = snapName;
		SnapDescription = snapDescription;
		SnapDate = snapDate;
		SnapRunningDays = snapRunningDays;
		SnapState=snapState;
		SnapBackupManifest = snapBackupManifest;
	}
	public String getVMname() {
		return VMname;
	}
	public String getSnapName() {
		return SnapName;
	}
	public String getSnapDescription() {
		return SnapDescription;
	}
	public Date getSnapDate() {
		return SnapDate;
	}
	public int getSnapRunningDays() {
		return SnapRunningDays;
	}
	public String getSnapState() {
		return SnapState;
	}
	public String getSnapBackupManifest() {
		return SnapBackupManifest;
	}

}
