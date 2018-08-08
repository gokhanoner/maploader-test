package demo;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.Hazelcast;

public class Member {

    public static void main(String[] args) {
        Config config = new Config();
        config.getGroupConfig().setName("maploader-demo");

        MapStoreConfig msc = new MapStoreConfig();
        msc.setEnabled(true)
                .setProperty("url", "jdbc:mysql://localhost/maploader")
                .setProperty("username", "root")
                //.setProperty("limit", "10000")
                .setImplementation(new TestMapLoader())
                .setInitialLoadMode(InitialLoadMode.EAGER);

        config.getMapConfig("test").setMapStoreConfig(msc);

        Hazelcast.newHazelcastInstance(config);
    }
}
