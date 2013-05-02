package com.vmware.vim25.mo.samples;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.*;

import com.vmware.vim25.mo.samples.SnapsbyVM;

public class VMRunFromSnapshotsOverXDays {
	


	static int nrDays = 0;
	static ArrayList<SnapsbyVM> list = new ArrayList();
	static String VMname;
	
	public static void main(String[] args) throws Exception
	{
		if(args.length!=4)
	    {
	      System.out.println("Usage: java VMRunFromSnapshotsOverXDays <vCenter url> " +
	      		"<username> <password> <Number of Days Running from Snapshots>");
	    
	      System.exit(0);
	    }
		
		nrDays = Integer.parseInt(args[3]);
		 ServiceInstance si = new ServiceInstance(new URL(args[0]), args[1], args[2], true);
		Folder rootFolder = si.getRootFolder();
	
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities(
				new String[][] { {"VirtualMachine", "name" }, }, true);
		for(int i=0; i<vms.length; i++)
		{
			System.out.println("vm["+i+"]=" + vms[i].getName());
			VMname = vms[i].getName();
			listSnapshots((VirtualMachine) vms[i]);
		}
		int nrSnap = list.size();
		if(nrSnap>0){
			Date d1 = new Date();
			StringBuilder subject = new StringBuilder();
			StringBuilder content = new StringBuilder();
			subject.append("Alert: ");
			subject.append(nrSnap);
			subject.append(" Snapshot(s) running over ");
			subject.append(nrDays);
			subject.append(" Day(s) on " + d1);
		
		
		
			content.append("<!DOCTYPE html><html><head><title>");
			content.append(subject + "</title> <h1>" + subject + "</h1></head>");
		
			content.append("<body>");
		
			content.append("<table border='1'> <colgroup> <col span='3' width = 300px> <col span='3' width = 100px></colgroup><tr>");
			content.append("<th>VM Name</th><th>SnapShot Name</th><th>SnapShot Description</th><th>SnapShot Create Date</th><th>SnapShot Running Days</th><th>State</th></tr>");
			for(int n = 0;n<nrSnap;n++){
				SnapsbyVM snap =  list.get(n);
				content.append("<tr><td>" + snap.getVMname() +"</td><td>"+snap.getSnapName()+"</td><td>"+snap.getSnapDescription()+"</td><td>"+snap.getSnapDate()+"</td><td>"+snap.getSnapRunningDays()+"</td><td>" + snap.getState()+"</td>");
			}
			content.append("</table>");
			content.append("</body>");

			writeHTML(content.toString());
		} else {
			writeHTML(null);
		}
	
		
		si.getServerConnection().logout();
		System.exit(nrSnap);
		
	}
	
	static void listSnapshots(VirtualMachine vm)
	  {
	    if(vm==null)
	    {
	      return;
	    }
	    VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();
	    if (snapInfo != null){
	    	VirtualMachineSnapshotTree[] snapTree = snapInfo.getRootSnapshotList();
	    	printSnapshots(snapTree);
	    }
	    
	  }

	  static void printSnapshots(VirtualMachineSnapshotTree[] snapTree)
	  {
		  
		 Date d1;
		 Date d2 = new Date();
		 int nrDaysRunning =0;
		  
	    for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
	    {
	      VirtualMachineSnapshotTree node = snapTree[i];
	     // System.out.println("Snapshot Name : " + node.getName());       
	      d1 = node.getCreateTime().getTime();
	      nrDaysRunning = (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	     
	      if (nrDaysRunning >= nrDays) {
	    	  		SnapsbyVM sVM = new SnapsbyVM(VMname,node.getName(),node.getDescription(),d1,nrDaysRunning,node.getState().toString());
	    	  		list.add(sVM);
	    	  	
	      }
	    
	      
	      VirtualMachineSnapshotTree[] childTree = 
	        node.getChildSnapshotList();
	      if(childTree!=null)
	      {
	        printSnapshots(childTree);
	      }
	    }
	  }
	


static void writeHTML(String content){
	try{

		  FileWriter fstream = new FileWriter("VMRunFromSnapshotsOverXDays.html");
		  BufferedWriter out = new BufferedWriter(fstream);
		  out.write(content);
	

		  out.close();
	}catch (Exception e){
		  System.err.println("Error: " + e.getMessage());
		  }
	}
}


