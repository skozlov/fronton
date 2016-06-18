package ru.biserhobby.fronton.core;

import org.jsoup.nodes.Document;

import java.io.File;
import java.nio.charset.Charset;

public class SimplePage implements Runnable {
	private final File source;
	private final File target;
	private final Charset charset;
	private final CustomAttributeProcessor customAttributeProcessor;

	public SimplePage(File source, File target, Charset charset, CustomAttributeProcessor customAttributeProcessor) {
		Utils.checkArgumentNotNull(source, "source");
		Utils.checkArgumentNotNull(target, "target");
		Utils.checkArgumentNotNull(charset, "charset");
		Utils.checkArgumentNotNull(customAttributeProcessor, "customAttributeProcessor");
		this.source = source;
		this.target = target;
		this.charset = charset;
		this.customAttributeProcessor = customAttributeProcessor;
	}

	@Override
	public void run() throws FrontonIOException, HtmlReadException{
		Document document = Utils.parse(source, charset);
		Utils.processFinalDocument(document, target, customAttributeProcessor);
	}
}
