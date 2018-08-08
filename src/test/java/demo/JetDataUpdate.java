package demo;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.pipeline.BatchStage;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import static java.util.AbstractMap.*;

public class JetDataUpdate {

    public static void main(String[] args) {
        JetInstance jet = Jet.newJetInstance();
        try{
            long start = System.nanoTime();
            jet.newJob(buildPipeline()).join();
            System.out.printf("\nCompleted in %s\n", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        } finally {
            Jet.shutdownAll();
        }
    }


    static Pipeline buildPipeline() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getGroupConfig().setName("maploader-demo");

        Pipeline p = Pipeline.create();

        BatchStage<TestPojo> testPojoBatchStage = p.drawFrom(Sources.jdbc(
                () -> DriverManager.getConnection("jdbc:mysql://localhost/maploader", "root", ""),
                (con, parallelism, index) -> {
                    PreparedStatement stmt = con.prepareStatement("SELECT * FROM test WHERE MOD(id, ?) = ? LIMIT 10000");
                    stmt.setInt(1, parallelism);
                    stmt.setInt(2, index);
                    return stmt.executeQuery();
                },
                rs -> TestPojo.of(rs.getInt(1), rs.getString(2), rs.getTimestamp(3).toInstant())));

        testPojoBatchStage
                //.peek()
                .map(e -> new SimpleEntry(e.getId(), e))
                .drainTo(Sinks.remoteMap("test", clientConfig));

        return p;
    }
}
