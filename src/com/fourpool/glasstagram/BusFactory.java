package com.fourpool.glasstagram;

import com.squareup.otto.Bus;

public class BusFactory {
	private static final Bus BUS = new Bus();

	public static Bus getInstance() {
		return BUS;
	}
}
