package au.com.sensis.plugin.dynamicchangelog.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
	
	public static final String ROLLBACK_SUFFIX = "_rollback";
	public static final String SQL_SUFFIX = ".sql";
	
	/** Filename for rollforwards must match this pattern. e.g.: 20120607_1_recreate_mvs_for_tuning.sql */
	private static final String ROLLFORWARD_FILENAME_REGEX = "^\\d{8}_\\d+_.+\\.sql$";
	private static final Pattern ROLLFORWARD_FILENAME_PATTERN = Pattern.compile(ROLLFORWARD_FILENAME_REGEX);
	
	public static FilenameFilter rollforwardFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			//exclude svn metadata
			if(name.equals(".svn")) {
				return false;
			}
			
			//exclude subdirectories
			if(new File(dir, name).isDirectory()) {
				return false;
			}
			
			return true;
		}
	}; 
	
	public static Set<File> findRollforwardFiles(File rollforwardDir) {
		File[] rollforwards = rollforwardDir.listFiles(rollforwardFilter);
		Set<File> sortedRollforwards = new TreeSet<File>();
		for(File rff : rollforwards) {
			FileUtils.validateRollforwardFilename(rff.getName());
			sortedRollforwards.add(rff);
		}
		return sortedRollforwards;
	}

	
	public static File findMatchingRollback(File rollbackDir, File rollforwardFile) {
		
		final String rollbackName = convertName(rollforwardFile.getName());
		
		File[] matchingFiles = rollbackDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.equals(rollbackName)) {
					return true;
				}
				return false;
			}
		});
		
		if(matchingFiles.length == 0) {
			throw new RuntimeException("No rollback file found: " + rollbackName);
		}
		return matchingFiles[0];		
	}

	protected static String convertName(String rollforwardName) {
		String rfNoExt = rollforwardName.substring(0, rollforwardName.lastIndexOf(SQL_SUFFIX));
		return String.format("%s%s%s", rfNoExt, ROLLBACK_SUFFIX, SQL_SUFFIX);
	}
	
	public static void validateRollforwardFilename(String filename) {
		Matcher m = ROLLFORWARD_FILENAME_PATTERN.matcher(filename);
		if(!m.matches()) {
			String msg = "Invalid rollforward filename (%s). Must match pattern: %s";
			throw new RuntimeException(String.format(msg, filename, ROLLFORWARD_FILENAME_REGEX));
		}
	}
	
	/** Extract the ID to use in the changelog file. The date and sequence number will be used.
	 * 
	 * e.g. File "20120607_1_recreate_mvs_for_tuning.sql" will give an ID of "20120607_1"
	 * 
	 * Note: Any files passed in here should already have been validated with 'validateRollforwardFilename(String filename)'
	 */
	public static String extractIdFromFilename(File rollforwardFile) {
		String filename = rollforwardFile.getName();
		int idEndIndex = filename.indexOf("_", 9);
		return filename.substring(0, idEndIndex);
	}
	
}
