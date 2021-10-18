package com.price.processor.throttler;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.price.processor.PriceProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PriceThrottler implements PriceProcessor, AutoCloseable {

    private final static Logger logger = LogManager.getLogger(PriceThrottler.class);

    private final ConcurrentHashMap<PriceProcessor, CompletableFuture<Void>> tasks = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<PriceProcessor, ConcurrentHashMap<String, CurrencyPairPrice>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService taskPool = Executors.newCachedThreadPool();
    private final AtomicLong version = new AtomicLong(0);


    @Override
    public void onPrice(String ccyPair, double rate) {

        var newPricePairVersion = version.incrementAndGet();
        var currencyPairPrice = new CurrencyPairPrice(ccyPair, rate, newPricePairVersion);

        for (var entry : subscribers.entrySet()) {
            var processor = entry.getKey();
            var pricesQueue = entry.getValue();

            pricesQueue.put(currencyPairPrice.getCcyPair(), currencyPairPrice);

            scheduleTask(processor);
        }
    }


    @Override
    public void subscribe(PriceProcessor priceProcessor) {
        var priceQueue = new ConcurrentHashMap<String, CurrencyPairPrice>();
        subscribers.put(priceProcessor, priceQueue);
        logger.info(priceProcessor + " subscribed");
    }

    @Override
    public void unsubscribe(PriceProcessor priceProcessor) {
        subscribers.remove(priceProcessor);
        logger.info(priceProcessor + " unsubscribed");
    }

    @Override
    public void close() {

        for (var processor : tasks.keySet()) {
            unsubscribe(processor);
            tasks.remove(processor);
        }

        taskPool.shutdown();
    }

    private void scheduleTask(PriceProcessor processor) {

        var task = tasks.get(processor);

        //if we have a price in the queue and a task is not working, so we can launch new processor
        if (task == null || task.isDone()) {
            var pairPriceOptional = subscribers.get(processor).values().stream().sorted().findFirst();
            if(pairPriceOptional.isPresent()) {
                var pairPrice = pairPriceOptional.get();
                subscribers.get(processor).remove(pairPrice.getCcyPair());
                Runnable runnableTask = () -> processor.onPrice(pairPrice.getCcyPair(), pairPrice.getRate());
                task = CompletableFuture.runAsync(runnableTask, taskPool).whenCompleteAsync(
                        (input, exception) -> {
                            if (exception != null) {
                                logger.error(exception);
                            } else {
                                scheduleTask(processor);
                            }
                        }
                );
            }
        }
        if (task != null) {
            tasks.put(processor, task);
        }
    }

    public boolean isProcessed(){
        return tasks.searchValues(2, (v) -> !v.isDone()) ;
    }
}
