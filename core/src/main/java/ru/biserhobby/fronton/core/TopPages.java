package ru.biserhobby.fronton.core;

import org.jsoup.nodes.Document;

import java.io.*;

public class TopPages implements Runnable {
	private final Pages pages;

	public TopPages(Pages pages){
		Utils.checkArgumentNotNull(pages, "pages");
		this.pages = pages;
	}

	private void writeDocument(Document document, File file) throws FrontonIOException{
		try(OutputStream stream = new FileOutputStream(file)) {
			try(Writer writer = new OutputStreamWriter(stream, document.charset())){
				writer.write(document.toString());
			}
		} catch (IOException e) {
			throw new FrontonIOException(e);
		}
	}

	@Override
	public void run() throws FrontonException{
		pages.processInner().parallel().forEach(entry -> writeDocument(entry.getKey(), entry.getValue()));
	}
}
