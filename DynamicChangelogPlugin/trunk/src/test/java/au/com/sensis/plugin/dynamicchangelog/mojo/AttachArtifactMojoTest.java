package au.com.sensis.plugin.dynamicchangelog.mojo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import au.com.sensis.plugin.dynamicchangelog.exception.InvalidFilenameException;


public class AttachArtifactMojoTest {
	
	private AttachArtifactMojo attachArtifactMojo;
	
	private String rfScenarioBaseDir = "src/test/resources/rollforwards";
	
	private String defaultRfBase = rfScenarioBaseDir + "/validScenario"; 
	private String defaultTemplateBase = "src/test/resources/template"; 

	private File rollforwardDir;
	private File rollbackDir;
	private File templateHeader;
	private File templateVelocityFragment;
	private File templateFooter;
	
	private File changelogOutputFile = new File("target/changelog_output.xml");
	private String environmentName;

	@Before
	public void setUp() {
		initDefaultDirsFromRoots();
		
		attachArtifactMojo = new AttachArtifactMojo();
		attachArtifactMojo.rollforwardDir = rollforwardDir;
		attachArtifactMojo.rollbackDir = rollbackDir;
		attachArtifactMojo.templateHeader = templateHeader;
		attachArtifactMojo.templateFooter = templateFooter;
		attachArtifactMojo.templateVelocityFragment = templateVelocityFragment;
		attachArtifactMojo.changelogOutputFile = changelogOutputFile;
		attachArtifactMojo.environmentName = environmentName;
	}

	@Test
	public void willGenerateValidOutputInValidScenario() throws Exception {
		//Given
		//When
		attachArtifactMojo.execute();
		//Then
		Document myExpectedOutputXML = XMLUnit.buildDocument(XMLUnit.newTestParser(), new FileReader(changelogOutputFile));
		XMLAssert.assertXpathEvaluatesTo("3", "count(/databaseChangeLog/changeSet)", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121122_1']", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121123_1']", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121124_1']", myExpectedOutputXML);
	}

	@Test
	public void willGenerateValidOutputInValidScenarioWithEnv() throws Exception {
		//Given
		attachArtifactMojo.environmentName = "dev";
		//When
		attachArtifactMojo.execute();
		//Then
		Document myExpectedOutputXML = XMLUnit.buildDocument(XMLUnit.newTestParser(), new FileReader(changelogOutputFile));
		XMLAssert.assertXpathEvaluatesTo("4", "count(/databaseChangeLog/changeSet)", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121122_1']", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121122_2']", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121123_1']", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121124_1']", myExpectedOutputXML);
	}

	@Test
	public void willThrowExceptionWhenRfNameIsInvalid() throws Exception {
		//Given
		defaultRfBase = rfScenarioBaseDir + "/invalidRfName";
		environmentName = "dev";
		setUp();
		
		//When
		try {
			attachArtifactMojo.execute();
			fail("Should have thrown exception due to missing [test] rollback file");
		} catch (InvalidFilenameException e) {
			//Then
			String exMsg = e.getMessage();
			assertTrue(exMsg.startsWith("Invalid filename: "));
		}
	}
	
	@Test
	public void willThrowExceptionWhenMissingRollbackInOtherEnv() throws Exception {
		//Given
		defaultRfBase = rfScenarioBaseDir + "/missingEnvRb";
		environmentName = "dev";
		setUp();
		
		//When
		try {
			attachArtifactMojo.execute();
			fail("Should have thrown exception due to missing [test] rollback file");
		} catch (RuntimeException e) {
			//Then
			String exMsg = e.getMessage();
			assertTrue(exMsg.startsWith("No rollback file found: 20121122_3_[test]"));
		}
	}
	
	private void initDefaultDirsFromRoots() {
		rollforwardDir = new File(defaultRfBase + "/rf");
		rollbackDir = new File(defaultRfBase + "/rb");

		templateHeader = new File(defaultTemplateBase + "/rf_changelog_header.xml");
		templateVelocityFragment = new File(defaultTemplateBase + "/rf_changelog_fragment.vm");
		templateFooter = new File(defaultTemplateBase + "/rf_changelog_footer.xml");
	}
}
