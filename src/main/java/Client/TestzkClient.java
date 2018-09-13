package Client;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class TestzkClient {

	
	public static void main(String[] args){
		Zk zk = new Zk();
		try {
			ZooKeeper zooKeeper = zk.startConn();
			if(zooKeeper.exists("/bop", false) == null){
				zk.CreateNode(zooKeeper, "/bop", "v2");
				String node = zk.getNode(zooKeeper,"/bop");
				System.out.println(node);
				zk.downNode(zooKeeper);
			}else{
				System.out.println("节点已经存在");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
class Zk{
	public static final String  URL="192.168.171.128:2181";
	public static final int  SESSION_TIMEOUT=30*1000;
	public static final CreateMode  NODE_CLASS=CreateMode.PERSISTENT;
	
	public ZooKeeper startConn() throws IOException{
		return new ZooKeeper(URL, SESSION_TIMEOUT, null);
	}
	public void CreateNode(ZooKeeper zk,String path,String data) throws KeeperException, InterruptedException{
		zk.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, NODE_CLASS);
	}
	public String getNode(ZooKeeper zk,String path) throws KeeperException, InterruptedException{
		if(null!=zk){
			byte[] data = zk.getData(path, false, new Stat());
			return new String(data);
		}
		return null;
	}
	public void downNode(ZooKeeper zk) throws InterruptedException{
		if(zk!=null){
			zk.close();
		}	
	}
}