package com.icoolkj.platform.infra.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfraFileGcResult {

    private int purged;
    private int skipped;
    private int failed;

    public int processed() {
        return purged + skipped + failed;
    }
}
