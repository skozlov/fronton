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

	public Insert(File source, String sourceContainerSelector, File target) {
		Utils.checkArgumentNotNull(source, "source");
		Utils.checkArgumentNotEmpty(sourceContainerSelector, "sourceContainerSelector");
		Utils.checkArgumentNotNull(target, "target");
		this.source = source;
		this.sourceContainerSelector = sourceContainerSelector;
		this.target = target;
	}

	@Override
	public Stream<Map.Entry<Document, File>> apply(Document template, String templateContainerSelector)
			throws HtmlReadException, InvalidSelectorException{

		Utils.checkArgumentNotNull(template, "template");
		Utils.checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		Document source = Utils.parse(this.source, template.charset());
		Utils.insert(template, templateContainerSelector, source, sourceContainerSelector);
		return Stream.of(new AbstractMap.SimpleImmutableEntry<>(template, target));
	}
}
