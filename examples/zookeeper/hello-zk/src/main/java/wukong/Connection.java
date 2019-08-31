package wukong;

import org.apache.zookeeper.*;

import java.io.IOException;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * @author Eason
 * @create 2018-04-05 15:40
 **/
public class Connection implements Watcher {
    private static final int DEFAULT_SESSIONTIMEOUT = 5 * 1000;
    private final String connectionString;
    private final int sessionTimeout;
    private ZooKeeper zooKeeper;
    private volatile boolean connected;

    public boolean isConnected() {
        return connected;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public Connection(String connectionString, int sessionTimeout) {
        this.connectionString = connectionString;
        this.sessionTimeout = sessionTimeout;
        try {
            zooKeeper = new ZooKeeper(connectionString, sessionTimeout, this, false);
            connected = true;
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public Connection(String connectionString) {
        this(connectionString, DEFAULT_SESSIONTIMEOUT);
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println(event);
        //TODO://
    }


    /**
     * 创建持久性无序节点
     * @param path
     * @param value
     * @return 如果返回值为null, 说明创建失败
     */
    public String createNode(String path, String value) {
        try {
            return zooKeeper.create(path, value.getBytes(), OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 创建临时无序节点
     *
     * @param path
     * @param value
     * @return 如果返回值为null, 说明创建失败
     */
    public String createEphemeralNode(String path, String value) {
        try {
            return zooKeeper.create(path, value.getBytes(), OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建临时有序节点
     *
     * @param path
     * @param value
     * @return 如果返回值为null, 说明创建失败
     */
    public String createEphemeralSequentialNode(String path, String value) {
        try {
            return zooKeeper.create(path, value.getBytes(), OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建持久有序节点
     *
     * @param path
     * @param value
     * @return 如果返回值为null, 说明创建失败
     */
    public String createSequentialNode(String path, String value) {
        try {
            return zooKeeper.create(path, value.getBytes(), OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static void main(String[] args) {
        String connectionString = "127.0.0.1:2181";
        Connection connection = new Connection(connectionString);

        connection.createNode("/path1","test");
        connection.createSequentialNode("/path2","test");
        connection.createSequentialNode("/path2","test");
        connection.createEphemeralNode("/path3","test");
        connection.createEphemeralSequentialNode("/path4","test");
        connection.createEphemeralSequentialNode("/path4","test");

        while (connection.isConnected()){

        }
    }
}
