package uk.ac.ebi.spot.sandbox.patternanalyser.service;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

@Service
public class OntologyLoadingService {


    private OWLOntology ontology = null;
    private OWLOntologyManager manager;

    private OWLDataFactory dataFactory;

    private OWLReasoner reasoner;
    private OWLReasonerConfiguration config;
    private  OWLReasonerFactory reasonerFactory;

    @Autowired
    public OntologyLoadingService(){

        reasonerFactory = new StructuralReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        config = new SimpleConfiguration(progressMonitor);

        // Get hold of an ontology manager
        manager = OWLManager.createOWLOntologyManager();
        dataFactory = OWLManager.getOWLDataFactory();

        

    }

    public OWLDataFactory getDataFactory() {
        return dataFactory;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public void setReasoner(OWLOntology ontology){
        reasoner = reasonerFactory.createReasoner(ontology, config);

        System.out.println("Precomputing inferences...");
        reasoner.precomputeInferences();

        System.out.println("Checking ontology consistency...");
        reasoner.isConsistent();

        System.out.println("Checking for unsatisfiable classes...");
        if (reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().size() > 0) {
            throw new RuntimeException("Once classified, unsatisfiable classes were detected");
        }
        else {
            System.out.println("Reasoning complete! ");
        }
    }

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public OWLOntology loadOntology(String fileName, String ontIri){
        System.setProperty("entityExpansionLimit", "100000000");

        System.out.println("Trying to load the ontology");


        try {

            // Let's load an ontology from the web
            IRI iri = IRI.create(ontIri);

            File file = new File(fileName);
            manager.addIRIMapper(new SimpleIRIMapper(iri, IRI.create(file)));


            ontology = manager.loadOntology(iri);

            System.out.println("Loaded ontology: " + ontology);

        }


        catch (OWLOntologyCreationIOException e) {
            // IOExceptions during loading get wrapped in an OWLOntologyCreationIOException
            IOException ioException = (IOException) e.getCause();
            if (ioException instanceof FileNotFoundException) {
                System.out.println("Could not load ontology. File not found: " + ioException.getMessage());
            }
            else if (ioException instanceof UnknownHostException) {
                System.out.println("Could not load ontology. Unknown host: " + ioException.getMessage());
            }
            else {
                System.out.println("Could not load ontology: " + ioException.getClass().getSimpleName() + " " + ioException.getMessage());
            }
        }
        catch (UnparsableOntologyException e) {
            // If there was a problem loading an ontology because there are syntax errors in the document (file) that
            // represents the ontology then an UnparsableOntologyException is thrown
            System.out.println("Could not parse the ontology: " + e.getMessage());
            // A map of errors can be obtained from the exception
            Map<OWLParser, OWLParserException> exceptions = e.getExceptions();
            // The map describes which parsers were tried and what the errors were
            for (OWLParser parser : exceptions.keySet()) {
                System.out.println("Tried to parse the ontology with the " + parser.getClass().getSimpleName() + " parser");
                System.out.println("Failed because: " + exceptions.get(parser).getMessage());
            }
        }
        catch (UnloadableImportException e) {
            // If our ontology contains imports and one or more of the imports could not be loaded then an
            // UnloadableImportException will be thrown (depending on the missing imports handling policy)
            System.out.println("Could not load import: " + e.getImportsDeclaration());
            // The reason for this is specified and an OWLOntologyCreationException
            OWLOntologyCreationException cause = e.getOntologyCreationException();
            System.out.println("Reason: " + cause.getMessage());
        }
        catch (OWLOntologyCreationException e) {
            System.out.println("Could not load ontology: " + e.getMessage());
        }

        return ontology;
    }
}
