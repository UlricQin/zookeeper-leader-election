package client;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class ZooKeeperClientTwo  implements Watcher {
	
	private ZooKeeper zk;
	
	private final String PATH = "/election";
	
	public ZooKeeperClientTwo() throws IOException, KeeperException, InterruptedException{
		
		/********************
		 * STARTING ZOOKEEPER
		 ********************/
		System.out.println("CLIENT TWO :: STARTING\n");	
		zk = new ZooKeeper("127.0.0.1", 3000, this);
		System.out.println("CLIENT TWO :: FINISHED STARTING\n");
		
		// Leader Election
		leaderElection();
		
		
		/*
		String election = "/election";

		String r = zk.create(election, new byte[0], Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
		System.out.println(r);
		
		String path = "/election/n_";
		path = zk.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println(path);

		zk.getChildren(election, true);
		*/
		
		int pause = new Scanner(System.in).nextInt();

		
		
		//2.Let C be the children of "ELECTION", and i be the sequence number of z;
		
		
		/*

		opt = new Scanner(System.in).nextInt();
		
		Stat s = zk.exists("/teste", false);
		if ( s == null )
			System.out.println("znode nao existe\n");
		else
			System.out.println("znode existe.\n");
		*/
		
		
		/*
		String path = "/teste";
		if (zk != null) {
			zk.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
		}

		*/

		//System.out.println("criado!!");
		//System.out.println("session id: " + zk.getSessionId() + "\n");

	}

	/**
	 * Leader Election
	 * @throws InterruptedException 
	 * @throws KeeperException
	 */
	public void leaderElection() throws KeeperException, InterruptedException{
		
		// If is the first client, then it should create the znode "/election"
		Stat stat = zk.exists(PATH, false);
		if(stat == null){
			System.out.println("CLIENT TWO :: Im the first client, creating " + PATH + ".");
			String election = "/election";
			String r = zk.create(election, new byte[0], Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			System.out.println("CLIENT TWO :: " + r + " created.");
		}
		
		// Create znode z with path "ELECTION/n_" with both SEQUENCE and EPHEMERAL flags
		String childPath = PATH + "/n_";
		
		childPath = zk.create(childPath, new byte[0], Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("CLIENT TWO :: My leader proposal created. Path = " + childPath + ".");
		
		// Let C be the children of "ELECTION", and i be the sequence number of z;
		// Watch for changes on "ELECTION/n_j", where j is the smallest sequence
		// number such that j < i and n_j is a znode in C;
		List<String> children = zk.getChildren(PATH, false);
		
		String tmp = children.get(0);
		
		for(String s : children){
			if(tmp.compareTo(s) > 0)
				tmp = s;	
		}
		
		// i contains the smallest sequence number
		String leader = PATH + "/" + tmp;
		Stat s = zk.exists(leader, true);


		// syso
		System.out.println("CLIENT TWO :: Leader is the owner of znode: " + leader);
		System.out.println("CLIENT ONE :: Leader id: " + s.getEphemeralOwner());
		
		/*
		Let ELECTION be a path of choice of the application. To volunteer to be a leader:
		1.Create znode z with path "ELECTION/n_" with both SEQUENCE and EPHEMERAL flags;
		2.Let C be the children of "ELECTION", and i be the sequence number of z;
		3.Watch for changes on "ELECTION/n_j", where j is the smallest sequence number such that j < i and n_j is a znode in C;

		Upon receiving a notification of znode deletion:
		1.Let C be the new set of children of ELECTION;
		2.If z is the smallest node in C, then execute leader procedure;
		3.Otherwise, watch for changes on "ELECTION/n_j", where j is the smallest sequence number such that j < i and n_j is a znode in C;
		 */
	}
	
	public void newLeaderElection() throws KeeperException, InterruptedException{
		
		List<String> children = zk.getChildren(PATH, false);
		
		String tmp = children.get(0);
		
		for(String s : children){
			if(tmp.compareTo(s) > 0)
				tmp = s;	
		}
		
		// i contains the smallest sequence number
		String leader = PATH + "/" + tmp;
		Stat s = zk.exists(leader, true);

		// syso
		System.out.println("CLIENT TWO :: Leader is the owner of znode: " + leader);
		System.out.println("CLIENT ONE :: Leader id: " + s.getEphemeralOwner());
	}
	

	@Override
	public void process(WatchedEvent event) {
		
		//String eventPath = event.getPath();
		
		switch (event.getType()){
		
		case NodeChildrenChanged:
			System.out.println("CLIENT TWO :: NodeChildrenChanged | ZNode: " + event.getPath());
			break;
			
		case NodeCreated:
			System.out.println("CLIENT TWO :: NodeCreated | ZNode: " + event.getPath());
			break;
		           
		case NodeDataChanged:
			System.out.println("CLIENT TWO :: NodeDataChanged | ZNode: " + event.getPath());
			break;
		           
		case NodeDeleted:
			System.out.println("CLIENT TWO :: NodeDeleted | ZNode: " + event.getPath());
			System.out.println("CLIENT TWO :: Leader was lost, newLeaderElection started.");
			try {
				newLeaderElection();
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
			
		case None:
			
			switch (event.getState()){
			
			case Disconnected:
				break;
				
			case Expired:
				break;
		
			case NoSyncConnected:
				System.out.println("CLIENT TWO :: NoSyncConnected - Deprecated");
				break;
				
			case SyncConnected:
				break;
			
			case Unknown:
				System.out.println("CLIENT TWO :: Unknown - Deprecated");
				break;
			}
			
		}
		
	}


	public static void main (String[] args){

		try {
			new ZooKeeperClientTwo();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
