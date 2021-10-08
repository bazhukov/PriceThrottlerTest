package com.price.processor.throttler;

import java.util.Objects;

final class CurrencyPairPrice implements Comparable<CurrencyPairPrice> {

    private final String ccyPair;
    private final double rate;
    private final Long version;

    public CurrencyPairPrice(String ccyPair, double rate, long version) {
        this.ccyPair = ccyPair;
        this.rate = rate;
        this.version = version;
    }

    public String getCcyPair() {
        return ccyPair;
    }

    public double getRate() {
        return rate;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public int compareTo(CurrencyPairPrice o) {
        return this.version.compareTo(o.getVersion());
    }

    @Override
    public String toString() {
        return "CurrencyPairPrice{" +
                "ccyPair='" + ccyPair + '\'' +
                ", rate=" + rate +
                ", version=" + version +
                '}';
    }
}
