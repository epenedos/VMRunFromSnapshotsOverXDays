package com.vmware.vim25.mo.samples;
import java.net.URL;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.*;
import com.vmware.vim25.mo.Folder;

import com.vmware.vim25.mo.samples.SnapsbyVM;

public class VMRunFromSnapshotsOverXDays {
	

	static int nrSnap = 0;
	static int nrDays = 0;
	static List<SnapsbyVM> list;
	static String VMname;
	
	public static void main(String[] args) throws Exception
	{
		if(args.length!=4)
	    {
	      System.out.println("Usage: java VMRunFromSnapshotsOverXDays <vCenter url> " +
	      		"<username> <password> <Number of Days Running from Snapshots> <op>");
	    
	      System.exit(0);
	    }
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

		sendMail();
		writeHTML();

		
		
		si.getServerConnection().logout();
	}
	
	static void listSnapshots(VirtualMachine vm)
	  {
	    if(vm==null)
	    {
	      return;
	    }
	    VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();
	    VirtualMachineSnapshotTree[] snapTree = 
	      snapInfo.getRootSnapshotList();
	    printSnapshots(snapTree);
	  }

	  static void printSnapshots(
	      VirtualMachineSnapshotTree[] snapTree)
	  {
		  
		 Date d1;
		 Date d2 = new Date();
		 int nrDaysRunning =0;
		  
	    for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
	    {
	      VirtualMachineSnapshotTree node = snapTree[i];
	      System.out.println("Snapshot Name : " + node.getName());       
	      d1 = node.getCreateTime().getTime();
	      nrDaysRunning = (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	      
	      if (nrDaysRunning >= nrDays) {
	    	  		SnapsbyVM sVM = new SnapsbyVM(VMname,node.getName(),node.getDescription(),d1,nrDaysRunning);
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
	
static void sendMail(String to,String from,String host, String subject, String content){
	
    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", host);
    Session session = Session.getDefaultInstance(properties);

    try{

       MimeMessage message = new MimeMessage(session);
       message.setFrom(new InternetAddress(from));
       message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));

       message.setSubject(subject);
       message.setContent(content,"text/html" );
       Transport.send(message);
       
    }catch (MessagingException mex) {
       mex.printStackTrace();
    }
}

static void writeHTML(String header, String Content){
	
}



}
	

