package ru.biserhobby.fronton.core;

import org.jsoup.nodes.Document;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class Pages {
	private final File template;
	private final Charset charset;
	private final String templateContainerSelector;
	private final Stream<? extends BiFunction<Document, String, Stream<Map.Entry<Document, File>>>>
			pages;

	public Pages(
			File template,
			Charset charset,
			String templateContainerSelector,
			Stream<? extends BiFunction<Document, String, Stream<Map.Entry<Document, File>>>> pages) {

		Utils.checkArgumentNotNull(template, "template");
		Utils.checkArgumentNotNull(charset, "charset");
		Utils.checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		Utils.checkArgumentNotNull(pages, "pages");
		this.template = template;
		this.charset = charset;
		this.templateContainerSelector = templateContainerSelector;
		this.pages = pages;
	}

	public Stream<? extends Map.Entry<? extends Document, ? extends File>> processInner() throws FrontonException{
		Document template = Utils.parse(this.template, charset);
		return pages.flatMap(page -> page.apply(template.clone(), templateContainerSelector));
	}
}
