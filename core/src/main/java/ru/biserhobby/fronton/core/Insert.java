package ru.biserhobby.fronton.core;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Insert implements BiFunction<Document, String, Stream<Map.Entry<Document, File>>> {
	private final File source;
	private final String sourceContainerSelector;
	private final File target;
	private final boolean copyStylesheetLinks;
	private final boolean copyScripts;

	public Insert(
			File source,
			String sourceContainerSelector,
			File target,
			boolean copyStylesheetLinks,
			boolean copyScripts) {

		Utils.checkArgumentNotNull(source, "source");
		Utils.checkArgumentNotEmpty(sourceContainerSelector, "sourceContainerSelector");
		Utils.checkArgumentNotNull(target, "target");
		this.source = source;
		this.sourceContainerSelector = sourceContainerSelector;
		this.target = target;
		this.copyStylesheetLinks = copyStylesheetLinks;
		this.copyScripts = copyScripts;
	}

	@Override
	public Stream<Map.Entry<Document, File>> apply(Document template, String templateContainerSelector)
			throws HtmlReadException, InvalidSelectorException{

		Utils.checkArgumentNotNull(template, "template");
		Utils.checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		Document source = Utils.parse(this.source, template.charset());
		Utils.insert(
				template,
				templateContainerSelector,
				source,
				sourceContainerSelector,
				copyStylesheetLinks,
				copyScripts);
		return Stream.of(new AbstractMap.SimpleImmutableEntry<>(template, target));
	}
}
