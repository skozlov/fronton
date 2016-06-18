package ru.biserhobby.fronton.core;

public class TopPages implements Runnable {
	private final Pages pages;
	private final CustomAttributeProcessor customAttributeProcessor;

	public TopPages(Pages pages, CustomAttributeProcessor customAttributeProcessor){
		Utils.checkArgumentNotNull(pages, "pages");
		Utils.checkArgumentNotNull(customAttributeProcessor, "customAttributeProcessor");
		this.pages = pages;
		this.customAttributeProcessor = customAttributeProcessor;
	}

	@Override
	public void run() throws FrontonException{
		pages.processInner().parallel().forEach(entry -> Utils.processFinalDocument(entry.getKey(), entry.getValue(), customAttributeProcessor));
	}
}
