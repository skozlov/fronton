package ru.biserhobby.fronton.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
	private static final Pattern CUSTOM_ATTR_PATTERN = Pattern.compile("^fronton:(?<target>.+)$");

	private Utils(){}

	public static Elements select(Element element, String selector) throws InvalidSelectorException{
		checkArgumentNotNull(element, "element");
		checkArgumentNotEmpty(selector, "selector");
		try{
			return element.select(selector);
		} catch (Selector.SelectorParseException e){
			throw new InvalidSelectorException(e);
		}
	}

	public static void insert(
			Element template,
			String templateContainerSelector,
			Element source,
			String sourceContainerSelector,
			boolean copyStylesheetLinks,
			boolean copyScripts) throws InvalidSelectorException{

		checkArgumentNotNull(template, "template");
		checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		checkArgumentNotNull(source, "source");
		checkArgumentNotEmpty(sourceContainerSelector, "sourceContainerSelector");
		String htmlToInsert = select(source, sourceContainerSelector).html();
		select(template, templateContainerSelector).html(htmlToInsert);
		if(copyStylesheetLinks){
			String links = select(source, "head > link[rel=stylesheet]").outerHtml();
			select(template, "head").append(links);
		}
		if(copyScripts){
			String scripts = select(source, "head > script").outerHtml();
			select(template, "head").append(scripts);
		}
	}

	public static Document parse(File file, Charset charset) throws HtmlReadException{
		try {
			return Jsoup.parse(file, charset.name());
		} catch (IOException e) {
			throw new HtmlReadException(e);
		}
	}

	public static void checkArgumentNotNull(Object argument, String argumentName) throws IllegalArgumentException{
		if(argument == null){
			throw new IllegalArgumentException(String.format("`%s` cannot be null", argumentName));
		}
	}

	public static void checkArgumentNotEmpty(String argument, String argumentName) throws IllegalArgumentException{
		checkArgumentNotNull(argument, argumentName);
		if(argument.isEmpty()){
			throw new IllegalArgumentException(String.format("`%s` cannot be empty", argument));
		}
	}

	public static void processFinalDocument(Document document, File file, CustomAttributeProcessor customAttributeProcessor) throws FrontonIOException{
		checkArgumentNotNull(document, "document");
		checkArgumentNotNull(file, "file");
		checkArgumentNotNull(customAttributeProcessor, "customAttributeProcessor");
		processCustomAttributes(document, customAttributeProcessor);
		writeDocument(document, file);
	}

	private static void processCustomAttributes(Document document, CustomAttributeProcessor processor){
		Elements elements = document.select("[^fronton:]");
		elements.forEach(e -> processElementCustomAttributes(e, processor));
	}

	private static void processElementCustomAttributes(Element element, CustomAttributeProcessor processor) {
		element.attributes().forEach(attr -> {
			String customAttrName = attr.getKey();
			Matcher matcher = CUSTOM_ATTR_PATTERN.matcher(customAttrName);
			if(matcher.matches()){
				String targetAttrName = matcher.group("target");
				Optional<String> targetAttrValue =
						element.hasAttr(targetAttrName)
								? Optional.of(element.attr(targetAttrName))
								: Optional.empty();
				String customAttrValue = attr.getValue();
				Optional<String> newCustomValue = processor.mapCustom(customAttrValue);
				Optional<String> newTargetValue = processor.mapTarget(customAttrValue, targetAttrValue);
				updateAttr(element, customAttrName, newCustomValue);
				updateAttr(element, targetAttrName, newTargetValue);
			}
		});
	}

	private static void updateAttr(Element element, String name, Optional<String> value){
		if(value.isPresent()){
			element.attr(name, value.get());
		} else {
			element.removeAttr(name);
		}
	}

	private static void writeDocument(Document document, File file) throws FrontonIOException{
		//noinspection ResultOfMethodCallIgnored
		file.getParentFile().mkdirs();
		try(OutputStream stream = new FileOutputStream(file)) {
			try(Writer writer = new OutputStreamWriter(stream, document.charset())){
				writer.write(document.toString());
			}
		} catch (IOException e) {
			throw new FrontonIOException(e);
		}
	}
}
