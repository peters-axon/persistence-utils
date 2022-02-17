package com.axonivy.market.jpa.demo.test.mock;

public class NotMockedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public NotMockedException() {
		super(getFunction());
	}

	
	private static String getFunction() {
		String result = "<unknown>";
		try {
			throw new RuntimeException();
		} catch (Exception e) {
			StackTraceElement[] st = e.getStackTrace();
			if(st.length > 2) {
				StackTraceElement el = st[2];

				result = el.toString();
			}
		}
		return result;
	}
}
