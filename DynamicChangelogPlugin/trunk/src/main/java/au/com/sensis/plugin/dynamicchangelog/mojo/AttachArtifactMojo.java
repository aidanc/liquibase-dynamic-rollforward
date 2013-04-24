package au.com.sensis.plugin.dynamicchangelog.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import au.com.sensis.plugin.dynamicchangelog.entity.DynamicRollforwardEntry;
import au.com.sensis.plugin.dynamicchangelog.util.FileUtils;
import au.com.sensis.plugin.dynamicchangelog.util.RollforwardFileRetriever;

/**
 * Create the liquibase changelog for sql rollforward scripts.
 *
 * @goal dynamic-changelog
 * @phase generate-resources
 * @author Aidan Carter
 */
public class AttachArtifactMojo extends AbstractMojo
{
	/**
	 * The folder containing the rollforward scripts to process.
	 *
	 * @parameter expression="${rollforwardDir}" default-value="${basedir}/src/main/resources/sql/rollforward"
	 * @required
	 */
	protected File rollforwardDir;

	/**
	 * The folder containing the rollback scripts. There must be a matching rollback for each rollforward, and the filename must
	 * be: [rollforwardname]_rollback.sql
	 *
	 * @parameter expression="${rollbackDir}" default-value="${basedir}/src/main/resources/sql/rollback"
	 * @required
	 */
	protected File rollbackDir;

	/**
	 * Changelog header file. Added to the start of the dynamic changelog.
	 *
	 * @parameter expression="${templateHeader}" default-value="${basedir}/src/main/resources/liquibase/template/rf_changelog_header.xml"
	 * @required
	 */
	protected File templateHeader;

	/**
	 * Changelog footer file. Added to the end of the dynamic changelog.
	 *
	 * @parameter expression="${templateFooter}" default-value="${basedir}/src/main/resources/liquibase/template/rf_changelog_footer.xml"
	 * @required
	 */
	protected File templateFooter;

	/**
	 * Changelog fragment file (velocity template). One instance of this will be created for each rollforward file. 
	 *
	 * @parameter expression="${templateVelocityFragment}" default-value="${basedir}/src/main/resources/liquibase/template/rf_changelog_fragment.vm"
	 * @required
	 */
	protected File templateVelocityFragment;
	
	/**
	 * The output file.
	 *
	 * @parameter expression="${changelogOutputFile}" default-value="${project.build.outputDirectory}/liquibase/changelog/db_dynamic_rf.xml"
	 * @required
	 */
	protected File changelogOutputFile;

	/**
	 * Name of the environment you are executing against. This will ONLY be used to execute environment
	 * specific rollforward files.
	 *  
	 * e.g.: 20120918_1_[test]_add_config_entries_for_stepup.sql
	 * 
	 * Note that any rollforward files for different environments will be ignored. So in the above example, if
	 * the environmentName selected was "dev", then the file above will not be processed.
	 * 
	 * @parameter expression="${environmentName}"
	 */
	protected String environmentName;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	@SuppressWarnings("unused")
	private MavenProject project;
	
	private VelocityEngine velocityEngine;

	/** {@inheritDoc} */
	@Override
	public void execute()
	{
		validateInputParams();
		init();
		
		try {
			StringBuilder sb = new StringBuilder();
			Template velocityTemplate = velocityEngine.getTemplate(templateVelocityFragment.getName());
			Set<File> rollforwards = new RollforwardFileRetriever(environmentName, getLog())
				.getSortedRollforwardList(rollforwardDir);
			
			for(File rfFile : rollforwards) {
				File rbFile = FileUtils.findMatchingRollback(rollbackDir, rfFile);
				DynamicRollforwardEntry drEntry = new DynamicRollforwardEntry(rfFile, rbFile, velocityTemplate);
				sb.append(drEntry.toString());
			}
			writeOutputFile(sb.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void init() {
		try {
			velocityEngine = new VelocityEngine();
			Properties p = new Properties() ;
            p.setProperty("resource.loader","file");
            p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            p.setProperty("file.resource.loader.path", templateVelocityFragment.getParent());
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("Error while initializing VelocityEngine", e);
		}
	}
	
	private void validateInputParams() {
		
		if(!rollforwardDir.exists() || !rollforwardDir.isDirectory()) {
			throw new RuntimeException("rollforwardDir does not exist or is not a directory: " + rollforwardDir.getAbsolutePath());
		}
		
		if(!rollbackDir.exists() || !rollbackDir.isDirectory()) {
			throw new RuntimeException("rollbackDir does not exist or is not a directory: " + rollbackDir.getAbsolutePath());
		}
	}
	
	private void writeOutputFile(String generatedChangelog) throws IOException {
		
		if(changelogOutputFile.exists()) {
			changelogOutputFile.delete();
		}
		File parentFile = changelogOutputFile.getParentFile();
		if(!parentFile.exists()) {
			parentFile.mkdirs();
		}

		changelogOutputFile.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(changelogOutputFile));
		
		//Write header
		writeFile(templateHeader, out);
		
		//Write entries for rollforwards
		out.write(generatedChangelog);

		//Write footer
		writeFile(templateFooter, out);

		out.flush();
		out.close();
	}
	
	private void writeFile(File toWrite, BufferedWriter out) throws IOException {
		BufferedReader headerReader = new BufferedReader(new FileReader(toWrite));
		String line = headerReader.readLine();
		while(line != null) {
			out.write(line);
			out.write("\n");
			line = headerReader.readLine();
		}
		headerReader.close();
	}

}
