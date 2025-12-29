package org.forpdi.planning.attribute.types.enums;

import java.util.GregorianCalendar;

public enum Periodicity {

	DAILY(1, "Diária", new GregorianCalendar(0, 0, 1)),
	WEEKLY(2, "Semanal", new GregorianCalendar(0, 0, 7)),
	FORTNIGHTLY(3, "Quinzenal", new GregorianCalendar(0, 0, 15)),
	MONTHLY(4, "Mensal", new GregorianCalendar(0, 1, 0)),
	BIMONTHLY(5, "Bimestral", new GregorianCalendar(0, 2, 0)),
	QUARTERLY(6, "Trimestral", new GregorianCalendar(0, 3, 0)),
	SEMIANNUAL(7, "Semestral", new GregorianCalendar(0, 6, 0)),
	ANNUAL(8, "Anual", new GregorianCalendar(1, 0, 0)),
	BIENNIAL(9, "Bienal", new GregorianCalendar(2, 0, 0)),
	TRIENNIAL(10, "Trienal", new GregorianCalendar(3, 0, 0));

	private Integer value;
	private String name;
	private GregorianCalendar time;

	private Periodicity(Integer value, String name, GregorianCalendar time){
		this.value = value;
		this.name = name;
		this.time = time;
	}
	
	static public Periodicity getPeriodicityByName(String name) {
		for (Periodicity periodicity : values()) {
			if (periodicity.getName().equals(name)) {
				return periodicity;
			}
		}
		throw new IllegalArgumentException("Invalid periodicity name: " + name);
	}
	
	public Integer getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	
	public GregorianCalendar getTime() {
		return time;
	}
	
	public long getTimeInMilliseconds() {
		return time.getTimeInMillis();
	}
}
