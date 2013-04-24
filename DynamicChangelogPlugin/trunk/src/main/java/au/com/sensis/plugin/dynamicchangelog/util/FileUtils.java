package au.com.sensis.plugin.dynamicchangelog.util;

import static au.com.sensis.plugin.dynamicchangelog.util.Constants.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.TreeSet;

public class FileUtils {
	
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
		
		boolean matchesEnvRollForward = ROLLFORWARD_ENV_FILENAME_PATTERN.matcher(filename).matches();
		boolean matchesStdRollForward = ROLLFORWARD_FILENAME_PATTERN.matcher(filename).matches();
		
		if(!matchesStdRollForward && !matchesEnvRollForward) {
			String msg = "Invalid rollforward filename (%s). Must match one of:";
			msg += "\n\tStandard rollforward: %s";
			msg += "\n\tEnv specific rollforward %s";
			String fullMsg = String.format(String.format(msg, filename, ROLLFORWARD_FILENAME_REGEX, 
					ROLLFORWARD_ENV_FILENAME_PATTERN));
			throw new RuntimeException(fullMsg);
		}
	}
	
	/** Extract the ID to use in the changelog file. The date and sequence number will be used.
	 * 
	 * e.g. File "20120607_1_recreate_mvs_for_tuning.sql" will give an ID of "20120607_1"
	 * e.g. File "20120811_1_[test]_recreate_mvs_for_tuning.sql" will give an ID of "20120811_1"
	 * 
	 * Note: Any files passed in here should already have been validated with 'validateRollforwardFilename(..)'
	 */
	public static String extractIdFromFilename(File rollforwardFile) {
		String filename = rollforwardFile.getName();
		int idEndIndex = filename.indexOf("_", 9);
		return filename.substring(0, idEndIndex);
	}
	
	/** Utility method to sort an array of files by their filename. This is used for ensuring
	 * there are no possible platform dependent behaviour issues when performing a directory listing.
	 * 
	 * @param fileListing
	 * @return
	 */
	public static Set<File> sortFileList(File[] fileListing) {
		Set<File> sortedFileSet = new TreeSet<File>();
		for(File rff : fileListing) {
			sortedFileSet.add(rff);
		}
		return sortedFileSet;
	}
	
	
}
