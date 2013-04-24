package au.com.sensis.plugin.dynamicchangelog.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import au.com.sensis.plugin.dynamicchangelog.util.FileUtils;

/** Utility class to retrieve the list of rollforward files to process from the input directory.
 * 
 * @author Aidan Carter
 *
 */
public class RollforwardFileRetriever {

    private Log log;
	public FilenameFilter rollforwardFilter;
	
	public RollforwardFileRetriever(Log log) {
		this.log = log;
		rollforwardFilter = createFileFilter();
	}
	
	public Set<File> getSortedRollforwardList(File rollforwardDir) {
		File[] rollforwards = rollforwardDir.listFiles(rollforwardFilter);
		return FileUtils.sortFileList(rollforwards);
	}

	private FilenameFilter createFileFilter() {
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

				String msg = "Rollforward file accepted for inclusion in changelog: %s";
				log.debug(String.format(msg, name));
				return true;
			}
		}; 
		return rollforwardFilter;
	}

}
