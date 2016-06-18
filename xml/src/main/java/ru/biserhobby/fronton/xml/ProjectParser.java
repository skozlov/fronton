package ru.biserhobby.fronton.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.biserhobby.fronton.core.*;
import ru.biserhobby.fronton.core.CustomAttributeProcessor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ProjectParser implements Function<File, Project> {
	private static final DocumentBuilder DOCUMENT_BUILDER;

	static {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		try {
			DOCUMENT_BUILDER = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private final File sourceBasedir;
	private final File targetBasedir;
	private final CustomAttributeProcessor customAttributeProcessor;

	public ProjectParser(File sourceBasedir, File targetBasedir, CustomAttributeProcessor customAttributeProcessor) {
		Utils.checkArgumentNotNull(sourceBasedir, "sourceBasedir");
		Utils.checkArgumentNotNull(targetBasedir, "targetBasedir");
		Utils.checkArgumentNotNull(customAttributeProcessor, "customAttributeProcessor");
		this.sourceBasedir = sourceBasedir;
		this.targetBasedir = targetBasedir;
		this.customAttributeProcessor = customAttributeProcessor;
	}

	@Override
	public Project apply(File file) throws ProjectParsingException, FrontonIOException {
		Utils.checkArgumentNotNull(file, "file");
		try {
			Document document = DOCUMENT_BUILDER.parse(file);
			Element root = document.getDocumentElement();
			Charset charset = parseCharset(root);
			Stream<Runnable> topDirectives = getElements(root.getChildNodes())
					.map(e -> parseTopDirective(e, charset));
			return new Project(topDirectives);
		} catch (SAXException e) {
			throw new ProjectParsingException(e);
		} catch (IOException e) {
			throw new FrontonIOException(e);
		}
	}

	private Charset parseCharset(Element root) {
		String name = getNotEmptyAttr(root, "charset");
		try {
			return Charset.forName(name);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException e){
			throw new ProjectParsingException(e);
		}
	}

	private Runnable parseTopDirective(Element element, Charset charset){
		String tagName = element.getTagName();
		if("simplePage".equals(tagName)){
			return parseSimplePage(element, charset);
		} else if("topPages".equals(tagName)){
			return parseTopPages(element, charset);
		} else {
			throw new ProjectParsingException(String.format("Illegal tag `%s` (%s)", tagName, getLocation(element)));
		}
	}

	private SimplePage parseSimplePage(Element element, Charset charset){
		Map.Entry<File, File> sourceAndTarget = parsePageSourceAndTarget(element);
		File source = sourceAndTarget.getKey();
		File target = sourceAndTarget.getValue();
		return new SimplePage(source, target, charset, customAttributeProcessor);
	}

	private TopPages parseTopPages(Element element, Charset charset){
		return new TopPages(parsePages(element, charset), customAttributeProcessor);
	}

	private Pages parsePages(Element element, Charset charset){
		return new Pages(
				parseTemplate(element),
				charset,
				parseTemplateContainerSelector(element),
				parseSubPages(element, charset));
	}

	private Stream<? extends BiFunction<org.jsoup.nodes.Document, String, Stream<Map.Entry<org.jsoup.nodes.Document, File>>>>
		parseSubPages(Element element, Charset charset){

		return getElements(element.getChildNodes()).map(e -> parseInnerDirective(e, charset));
	}

	private BiFunction<org.jsoup.nodes.Document, String, Stream<Map.Entry<org.jsoup.nodes.Document, File>>>
		parseInnerDirective(Element element, Charset charset){

		String tagName = element.getTagName();
		if("page".equals(tagName)){
			return parseInnerPage(element);
		} else if("pages".equals(tagName)){
			return parseInnerPages(element, charset);
		} else {
			throw new ProjectParsingException(String.format(
					"Illegal tag `%s` (%s)", tagName, getLocation(element)));
		}
	}

	private BiFunction<org.jsoup.nodes.Document, String, Stream<Map.Entry<org.jsoup.nodes.Document, File>>>
		parseInnerPages(Element element, Charset charset) {

		String action = getNotEmptyNullableAttr(element, "action");
		if(action == null || action.equals("insert")){
			return parseInsertPages(element, charset);
		} else {
			throw new ProjectParsingException(String.format(
					"Unsupported action `%s` (%s)", action, getLocation(element)));
		}
	}

	private InsertPages parseInsertPages(Element element, Charset charset){
		String sourceContainerSelector = parseSourceContainerSelector(element);
		Pages pages = parsePages(element, charset);
		return new InsertPages(sourceContainerSelector, pages);
	}

	private BiFunction<org.jsoup.nodes.Document, String, Stream<Map.Entry<org.jsoup.nodes.Document, File>>>
		parseInnerPage(Element element) {

		String action = getNotEmptyNullableAttr(element, "action");
		if(action == null || action.equals("insert")){
			return parseInsert(element);
		} else if("removeContainer".equals(action)){
			return parseRemoveContainer(element);
		} else {
			throw new ProjectParsingException(String.format(
					"Unsupported action `%s` (%s)", action, getLocation(element)));
		}
	}

	private Insert parseInsert(Element element) {
		Map.Entry<File, File> sourceAndTarget = parsePageSourceAndTarget(element);
		File source = sourceAndTarget.getKey();
		File target = sourceAndTarget.getValue();
		String sourceContainerSelector = parseSourceContainerSelector(element);
		return new Insert(source, sourceContainerSelector, target);
	}

	private RemoveContainer parseRemoveContainer(Element element) {
		return new RemoveContainer(parsePageTarget(element));
	}

	private File parsePageTarget(Element element){
		String name = getNotEmptyAttr(element, "target");
		return new File(targetBasedir, name);
	}

	private Map.Entry<File, File> parsePageSourceAndTarget(Element element){
		String sourceName = getNotEmptyAttr(element, "src");
		String targetName = getNotEmptyNullableAttr(element, "target");
		if(targetName == null){
			targetName = sourceName;
		}
		File source = new File(sourceBasedir, sourceName);
		File target = new File(targetBasedir, targetName);
		return new AbstractMap.SimpleImmutableEntry<>(source, target);
	}

	private String parseTemplateContainerSelector(Element element){
		return parseContainerSelector(element, "container");
	}

	private String parseSourceContainerSelector(Element element){
		return parseContainerSelector(element, "sourceContainer");
	}

	private String parseContainerSelector(Element element, String attrName){
		String selector = getNotEmptyNullableAttr(element, attrName);
		return (selector == null) ? "body" : selector;
	}

	private File parseTemplate(Element element){
		return new File(sourceBasedir, getNotEmptyAttr(element, "template"));
	}

	private static String getNotEmptyAttr(Element element, String attrName){
		String value = getNotEmptyNullableAttr(element, attrName);
		if(value == null){
			throw new ProjectParsingException(String.format(
					"Missing required attribute `%s` (%s)", attrName, getLocation(element)));
		}
		return value;
	}

	private static String getNotEmptyNullableAttr(Element element, String attrName){
		if(!element.hasAttribute(attrName)){
			return null;
		}
		String value = element.getAttribute(attrName);
		if(value.isEmpty()){
			throw new ProjectParsingException(String.format(
					"Illegal empty attribute `%s` (%s)", attrName, getLocation(element)));
		}
		return value;
	}

	private static String getLocation(Element element){
		return getLocationBuilder(element).toString();
	}

	private static StringBuilder getLocationBuilder(Element element){
		Node parent = element.getParentNode();
		if(parent instanceof Document){
			return new StringBuilder(element.getTagName());
		} else {
			StringBuilder builder = getLocationBuilder((Element) parent).append('[');
			List<Element> elements = getElements(parent.getChildNodes()).collect(Collectors.toList());
			int i = 0;
			for(Element e : elements){
				if(element.isSameNode(e)){
					builder.append(i);
					break;
				}
				i++;
			}
			builder.append(']');
			return builder;
		}
	}

	private static Stream<Element> getElements(NodeList nodes){
		return IntStream.range(0, nodes.getLength())
				.mapToObj(nodes::item)
				.flatMap(node -> (node instanceof Element) ? Stream.of((Element) node) : Stream.empty());
	}
}
