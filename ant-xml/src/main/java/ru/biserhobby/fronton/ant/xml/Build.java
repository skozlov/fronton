package ru.biserhobby.fronton.ant.xml;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import ru.biserhobby.fronton.core.FrontonException;
import ru.biserhobby.fronton.core.Project;
import ru.biserhobby.fronton.xml.ProjectParser;

import java.io.File;

public class Build extends Task {
	private File sourceBasedir;
	private File targetBasedir;
	private File projectDescriptor;

	public void setSourceBasedir(File sourceBasedir) {
		this.sourceBasedir = sourceBasedir;
	}

	public void setTargetBasedir(File targetBasedir) {
		this.targetBasedir = targetBasedir;
	}

	public void setProjectDescriptor(File projectDescriptor) {
		this.projectDescriptor = projectDescriptor;
	}

	@Override
	public void execute() throws BuildException {
		checkParameterNotNull(sourceBasedir, "sourceBasedir");
		checkParameterNotNull(targetBasedir, "targetBasedir");
		checkParameterNotNull(projectDescriptor, "projectDescriptor");
		try {
			ProjectParser parser = new ProjectParser(sourceBasedir, targetBasedir);
			Project project = parser.apply(projectDescriptor);
			project.run();
		} catch (FrontonException e){
			throw new BuildException(e);
		}
	}

	private static void checkParameterNotNull(Object value, String name){
		if(value == null){
			throw new BuildException(String.format("Missing required parameter `%s`", name));
		}
	}
}
