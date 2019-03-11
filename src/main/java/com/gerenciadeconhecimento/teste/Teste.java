package com.gerenciadeconhecimento.teste;

import crawler.SemanticCrawler;
import impl.SemanticCrawlerImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class Teste {
    
    public static final String RDF_FILE = "http://dbpedia.org/resource/Roger_Federer";
    
    public static void main(String[] args) {
        SemanticCrawler sc = new SemanticCrawlerImpl();
        Model graph = ModelFactory.createDefaultModel();
        sc.search(graph, RDF_FILE);
        try{
            graph.write(System.out, "TURTLE");
        }catch(Exception e){
            System.out.println("\nOcorreu uma excessão na main!\nClass: " +
                                e.getClass() + "\nCause: " + e.getCause() +
                                "\nMessage: " + e.getMessage()
            );
        }
    }
}
