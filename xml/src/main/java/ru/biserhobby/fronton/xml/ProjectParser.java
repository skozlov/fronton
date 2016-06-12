package ru.biserhobby.fronton.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.biserhobby.fronton.core.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
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

	public ProjectParser(File sourceBasedir, File targetBasedir) {
		this.sourceBasedir = sourceBasedir;
		this.targetBasedir = targetBasedir;
	}

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

	private Charset parseCharset(Element root) throws ProjectParsingException{
		String name = getNotEmptyAttr(root, "charset");
		try {
			return Charset.forName(name);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException e){
			throw new ProjectParsingException(e);
		}
	}

	private Runnable parseTopDirective(Element element, Charset charset){
		String tagName = element.getTagName();
		if("page".equals(tagName)){
			return parseSimplePage(element);
		} else if("pages".equals(tagName)){
			return parseTopPages(element, charset);
		} else {
			throw new ProjectParsingException(String.format("Illegal top-level tag `%s`", tagName));
		}
	}

	private SimplePage parseSimplePage(Element element){
		Map.Entry<File, File> sourceAndTarget = parsePageSourceAndTarget(element);
		File source = sourceAndTarget.getKey();
		File target = sourceAndTarget.getValue();
		return new SimplePage(source.toPath(), target.toPath());
	}

	private TopPages parseTopPages(Element element, Charset charset){
		return new TopPages(parsePages(element, charset));
	}

	private Pages parsePages(Element element, Charset charset){
		return new Pages(
				parseTemplate(element),
				charset,
				parseTemplateContainerSelector(element),
				parseSubPages(element, charset));
	}

	private File parseTemplate(Element element){
		return new File(sourceBasedir, getNotEmptyAttr(element, "template"));
	}

	private String parseTemplateContainerSelector(Element element){
		return parseContainerSelector(element, "container");
	}

	private String parseSourceContainerSelector(Element element){
		return parseContainerSelector(element, "sourceContainer");
	}

	private String parseContainerSelector(Element element, String attrName){
		String selector = element.getAttribute(attrName);
		return (selector == null || selector.isEmpty()) ? "body" : selector;
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

	private RemoveContainer parseRemoveContainer(Element element) {
		return new RemoveContainer(parsePageTarget(element));
	}

	private Insert parseInsert(Element element) {
		Map.Entry<File, File> sourceAndTarget = parsePageSourceAndTarget(element);
		File source = sourceAndTarget.getKey();
		File target = sourceAndTarget.getValue();
		String sourceContainerSelector = parseSourceContainerSelector(element);
		return new Insert(source, sourceContainerSelector, target);
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

	private Map.Entry<File, File> parsePageSourceAndTarget(Element element){
		String sourceName = getNotEmptyNullableAttr(element, "src");
		String targetName = getNotEmptyNullableAttr(element, "target");
		if(sourceName == null){
			if(targetName == null) {
				throw new ProjectParsingException(String.format(
						"Neither `src` nor `target` attribute is specified (%s)", getLocation(element)));
			}
			sourceName = targetName;
		} else if(targetName == null){
			targetName = sourceName;
		}
		File source = new File(sourceBasedir, sourceName);
		File target = new File(targetBasedir, targetName);
		return new AbstractMap.SimpleImmutableEntry<>(source, target);
	}

	private File parsePageTarget(Element element){
		String name = getNotEmptyAttr(element, "target");
		return new File(targetBasedir, name);
	}

	private static Stream<Element> getElements(NodeList nodes){
		return IntStream.range(0, nodes.getLength()).mapToObj(i -> {
			Node node = nodes.item(i);
			if(node instanceof Element){
				return (Element)node;
			} else {
				throw new ProjectParsingException(String.format(
						"Only elements are allowed, but found `%s` (%s)", node.getClass(), node));
			}
		});
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
		String value = element.getAttribute(attrName);
		if(value != null && value.isEmpty()){
			throw new ProjectParsingException(String.format(
					"Illegal empty attribute `%s` (%s)", attrName, getLocation(element)));
		}
		return value;
	}

	private static String getLocation(Node node){
		return getLocationBuilder(node).toString();
	}

	private static StringBuilder getLocationBuilder(Node node){
		Node parent = node.getParentNode();
		if(parent == null){
			return new StringBuilder(((Element)node).getTagName());
		} else {
			StringBuilder builder = getLocationBuilder(parent).append('[');
			NodeList children = parent.getChildNodes();
			for(int i = 0; i < children.getLength(); i++){
				if(node == children.item(i)){
					builder.append(i);
					break;
				}
			}
			builder.append(']');
			return builder;
		}
	}
}