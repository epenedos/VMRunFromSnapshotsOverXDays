package com.vmware.vim25.mo.samples;
import java.net.URL;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;
import com.vmware.vim25.mo.*;
public class VMRunFromSnapshotsOverXDays {
	static int nrDays =0;
	static int nrSnap = 0;
	public static void main(String[] args) throws Exception
	{
		if(args.length!=5)
	    {
	      System.out.println("Usage: java VMRunFromSnapshotsOverXDays <vCenter url> " +
	      		"<username> <password> <vmname> <op>");
	      System.out.println("op - list, create, remove, " +
	      		"removeall, revert");
	      System.exit(0);
	    }
		ServiceInstance si = new ServiceInstance(new URL("https://server/sdk"), "root", "password", true);
		Folder rootFolder = si.getRootFolder();
	
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities(
				new String[][] { {"VirtualMachine", "name" }, }, true);
		for(int i=0; i<vms.length; i++)
		{
			System.out.println("vm["+i+"]=" + vms[i].getName());
		}

		
		
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
	    for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
	    {
	      VirtualMachineSnapshotTree node = snapTree[i];
	      System.out.println("Snapshot Name : " + node.getName());           
	      VirtualMachineSnapshotTree[] childTree = 
	        node.getChildSnapshotList();
	      if(childTree!=null)
	      {
	        printSnapshots(childTree);
	      }
	    }
	  }
	
	
}
