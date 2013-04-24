package au.com.sensis.plugin.dynamicchangelog.mojo;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.app.VelocityEngine;

import au.com.sensis.plugin.dynamicchangelog.file.RollforwardFileRetriever;
import au.com.sensis.plugin.dynamicchangelog.output.ChangelogBodyGenerator;
import au.com.sensis.plugin.dynamicchangelog.output.ChangelogWriter;
import au.com.sensis.plugin.dynamicchangelog.validation.RollforwardListingValidator;

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

		//Validate the contents of rollforward/rollback directories
		new RollforwardListingValidator(rollforwardDir, rollbackDir, getLog()).validate();
		
		try {
			//Generate contents of the changelog file based on rollforward entries
			Set<File> rfFiles = new RollforwardFileRetriever(environmentName, getLog()).getSortedRollforwardList(rollforwardDir);
			ChangelogBodyGenerator changelogGenerator = new ChangelogBodyGenerator(velocityEngine, templateVelocityFragment, 
					rollbackDir);
			String changelogBody = changelogGenerator.generateChangelogBody(rfFiles);

			//Write the output file
			ChangelogWriter ChangelogWriter = new ChangelogWriter(changelogOutputFile, templateHeader, templateFooter, getLog());
			ChangelogWriter.write(changelogBody);

		} catch (Exception e) {
			throw new RuntimeException(e);
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
	

}
