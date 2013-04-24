package au.com.sensis.plugin.dynamicchangelog.util;

import java.util.regex.Pattern;

public class Constants {

	public static final String ROLLBACK_SUFFIX = "_rollback";
	public static final String SQL_SUFFIX = ".sql";
	public static final String SQL_REGEX_SUFFIX = "\\.sql$";
	
	/** Filename for rollforwards must match this pattern. e.g.: 20120607_1_recreate_mvs_for_tuning.sql */
	public static final String ROLLFORWARD_FILENAME_REGEX = "^\\d{8}_\\d+_\\w+";
	public static final Pattern ROLLFORWARD_FILENAME_PATTERN = Pattern.compile(
			ROLLFORWARD_FILENAME_REGEX + SQL_REGEX_SUFFIX);

	/** Filename for environment specific rollforwards must match this pattern. e.g.: 20120607_1_[test]_recreate_mvs_for_tuning.sql */
	public static final String ROLLFORWARD_ENV_FILENAME_REGEX = "^\\d{8}_\\d+_\\[(\\w+)\\]_\\w+";
	public static final Pattern ROLLFORWARD_ENV_FILENAME_PATTERN = Pattern.compile(
			ROLLFORWARD_ENV_FILENAME_REGEX + SQL_REGEX_SUFFIX);

	/** Filename for rollbacks must match this pattern. e.g.: 20120607_1_recreate_mvs_for_tuning_rollback.sql */
	public static final String ROLLBACK_FILENAME_REGEX = ROLLFORWARD_FILENAME_REGEX + ROLLBACK_SUFFIX + SQL_REGEX_SUFFIX;
	public static final Pattern ROLLBACK_FILENAME_PATTERN = Pattern.compile(ROLLBACK_FILENAME_REGEX);

	/** Filename for environment specific rollforwards must match this pattern. e.g.: 20120607_1_[test]_recreate_mvs_for_tuning_rollback.sql */
	public static final String ROLLBACK_ENV_FILENAME_REGEX = ROLLFORWARD_ENV_FILENAME_REGEX + ROLLBACK_SUFFIX + SQL_REGEX_SUFFIX;
	public static final Pattern ROLLBACK_ENV_FILENAME_PATTERN = Pattern.compile(ROLLBACK_ENV_FILENAME_REGEX);


}
