package ru.biserhobby.fronton.ant.xml;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import ru.biserhobby.fronton.core.CustomAttributeProcessor;
import ru.biserhobby.fronton.core.FrontonException;
import ru.biserhobby.fronton.core.Project;
import ru.biserhobby.fronton.xml.ProjectParser;

import java.io.File;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ru.biserhobby.fronton.core.CustomAttributeProcessor.*;

public class Build extends Task {
	private File sourceBasedir;
	private File targetBasedir;
	private File projectDescriptor;
	private boolean removeCustomAttributes = true;
	private boolean copyCustomAttributes = false;
	private boolean copyStylesheetLinks = false;
	private boolean copyScripts = false;

	public void setSourceBasedir(File sourceBasedir) {
		this.sourceBasedir = sourceBasedir;
	}

	public void setTargetBasedir(File targetBasedir) {
		this.targetBasedir = targetBasedir;
	}

	public void setProjectDescriptor(File projectDescriptor) {
		this.projectDescriptor = projectDescriptor;
	}

	public void setRemoveCustomAttributes(boolean removeCustomAttributes) {
		this.removeCustomAttributes = removeCustomAttributes;
	}

	public void setCopyCustomAttributes(boolean copyCustomAttributes) {
		this.copyCustomAttributes = copyCustomAttributes;
	}

	public void setCopyStylesheetLinks(boolean copyStylesheetLinks) {
		this.copyStylesheetLinks = copyStylesheetLinks;
	}

	public void setCopyScripts(boolean copyScripts) {
		this.copyScripts = copyScripts;
	}

	@Override
	public void execute() throws BuildException {
		checkParameterNotNull(sourceBasedir, "sourceBasedir");
		checkParameterNotNull(targetBasedir, "targetBasedir");
		checkParameterNotNull(projectDescriptor, "projectDescriptor");
		try {
			ProjectParser parser = new ProjectParser(
					sourceBasedir, targetBasedir, getCustomAttributeProcessor(), copyStylesheetLinks, copyScripts);
			Project project = parser.apply(projectDescriptor);
			project.run();
		} catch (FrontonException e){
			throw new BuildException(e);
		}
	}

	private CustomAttributeProcessor getCustomAttributeProcessor(){
		Function<String, Optional<String>> customMapper =
				removeCustomAttributes ? REMOVE_CUSTOM : LEAVE_CUSTOM_UNCHANGED;
		BiFunction<String, Optional<String>, Optional<String>> targetMapper =
				copyCustomAttributes ? COPY_CUSTOM_TO_TARGET : LEAVE_TARGET_UNCHANGED;
		return CustomAttributeProcessor.fromMappers(customMapper, targetMapper);
	}

	private static void checkParameterNotNull(Object value, String name){
		if(value == null){
			throw new BuildException(String.format("Missing required parameter `%s`", name));
		}
	}
}
