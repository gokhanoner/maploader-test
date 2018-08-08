package demo;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataLoader {

    public static void main(String[] args) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://localhost/maploader");
        ds.setUsername("root");

        try(HikariDataSource dss = ds) {
            List<List<Integer>> partition = Lists.partition(
                    IntStream.range(0, 1_000_000)
                            .boxed()
                            .collect(Collectors.toList()), 1_000);

            partition
                    .stream()
                    //.parallel()
                    .forEach(l -> {
                        try(Connection con = dss.getConnection()) {
                            PreparedStatement ps = con.prepareStatement("INSERT INTO test (code) VALUES (?)");
                            for (Integer i : l) {
                                ps.setString(1, "code" + i);
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
