package au.com.sensis.plugin.dynamicchangelog.entity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.regex.Matcher;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import au.com.sensis.plugin.dynamicchangelog.exception.DynamicChangelogException;
import au.com.sensis.plugin.dynamicchangelog.util.Constants;
import au.com.sensis.plugin.dynamicchangelog.util.FileUtils;

public class ChangeLogEntry {
	
	private File rollforwardFile;
	private File rollbackFile;
    private String rollforwardClasspathPrefix;
    private String rollbackClasspathPrefix;
	private Template velocityTemplate;

    public ChangeLogEntry(File rollforwardFile, String rollforwardClasspathPrefix, File rollbackFile,
                          String rollbackClasspathPrefix, Template velocityTemplate) {
        this.rollforwardFile = rollforwardFile;
        this.rollbackFile = rollbackFile;
        this.rollforwardClasspathPrefix = rollforwardClasspathPrefix;
        this.rollbackClasspathPrefix = rollbackClasspathPrefix;
        this.velocityTemplate = velocityTemplate;
    }

    public String toString() {
		try {
			String comment = rollforwardFile.getName();
            String rollforwardFilename = createFilenameWithPath(rollforwardFile, rollforwardClasspathPrefix);
            String rollbackFilename = createFilenameWithPath(rollbackFile, rollbackClasspathPrefix);

			String context = extractContext(rollforwardFile.getName());
			
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("rollforwardId", FileUtils.extractIdFromFilename(rollforwardFile));
			velocityContext.put("rollforwardComment", comment);
			velocityContext.put("rollforwardFilename", rollforwardFilename);
			velocityContext.put("rollbackFilename", rollbackFilename);
			velocityContext.put("context", context);
			
			StringWriter writer = new StringWriter();
			velocityTemplate.merge(velocityContext, writer);
			return writer.toString();
		} catch (IOException e) {
			String string = "Error while generating Velocity template for file: " + rollforwardFile.getName();
			throw new DynamicChangelogException(string, e);
		}
	}


    private String createFilenameWithPath(File sqlFile, String classpathPrefix) {
        if(classpathPrefix == null || classpathPrefix.equals("")) {
            return sqlFile.getAbsolutePath();
        }

        if(!(classpathPrefix.charAt(classpathPrefix.length()-1) == File.separatorChar)) {
            classpathPrefix += File.separatorChar;
        }
        return classpathPrefix + sqlFile.getName();
    }


	/** Extract the context to use in the changeset. The context will be the env defined in the filename.
	 *  
	 * Examples: 
	 * 	"20121122_3_[prod]_env_specific2.sql" should have a context of "prod"
	 *  "20121124_1_another_db_change02.sql" should have a context of ""
	 * 
	 * @param filename Filename of a rollforward file. Can be env specific or not.
	 * @return The 
	 */
	protected String extractContext(String filename) {
		Matcher envNameMatcher = Constants.ROLLFORWARD_ENV_FILENAME_PATTERN.matcher(filename);
		if(envNameMatcher.matches()) {
			return envNameMatcher.group(1);
		}
		return "";
	}
}
