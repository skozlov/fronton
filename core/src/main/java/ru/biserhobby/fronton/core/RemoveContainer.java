package ru.biserhobby.fronton.core;

import org.jsoup.nodes.Document;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class RemoveContainer implements BiFunction<Document, String, Stream<Map.Entry<Document, File>>> {
	private final File target;

	public RemoveContainer(File target) {
		Utils.checkArgumentNotNull(target, "target");
		this.target = target;
	}

	@Override
	public Stream<Map.Entry<Document, File>> apply(Document template, String templateContainerSelector)
			throws InvalidSelectorException{

		Utils.checkArgumentNotNull(template, "template");
		Utils.checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		Utils.select(template, templateContainerSelector).remove();
		return Stream.of(new AbstractMap.SimpleImmutableEntry<>(template, target));
	}
}
