package org.forpdi.core.utils;

public class Counter {

	private int counter;

	public Counter() {
		this.counter = 0;
	}
	
	public Counter(int counter) {
		this.counter = counter;
	}

	public int getCounter() {
		return counter;
	}

	public void add(int value) {
		counter += value;
	}
	
	public void increment() {
		counter += 1;
	}
	
	public void decrement() {
		counter -= 1;
	}
	
	@Override
	public String toString() {
		return String.valueOf(counter);
	}
}
