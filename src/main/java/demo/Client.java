package demo;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Client {

    public static void main(String[] args) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("maploader-demo");

        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

        System.out.println("create map proxy");

        IMap<Integer, TestPojo> testmap = timeit(() -> hz.getMap("test"));

        System.out.println("Load an existing key");

        timeit(() -> testmap.get(1));

        System.out.println("Load inexistent key (in cache)");

        timeit(() -> testmap.get(20_000));

        System.out.println("Load inexistent key (in db)");

        timeit(() -> testmap.get(300_000));

        System.out.println("Evict data");

        timeit(() -> {
            testmap.evictAll();
            return null;
        });

        System.out.println("Load partial data");

        timeit(() -> {
            testmap.loadAll(IntStream.range(0, 1000).boxed().collect(Collectors.toSet()), true);
            return null;
        });


    }

    static <T> T timeit(Supplier<T> opr) {
        T result = null;
        long start = System.nanoTime();
        try{
            result = opr.get();
        } finally {
            long time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            System.out.printf("\nCompleted in %s ms.\t\t Result : %s\n", time, result);
        }
        return result;
    }
}
