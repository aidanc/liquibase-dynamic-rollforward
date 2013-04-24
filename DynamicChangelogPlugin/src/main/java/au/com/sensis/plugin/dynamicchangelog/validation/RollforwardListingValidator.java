package au.com.sensis.plugin.dynamicchangelog.validation;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

import au.com.sensis.plugin.dynamicchangelog.exception.InvalidFilenameException;
import au.com.sensis.plugin.dynamicchangelog.file.InputFilenameFilter;
import au.com.sensis.plugin.dynamicchangelog.util.Constants;
import au.com.sensis.plugin.dynamicchangelog.util.FileUtils;

public class RollforwardListingValidator {
	
	private File rollforwardDir;
	private File rollbackDir;
    private Log log;

	public RollforwardListingValidator(File rollforwardDir, File rollbackDir, Log log) {
		super();
		this.rollforwardDir = rollforwardDir;
		this.rollbackDir = rollbackDir;
		this.log = log;
	}
	
	public void validate() {
		
		//Check all files in RF directory have the correct pattern
		Set<File> rfFiles = FileUtils.sortFileList(rollforwardDir.listFiles(new InputFilenameFilter(log)));
		validateDirectory(rfFiles, Constants.ROLLFORWARD_FILENAME_PATTERN, Constants.ROLLFORWARD_ENV_FILENAME_PATTERN);
		
		//Check all files in RB directory have the correct pattern
		Set<File> rbFiles = FileUtils.sortFileList(rollbackDir.listFiles(new InputFilenameFilter(log)));
		validateDirectory(rbFiles, Constants.ROLLBACK_FILENAME_PATTERN, Constants.ROLLBACK_ENV_FILENAME_PATTERN);
		
		//Check all RF files have a matching RB file
		validateRfHasMatchingRb();
	}
	
	
	private void validateDirectory(Set<File> fileListing, Pattern ... allowedFilenamePatterns) {
		if(fileListing == null) {
			return;
		}
		
		if(allowedFilenamePatterns == null) {
			throw new IllegalArgumentException("Must specify one or filename patterns to validate against");
		}
		
		//Check that filename matches at least one supplied pattern
		for(File file : fileListing) {
			String filename = file.getName();
			boolean valid = false;
			for(Pattern fnPattern : allowedFilenamePatterns) {
				valid = valid || fnPattern.matcher(filename).matches();
			}
			if(!valid) {
				String msg = String.format("Invalid filename: %s", filename);
				log.error(msg);
				throw new InvalidFilenameException(msg); 
			}
		}
	}
	
	private void validateRfHasMatchingRb() {
		File[] rfFiles = rollforwardDir.listFiles(new InputFilenameFilter(log));
		for(File rfFile : rfFiles) {
			FileUtils.findMatchingRollback(rollbackDir, rfFile);
		}
	}
	

}
