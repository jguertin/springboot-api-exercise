package com.vituary;

import com.vituary.model.ItemPriceAdjuster;
import com.vituary.model.SurgePriceAdjuster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AppConfig {
    private static final double SURGE_INCREASE_DEFAULT = 0.10;
    private static final Duration SURGE_WINDOW_DURATION_DEFAULT = Duration.ofHours(1);
    private static final int SURGE_EFFECTIVE_COUNT_DEFAULT = 10;

    @Bean
    public ItemPriceAdjuster surgePriceAdjuster() {
        return new SurgePriceAdjuster(SURGE_INCREASE_DEFAULT, SURGE_WINDOW_DURATION_DEFAULT, SURGE_EFFECTIVE_COUNT_DEFAULT);
    }
}
