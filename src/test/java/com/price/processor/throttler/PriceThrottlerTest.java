package com.price.processor.throttler;

import org.junit.jupiter.api.Test;

public class PriceThrottlerTest {

    @Test
    public void justRunner() {

        var slowListener = SimplePriceProcessor.create(1000L, "Slow1");
        var fastListener = SimplePriceProcessor.create(0L, "Fast1");

        try(var throttler = new PriceThrottler()){
            throttler.subscribe(slowListener);
            throttler.subscribe(fastListener);

            // Act
            for (var i = 1;  i<1000; i++){
                try {
                    var pairString = "EURUSD";
                    if(i % 100 == 0) pairString = "RUBUAH";

                    throttler.onPrice(pairString,  i);
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (throttler.isProcessed()) {
            }
        }
    }
}