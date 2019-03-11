package crawler;

import org.apache.jena.rdf.model.Model;

public interface SemanticCrawler {
    public void search(Model graph, String resourceURI);
}
