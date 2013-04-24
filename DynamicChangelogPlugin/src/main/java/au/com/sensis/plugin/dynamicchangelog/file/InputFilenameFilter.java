package au.com.sensis.plugin.dynamicchangelog.file;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.maven.plugin.logging.Log;

/** This filename filter will match all files in the rollforward directory (non recursively), excluding:
 * 
 *  .svn
 *  
 *  This complete list of files will be validated to ensure their names are in the correct format, and every 
 *  rollforward (for any environment) has a matching rollback file.
 * 
 * @author Aidan Carter
 *
 */
public class InputFilenameFilter implements FilenameFilter {
	
    private Log log;

	public InputFilenameFilter(Log log) {
		super();
		this.log = log;
	}

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

		String msg = "File accepted for validation: %s";
		log.debug(String.format(msg, name));
		return true;
	}

}
