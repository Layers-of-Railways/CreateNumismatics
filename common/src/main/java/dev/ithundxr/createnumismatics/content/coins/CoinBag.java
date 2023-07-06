package dev.ithundxr.createnumismatics.content.coins;

import dev.ithundxr.createnumismatics.content.backend.Coin;

import java.util.HashMap;
import java.util.Map;

public class CoinBag {
    private final Map<Coin, Integer> coins = new HashMap<>();
    private int value = 0;

    public CoinBag(Map<Coin, Integer> coins) {
        this.coins.putAll(coins);
        calculateValue();
    }

    public CoinBag() {}

    private void calculateValue() {
        for (Map.Entry<Coin, Integer> entry : coins.entrySet()) {
            this.value += entry.getKey().toSpurs(entry.getValue());
        }
    }

    public void add(Coin coin, int count) {
        this.coins.put(coin, get(coin) + count);
        calculateValue();
    }

    public void subtract(Coin coin, int count) {
        this.coins.put(coin, Math.max(0, get(coin) - count));
        calculateValue();
    }

    public int get(Coin coin) {
        return this.coins.getOrDefault(coin, 0);
    }

    public int getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == 0;
    }
}
