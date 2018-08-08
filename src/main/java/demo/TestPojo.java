package demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class TestPojo implements Serializable {
    private int id;
    private String code;
    private Instant timestamp;
}
