package au.com.sensis.plugin.dynamicchangelog.entity;

import java.io.File;
import java.io.StringWriter;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import au.com.sensis.plugin.dynamicchangelog.util.FileUtils;

public class DynamicRollforwardEntry {
	
	private File rollforwardFile;
	private File rollbackFile;
	private Template velocityTemplate;

	public DynamicRollforwardEntry(File rollforwardFile, File rollbackFile, Template velocityTemplate) {
		super();
		this.rollforwardFile = rollforwardFile;
		this.rollbackFile = rollbackFile;
		this.velocityTemplate = velocityTemplate;
	}

	public String toString() {
		try {
			String comment = rollforwardFile.getName();
			String rollforwardFilename = rollforwardFile.getAbsolutePath();
			String rollbackFilename = rollbackFile.getAbsolutePath();
			
			VelocityContext context = new VelocityContext();
			context.put("rollforwardId", FileUtils.extractIdFromFilename(rollforwardFile));
			context.put("rollforwardComment", comment);
			context.put("rollforwardFilename", rollforwardFilename);
			context.put("rollbackFilename", rollbackFilename);
			
			StringWriter writer = new StringWriter();
			velocityTemplate.merge(context, writer);
			return writer.toString();
		} catch (Exception e) {
			String string = "Error while generating Velocity template for file: " + rollforwardFile.getName();
			throw new RuntimeException(string, e);
		}
	}
	

}
