package ru.biserhobby.fronton.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public final class Utils {
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
			Element template, String templateContainerSelector, Element source, String sourceContainerSelector)
			throws InvalidSelectorException{

		checkArgumentNotNull(template, "template");
		checkArgumentNotEmpty(templateContainerSelector, "templateContainerSelector");
		checkArgumentNotNull(source, "source");
		checkArgumentNotEmpty(sourceContainerSelector, "sourceContainerSelector");
		String htmlToInsert = select(source, sourceContainerSelector).html();
		select(template, templateContainerSelector).html(htmlToInsert);
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
}
