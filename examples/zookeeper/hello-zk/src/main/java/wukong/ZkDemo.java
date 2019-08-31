package wukong;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 普通demo，很简单获取根目录下的全部children
 * @author fan
 */
public class ZkDemo {
    public static void main(String[] args) throws IOException {
        String hostPort = "localhost:2181";
        List<String> zooChildren = new ArrayList<String>();
        ZooKeeper zk = new ZooKeeper(hostPort, 2000, null);
        if (zk != null) {
            try {
                String zpath = "/";
                zooChildren = zk.getChildren(zpath, false);
                System.out.println("Znodes of '/': ");
                for (String child : zooChildren) {
                    System.out.println(child);
                }
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}