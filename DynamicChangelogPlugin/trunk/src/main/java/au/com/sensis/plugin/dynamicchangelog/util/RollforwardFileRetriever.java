package au.com.sensis.plugin.dynamicchangelog.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.apache.maven.plugin.logging.Log;


public class RollforwardFileRetriever {

    private Log log; //Annoying to have to pass this reference around...
	public FilenameFilter rollforwardFilter;
	
	public RollforwardFileRetriever(String environmentName, Log log) {
		this.log = log;
		rollforwardFilter = createFileFilter(environmentName);
	}
	
	public Set<File> getSortedRollforwardList(File rollforwardDir) {
		File[] rollforwards = rollforwardDir.listFiles(rollforwardFilter);
		Set<File> sortedRollforwards = new TreeSet<File>();
		for(File rff : rollforwards) {
			FileUtils.validateRollforwardFilename(rff.getName());
			sortedRollforwards.add(rff);
		}
		return sortedRollforwards;
	}

	private FilenameFilter createFileFilter(final String environmentName) {
		FilenameFilter rollforwardFilter = new FilenameFilter() {
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

				Matcher envNameMatcher = FileUtils.ROLLFORWARD_ENV_FILENAME_PATTERN.matcher(name);
				if(envNameMatcher.matches()) {
					String envName = envNameMatcher.group(1);
					if(environmentName == null || !environmentName.equalsIgnoreCase(envName)) {
						String msg = "Skipping rollforward: %s. Does not match activated environment: %s";
						log.debug(String.format(msg, name, environmentName));
						return false;
					}
				}
				
				String msg = "Rollforward file accepted for processing: %s";
				log.debug(String.format(msg, name));
				return true;
			}
		}; 
		return rollforwardFilter;
	}

}
