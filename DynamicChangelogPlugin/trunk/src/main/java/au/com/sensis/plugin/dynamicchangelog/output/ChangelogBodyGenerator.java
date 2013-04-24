package au.com.sensis.plugin.dynamicchangelog.output;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import au.com.sensis.plugin.dynamicchangelog.entity.ChangeLogEntry;
import au.com.sensis.plugin.dynamicchangelog.util.FileUtils;

public class ChangelogBodyGenerator {
	
	private VelocityEngine velocityEngine;
	private File templateVelocityFragment;
	private File rollbackDir;
	
	public ChangelogBodyGenerator(VelocityEngine velocityEngine, File templateVelocityFragment, File rollbackDir) {
		super();
		this.velocityEngine = velocityEngine;
		this.templateVelocityFragment = templateVelocityFragment;
		this.rollbackDir = rollbackDir;
		init();
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
	
	public String generateChangelogBody(Set<File> rfFiles) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		Template velocityTemplate = velocityEngine.getTemplate(templateVelocityFragment.getName());
		
		for(File rfFile : rfFiles) {
			File rbFile = FileUtils.findMatchingRollback(rollbackDir, rfFile);
			ChangeLogEntry drEntry = new ChangeLogEntry(rfFile, rbFile, velocityTemplate);
			sb.append(drEntry.toString());
		}
		
		return sb.toString();
	}

}
