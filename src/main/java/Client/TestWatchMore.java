package Client;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import lombok.Getter;
import lombok.Setter;

public class TestWatchMore {

	
	public static void main(String[] args){
		WatchMore watchMore = new WatchMore();
		try {
			ZooKeeper zooKeeper = watchMore.startConn();
			watchMore.setZk(zooKeeper);
			if(zooKeeper.exists(watchMore.PATH, false) != null){

				String node = watchMore.getNode();
				System.out.println(node);
//				for (int i =1; i < 4; i++) {
//					watchMore.setNode("A"+i+"A");
//					Thread.sleep(1000);
//				}
				Thread.sleep(Long.MAX_VALUE);
				watchMore.downNode();
			}else{
				watchMore.CreateNode("v2");
				System.out.println("创建新节点"+watchMore.PATH);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
class WatchMore{
	public static final String  URL="192.168.171.128:2181";
	public static final String  PATH="/watchone";
	public static final int  SESSION_TIMEOUT=30*1000;
	public static final CreateMode  NODE_CLASS=CreateMode.PERSISTENT;
	public static int count=20;
	private @Setter@Getter ZooKeeper zk;//zk公用不需要再作为参数传递
	
	public ZooKeeper startConn() throws IOException{
		return new ZooKeeper(URL, SESSION_TIMEOUT, null);
	}
	public void CreateNode(String data) throws KeeperException, InterruptedException{
		zk.create(PATH, data.getBytes(), Ids.OPEN_ACL_UNSAFE, NODE_CLASS);
	}

	private String triggerValue() throws KeeperException, InterruptedException {
		String result=null;
		//得到新数据以后再次写入watch对象进行监控
		byte[] data = zk.getData(PATH, new Watcher(){
			@Override
			public void process(WatchedEvent event) {
					try {
						String triggerValue = triggerValue();
						System.out.println("****triggerValue:显示："+triggerValue);
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}	finally {
	//					这里重新把改变的值传给客户端，更新以前取的值
					}
				}
			}, new Stat());
		return new String(data);
	}
	public String getNode() throws KeeperException, InterruptedException{
		if(null!=zk){
			byte[] data = zk.getData(PATH, new Watcher(){
				@Override
				public void process(WatchedEvent event) {
					try {
						String triggerValue = triggerValue();
						System.out.println("****triggerValue:显示："+triggerValue);
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}	finally {
//						这里重新把改变的值传给客户端，更新以前取的值
					}
				}

			}, new Stat());
			return new String(data);
		}
		return null;
	}
	public void setNode(String data) throws Exception{
		
		if(zk.exists(PATH, false) != null){
			zk.setData(PATH, data.getBytes(),count++);
			System.out.println("修改节点数据成功");
		}else{
			System.out.println("没有此节点");
		}	
	}
	public void downNode() throws InterruptedException{
		if(zk!=null){
			zk.close();
		}	
	}
}