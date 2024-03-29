package wukong;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @Author: rocky
 * @Date: Created in 2018/5/12.
 * https://www.cnblogs.com/rocky-fang/p/9030438.html
 */
public class ZookeeperTestConnection implements Watcher {
    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent event) {
        System.out.println("receive the event:"+event);
        if(Event.KeeperState.SyncConnected == event.getState()) {
            countDownLatch.countDown();
        }
    }
    public static final String ADDRESS = "127.0.0.1:2181";

    public static void main(String[] args) throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper(ADDRESS, 5000, new ZookeeperTestConnection());
        System.out.println(zooKeeper.getState());
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("zookeeper session established");
    }
}
