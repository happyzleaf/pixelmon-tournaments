package com.hiroku.tournaments.elo;

public enum EloTypes {
	SINGLE("single"),
	DOUBLE1v1("double1v1"),
	DOUBLE2v2("double2v2");

	public final String key;

	EloTypes(String key) {
		this.key = key;
	}
}
