package com.icoolkj.platform.infra.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfraFileGcJobSummary {

    private int purged;
    private int skipped;
    private int failed;
    private int batches;

    public int processed() {
        return purged + skipped + failed;
    }
}
