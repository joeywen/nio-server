package com.joey.common;

public class Pair <K, V> {
    private K key;
    private V value;

    public K getKey() {
    	return this.key;
    }

    public V getValue() {
    	return this.value;
    }

    public Pair(K k, V v) {
    	this.key = k;
    	this.value = v;
    }

    public java.lang.String toString() {
    	return String.format("key = %s, value = %s", this.key.toString(),this.value.toString());
    }

    @Override
    public int hashCode() {
    	String hashcode = String.format("%d%d", this.key.hashCode(), this.value.hashCode());
    	return hashcode.hashCode();
    }

    @Override
    public boolean equals(java.lang.Object o) {
    	if (o instanceof Pair) {
    		Pair pair = (Pair) o;
    		return (pair.key == this.key && pair.value == this.value);
    	} else {
    		return false;
    	}
    }
}
