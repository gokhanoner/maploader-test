package demo;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.function.Function.*;

public class TestMapLoader implements MapLoader<Integer, TestPojo>, MapLoaderLifecycleSupport {

    private int maxId;
    private Jdbi jdbi;

    @Override
    public TestPojo load(Integer key) {
        System.out.println("Loading key: " + key);
        return jdbi.withHandle(handle -> handle
                .select("SELECT * FROM test WHERE id = ?", key)
                .mapToBean(TestPojo.class)
                .findFirst()
                .orElse(null));

    }

    @Override
    public Map<Integer, TestPojo> loadAll(Collection<Integer> keys) {
        System.out.println("Loading " +  keys.size() + " keys");
        return jdbi.withHandle(handle -> handle
                    .createQuery("SELECT * FROM test WHERE id in (<idList>)")
                    .bindList("idList", new ArrayList<>(keys))
                    .mapToBean(TestPojo.class)
                    .collect(Collectors.toMap(TestPojo::getId, identity())));
    }

    @Override
    public Iterable<Integer> loadAllKeys() {
        System.out.println("Loading all keys: " + maxId);
        return jdbi.withHandle(handle -> handle
                        .select("SELECT id FROM test WHERE id < ?", maxId)
                        .mapTo(Integer.class)
                        .list());
    }

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");

        maxId = Integer.parseInt(properties.getProperty("limit", "1000"));

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);

        jdbi = Jdbi.create(ds);
    }

    @Override
    public void destroy() {

    }
}
