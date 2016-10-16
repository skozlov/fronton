package ru.biserhobby.fronton.core;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class InsertPages implements BiFunction<Document, String, Stream<Map.Entry<Document, File>>> {
	private final String sourceContainerSelector;
	private final Pages pages;
	private final boolean copyStylesheetLinks;

	public InsertPages(String sourceContainerSelector, Pages pages, boolean copyStylesheetLinks) {
		Utils.checkArgumentNotEmpty(sourceContainerSelector, "sourceContainerSelector");
		Utils.checkArgumentNotNull(pages, "pages");
		this.sourceContainerSelector = sourceContainerSelector;
		this.pages = pages;
		this.copyStylesheetLinks = copyStylesheetLinks;
	}

	@Override
	public Stream<Map.Entry<Document, File>> apply(Document template, String templateContainerSelector)
			throws FrontonException{

		Utils.checkArgumentNotNull(template, "template");
		Utils.checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		return pages.processInner().flatMap(entry -> {
			Document source = entry.getKey();
			File target = entry.getValue();
			Document templateInstance = template.clone();
			Utils.insert(
					templateInstance, templateContainerSelector, source, sourceContainerSelector, copyStylesheetLinks);
			return Stream.of(new AbstractMap.SimpleImmutableEntry<>(templateInstance, target));
		});
	}
}
