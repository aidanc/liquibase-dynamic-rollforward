package au.com.sensis.plugin.dynamicchangelog.mojo;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import au.com.sensis.plugin.dynamicchangelog.exception.InvalidFilenameException;


public class AttachArtifactMojoTest {
	
	private static final String XPATH_COUNT_CHANGESET = "count(/databaseChangeLog/changeSet)";

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
		String[] expectedIds = {"20121122_1", "20121123_1", "20121124_1"};
		//When
		attachArtifactMojo.execute();
		//Then
		Document changelogDoc = XMLUnit.buildDocument(XMLUnit.newTestParser(), new FileReader(changelogOutputFile));
		assertChangeSetEntriesExistAndAreInOrder(expectedIds, changelogDoc);
	}

	@Test
	public void willGenerateValidOutputInValidScenarioWithEnv() throws Exception {
		//Given
		attachArtifactMojo.environmentName = "dev";
		String[] expectedIds = {"20121122_1", "20121122_2", "20121123_1", "20121124_1"};
		//When
		attachArtifactMojo.execute();
		//Then
		Document changelogDoc = XMLUnit.buildDocument(XMLUnit.newTestParser(), new FileReader(changelogOutputFile));
		assertChangeSetEntriesExistAndAreInOrder(expectedIds, changelogDoc);
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
	
	private void assertChangeSetEntriesExistAndAreInOrder(String[] orderedIds, Document changelogDoc) 
			throws XpathException {
		XMLAssert.assertXpathEvaluatesTo(String.valueOf(orderedIds.length), XPATH_COUNT_CHANGESET, changelogDoc);
		for(int i=0 ; i < orderedIds.length ; i++) {
			//Check id is present
			XMLAssert.assertXpathExists(getXpathChangesetWithId(orderedIds[i]), changelogDoc);
			//Check id is in expected position
			XMLAssert.assertXpathEvaluatesTo(orderedIds[i], getXpathIdOfChangeset(i+1), changelogDoc);
		}
	}
	
	private String getXpathChangesetWithId(String id) {
		return String.format("//changeSet[@id='%s']", id);
	}
	
	private String getXpathIdOfChangeset(int index) {
		return String.format("//changeSet[%s]/@id", index);
	}
}
