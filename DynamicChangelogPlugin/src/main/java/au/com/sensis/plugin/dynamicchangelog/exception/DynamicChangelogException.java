package au.com.sensis.plugin.dynamicchangelog.exception;

public class DynamicChangelogException extends RuntimeException {

	private static final long serialVersionUID = 3536002130879309738L;

	public DynamicChangelogException() {
		super();
	}

	public DynamicChangelogException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DynamicChangelogException(String arg0) {
		super(arg0);
	}

	public DynamicChangelogException(Throwable arg0) {
		super(arg0);
	}
}
