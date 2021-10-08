package com.price.processor.throttler;

import com.price.processor.PriceProcessor;


/**
 *  PriceProcessor implementation for testing purpose
 */
final class SimplePriceProcessor implements PriceProcessor {

    private final long sleepInMilli;
    private final String processorName;


    private SimplePriceProcessor(long pauseInProcessingInMilliseconds, String processorName) {
        sleepInMilli = pauseInProcessingInMilliseconds;
        this.processorName = processorName;
    }


    public static SimplePriceProcessor create(Long delay, String processorName) {
        return new SimplePriceProcessor(delay, processorName);
    }

    @Override
    public void onPrice(String ccyPair, double rate) {
        if (sleepInMilli != 0) {
            try {
                Thread.sleep(sleepInMilli);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Processor " + processorName +" process " + ccyPair +" with rate " + rate);
    }

    @Override
    public void subscribe(PriceProcessor priceProcessor) {
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
    }

    @Override
    public String toString() {
        return "SimplePriceProcessor{" +
                "processorName='" + processorName +
                ", sleepInMilli=" + sleepInMilli +
                 '\'' +
                '}';
    }
}
