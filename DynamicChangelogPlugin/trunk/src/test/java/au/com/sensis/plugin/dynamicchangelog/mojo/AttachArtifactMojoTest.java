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


public class AttachArtifactMojoTest {
	
	private AttachArtifactMojo attachArtifactMojo;
	
	private String defaultRfBase = "src/test/resources/rollforwards/validScenario"; 
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
		XMLAssert.assertXpathEvaluatesTo("1", "count(/databaseChangeLog/changeSet)", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121122_1']", myExpectedOutputXML);
	}

	@Test
	public void willGenerateValidOutputInValidScenarioWithEnv() throws Exception {
		//Given
		attachArtifactMojo.environmentName = "dev";
		//When
		attachArtifactMojo.execute();
		//Then
		Document myExpectedOutputXML = XMLUnit.buildDocument(XMLUnit.newTestParser(), new FileReader(changelogOutputFile));
		XMLAssert.assertXpathEvaluatesTo("2", "count(/databaseChangeLog/changeSet)", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121122_1']", myExpectedOutputXML);
		XMLAssert.assertXpathExists("//changeSet[@id='20121122_2']", myExpectedOutputXML);
	}

	@Test
	public void willThrowExceptionWhenMissingRollbackInOtherEnv() throws Exception {
		//Given
		defaultRfBase = "src/test/resources/rollforwards/missingEnvRb";
		environmentName = "dev";
		setUp();
		
		//When
		try {
			attachArtifactMojo.execute();
			fail("Should have thrown exception due to missing [test] rollback file");
		} catch (RuntimeException e) {
			//Then
			String exMsg = e.getCause().getMessage();
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














