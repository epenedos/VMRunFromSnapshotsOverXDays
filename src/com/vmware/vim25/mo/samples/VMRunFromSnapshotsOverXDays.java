package com.vmware.vim25.mo.samples;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.URL;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;


import com.vmware.vim25.Event;
import com.vmware.vim25.EventFilterSpec;
import com.vmware.vim25.EventFilterSpecByEntity;
import com.vmware.vim25.EventFilterSpecByTime;
import com.vmware.vim25.EventFilterSpecRecursionOption;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.*;

import com.vmware.vim25.mo.samples.SnapsbyVM;

public class VMRunFromSnapshotsOverXDays {
	


	static int nrDays = 0;
	static ArrayList<SnapsbyVM> list = new ArrayList<SnapsbyVM>() ;
	static String VMname;
	static ServiceInstance si;
	static ManagedEntity vm;
	
	public static void main(String[] args) throws Exception
	{
		if(args.length!=4)
	    {
	      System.out.println("Usage: java VMRunFromSnapshotsOverXDays <vCenter url> " +
	      		"<username> <password> <Number of Days Running from Snapshots>");
	    
	      System.exit(-1);
	    }
		
		nrDays = Integer.parseInt(args[3]);
		si = new ServiceInstance(new URL(args[0]), args[1], args[2], true);
		Folder rootFolder = si.getRootFolder();
	
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities(new String[][] { {"VirtualMachine", "name" }, }, true);
		for(int i=0; i<vms.length; i++)
		{
			VMname = vms[i].getName();
			vm=vms[i];
			listSnapshots((VirtualMachine) vm);
		}
		
		
		processHTML();
		
		si.getServerConnection().logout();
		System.exit(list.size());
		
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
	    		ListSnapshotsRec(snapTree);
	    }
	    
	  }

	  static void ListSnapshotsRec(VirtualMachineSnapshotTree[] snapTree)
	  {
		  
		 Date d1;
		 Date d2 = new Date();
		 int nrDaysRunning =0;
		  
	    for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
	    {
	      VirtualMachineSnapshotTree node = snapTree[i];  
	      d1 = node.getCreateTime().getTime();
	      nrDaysRunning = (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	    
	      if (nrDaysRunning >= nrDays) {
	    	  		String user = queryEventsForUser(si);
	    	  		SnapsbyVM sVM = new SnapsbyVM(VMname,node.getName(),node.getDescription(),d1,nrDaysRunning,node.getState().toString(),node.getBackupManifest(),user);
	    	  		list.add(sVM);
	      }
	    
	      
	      VirtualMachineSnapshotTree[] childTree = node.getChildSnapshotList();
	      if(childTree!=null)
	      {
	        ListSnapshotsRec(childTree);
	      }
	    }
	  }
	
static void processHTML(){
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
		content.append("<h1>SnapShots</h1>");
		content.append("<table border='2'> <colgroup> <col span='3' width = 220px> <col span='3' width = 100px><col span='1' width = 150px> <col span='1' width = 100px> </colgroup><tr>");
		content.append("<th>VM Name</th><th>Name</th><th>Description</th><th>Create Date</th><th>Running Days</th><th>State</th><th>Backup Manifest</th><th>User</th></tr>");
		
	DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,Locale.US);
	
		for(int n = 0;n<nrSnap;n++){
			SnapsbyVM snap =  list.get(n);
			
			content.append("<tr><td>" + snap.getVMname() +"</td><td>"+snap.getSnapName()+"</td><td>"+snap.getSnapDescription()+"</td><td>"+df.format(snap.getSnapDate())+"</td><td>"+snap.getSnapRunningDays()+"</td><td>" + snap.getSnapState()+"</td><td>" + snap.getSnapBackupManifest() +"</td><td>" + snap.getSnapUser() +"</td>");
		}
		content.append("</table>");
		content.append("</body>");

		writeHTML(content.toString());
	} else {
		writeHTML(null);
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


static String queryEventsForUser(ServiceInstance si2) {
	EventManager evtMgr = si.getEventManager();
    EventFilterSpec efs = new EventFilterSpec();
   // Filter by TaskEvents and Info
    efs.setType(new String[] {"TaskEvent"});
    efs.setCategory(new String[] {"info"});
    // Filter by VM
    EventFilterSpecByEntity eFilter = new EventFilterSpecByEntity();
    	eFilter.setEntity(vm.getMOR());
	efs.setEntity(eFilter);
    	eFilter.setRecursion(EventFilterSpecRecursionOption.self);
		
	Event[] events;
	
		try {
			events = evtMgr.queryEvents(efs);
			for(int i=0; events!=null && i<events.length; i++) 
		    {
				String tmpUser = listEvent(events[i]);
		           if (tmpUser != null){
		        	   return tmpUser;
		           }
		    }
		} catch (RuntimeFault e) {
		
			e.printStackTrace();
		} catch (RemoteException e) {
			
			e.printStackTrace();
		}

	return "N/A";
    
	}


static String listEvent(Event evt)
{
  
 if (evt.getFullFormattedMessage().contentEquals( "Task: Create virtual machine snapshot") ){
  	return evt.getUserName().toString();
 } else {
	 return null;
 }
}
}


