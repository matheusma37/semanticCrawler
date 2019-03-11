package impl;

import crawler.SemanticCrawler;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class SemanticCrawlerImpl implements SemanticCrawler {
    private final List visitedURIs;
    private final Model model = ModelFactory.createDefaultModel();
    private final CharsetEncoder enc = Charset.forName("ISO-8859-1").newEncoder();
    
    public SemanticCrawlerImpl(){
        this.visitedURIs = new ArrayList();
    }
    
    /**
     *
     * @param graph
     * @param resourceURI
     */
    @Override
    public void search(Model graph, String resourceURI) {
        if (this.visitedURIs.indexOf(resourceURI) < 0){
            this.visitedURIs.add(resourceURI);
            
            if (enc.canEncode(resourceURI)){
                try {
                    model.read(resourceURI);
                    
                    graph.add(makeSearch(model, "<"+resourceURI+">"));
                    
                    getLinks(model, "<"+resourceURI+">").forEach(
                        (Resource link) -> {
                            if(link.isAnon()){
                                graph.add(makeSearch(model, link.getLocalName()));
                            } else {
                                search(graph, link.getURI());
                            }
                        }
                    );
                } catch (Exception e) {
                    System.out.println("Ocorreu uma excessão na search!\nClass: " +
                            e.getClass() + "\nCause: " + e.getCause() +
                            "\nMessage: " + e.getMessage());
                }
            }
        }
    }

    private Model makeSearch(Model model, String resourceURI){
        String queryString =
                "SELECT ?property ?value\n" +
                "WHERE {\n" +
                resourceURI + " ?property ?value.\n" +
                "}";
        
        Model m =  ModelFactory.createDefaultModel();
        
        Resource s;
        if(resourceURI.startsWith("<") && resourceURI.endsWith(">")){
            s = m.createResource(
                resourceURI.substring(1, resourceURI.length() - 1)
            );
        } else {
            s = m.createResource();
        }
        
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qe.execSelect();
            while(results.hasNext()){
                QuerySolution soln = results.nextSolution();
                
                Resource r = (Resource)(soln.get("property"));
                Property p = m.createProperty(r.getURI());
                
                RDFNode o = soln.get("value");
                if(o.isLiteral()){
                    s.addProperty (p, (Literal)o);
                }else if(o.isResource()){
                    Resource res = (Resource)o;
                    if(o.isAnon()){
                        m.add(makeSearch(model, res.getLocalName()));
                    } else {
                        s.addProperty(p, res.getURI());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Ocorreu uma excessão na makeSearch!\nClass: " + e.getClass() +
                    "\nCause: " + e.getCause() + "\nMessage: " + e.getMessage());
        } finally{
            return m;
        }
    }
    
    private List<Resource> getLinks(Model model, String resourceURI){
        List listOfLinks = new ArrayList();
        String queryString =
                "prefix owl: <http://www.w3.org/2002/07/owl#>" +
                "SELECT ?objectResource ?subjectResource\n" +
                "WHERE {\n" +
                    resourceURI + " owl:sameAs ?objectResource.\n" +
                    "?subjectResource owl:sameAs " + resourceURI + ".\n" +
                "}";
        
        Query query = QueryFactory.create(queryString);
        try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qe.execSelect();
            while(results.hasNext()){
                QuerySolution soln = results.nextSolution();
                Resource res = (Resource)(soln.get("objectResource"));
                listOfLinks.add(res);
                res = (Resource)(soln.get("subjectResource"));
                listOfLinks.add(res);
            }
        } catch (Exception e) {
            System.out.println("Ocorreu uma excessão getLinks!\nClass: " + e.getClass() +
                    "\nCause: " + e.getCause() + "\nMessage: " + e.getMessage());
        } finally{
            return listOfLinks;
        }
    }
}
