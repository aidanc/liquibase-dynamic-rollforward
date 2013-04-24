package au.com.sensis.plugin.dynamicchangelog.output;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.logging.Log;

/** Write the changelog file out to the file system. 
 * 
 * @author Aidan Carter
 *
 */
public class ChangelogWriter {
	
	private File changelogOutputFile;
	private File templateHeader;
	private File templateFooter;
    private Log log;
	
	public ChangelogWriter(File changelogOutputFile, File templateHeader, File templateFooter, Log log) {
		super();
		this.changelogOutputFile = changelogOutputFile;
		this.templateHeader = templateHeader;
		this.templateFooter = templateFooter;
		this.log = log;
	}

	public void write(String changelogBody) {
		
		if(changelogOutputFile.exists()) {
			changelogOutputFile.delete();
		}
		File parentFile = changelogOutputFile.getParentFile();
		if(!parentFile.exists()) {
			parentFile.mkdirs();
		}

		BufferedWriter out = null;
		try {
			changelogOutputFile.createNewFile();
			out = new BufferedWriter(new FileWriter(changelogOutputFile));
			
			//Write header
			writeFile(templateHeader, out);
			
			//Write entries for rollforwards
			out.write(changelogBody);

			//Write footer
			writeFile(templateFooter, out);
		} catch (IOException e) {
			log.error("IOException writing changelog file", e);
		} finally {
			if(out != null) {
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
				}
			}
		}
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
