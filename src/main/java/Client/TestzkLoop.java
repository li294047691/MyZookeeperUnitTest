package Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import lombok.Getter;
import lombok.Setter;

public class TestzkLoop{

	public static final String  URL="192.168.171.128:2181";
	public static final String  PATH="/bank";
	public static final String  PREFIX="sub";
	public static final int  SESSION_TIMEOUT=30*1000;
	public static final CreateMode  NODE_CLASS=CreateMode.PERSISTENT;
	public static int count=20;
	public static List<String> list=new ArrayList<String>();;
	private int currentWindow=0;
	private int totalWindow=5;
	private @Setter@Getter ZooKeeper zk;//zk公用不需要再作为参数传递
	
	public static void main(String[] args){
		TestzkLoop zkLoop = new TestzkLoop();
		try {
			ZooKeeper zooKeeper = zkLoop.startConn();
			zkLoop.setZk(zooKeeper);
			zkLoop.getList();
			if(zooKeeper.exists(zkLoop.PATH, false) != null){
				for (int i = 0; i < 15; i++) {
					System.out.println(list);
					String doRequest = zkLoop.doRequest();
					System.out.println(doRequest);
				}
			}else{
				zkLoop.CreateNode("v2");
				System.out.println("创建新节点"+zkLoop.PATH);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String doRequest() throws Exception{
		currentWindow++;
		for (int i = currentWindow; i <= totalWindow; i++) {
			if(list.contains(PREFIX+currentWindow)){
				return new String(zk.getData(PATH+"/"+PREFIX+currentWindow, false, new Stat()));
			}else{
				currentWindow++;
			}
		}
		for (int i = 1; i <= totalWindow; i++) {
			if(list.contains(PREFIX+i)){
				currentWindow=i;
				return new String(zk.getData(PATH+"/"+PREFIX+currentWindow, false, new Stat()));
			}
		}
		return "*********没有这个window***********";
	}
	public ZooKeeper startConn() throws IOException{
		return new ZooKeeper(URL, SESSION_TIMEOUT,new Watcher(){
			@Override
			public void process(WatchedEvent event) {
						try {
						 list = zk.getChildren(PATH, true);
							triggerValue();
						} catch (InterruptedException|KeeperException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						System.out.println("链表重新更新");
				}
			});
	}
	public void CreateNode(String data) throws KeeperException, InterruptedException{
		zk.create(PATH, data.getBytes(), Ids.OPEN_ACL_UNSAFE, NODE_CLASS);
	}

	private void triggerValue() throws KeeperException, InterruptedException {
		String result=null;
		//得到新数据以后再次写入watch对象进行监控
		list = zk.getChildren(PATH, new Watcher(){
			@Override
			public void process(WatchedEvent event) {
					try {
						triggerValue();
						System.out.println("链表重新更新");
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}	finally {
	//					这里重新把改变的值传给客户端，更新以前取的值
					}
				}
			}, new Stat());
	}
	public void getList() throws KeeperException, InterruptedException{
		if(null!=zk){
			list = zk.getChildren(PATH, new Watcher(){
				@Override
				public void process(WatchedEvent event) {
					try {
						triggerValue();
						System.out.println("链表重新更新");
					} catch (KeeperException | InterruptedException e) {
						e.printStackTrace();
					}	finally {
//						这里重新把改变的值传给客户端，更新以前取的值
					}
				}

			}, new Stat());
		}
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
