package au.com.sensis.plugin.dynamicchangelog.entity;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ChangeLogEntryTest {
	
	private static final String EMPTY_STR = "";
	
	private ChangeLogEntry changeLogEntry;

	@Before
	public void setUp() {
		changeLogEntry = new ChangeLogEntry(null, null, null, null, null);
	}

	@Test
	public void willExtractContextFromStagingRollforward() {
		//Given
		String filename = "20120101_1_[stage]_special_staging_rollforward.sql";
		//When
		String result = changeLogEntry.extractContext(filename);
		//Then
		assertNotNull(result);
		assertEquals("stage", result);
	}

	@Test
	public void willExtractContextAsEmptyStrFromStandardRollforward() {
		//Given
		String filename = "20120102_1_basic_rollforward.sql";
		//When
		String result = changeLogEntry.extractContext(filename);
		//Then
		assertNotNull(result);
		assertEquals(EMPTY_STR, result);
	}

}
