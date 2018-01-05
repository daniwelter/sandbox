package uk.ac.ebi.spot.sandbox.patternanalyser.service;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

@Service
public class PatternAnalysisService {


    private OntologyLoadingService ontologyLoadingService;


    @Autowired
    public PatternAnalysisService(OntologyLoadingService ontologyLoadingService){
        this.ontologyLoadingService = ontologyLoadingService;

    }


    public void analysePatterns(String fileName, String iri){
        OWLOntology ontology = ontologyLoadingService.loadOntology(fileName, iri);

        System.out.println("Ontology loaded");

        ontologyLoadingService.setReasoner(ontology);

        NodeSet<OWLClass> children = getChildren("http://purl.obolibrary.org/obo/CL_0000000");

        System.out.println("There are " + children.entities().count() + " children");

        RestrictionVisitor restrictionVisitor = new RestrictionVisitor(singleton(ontology));

        identifyProperties(children, ontology);
        
    }


    public NodeSet<OWLClass> getChildren(String iri){

        OWLClass parent = ontologyLoadingService.getDataFactory().getOWLClass(IRI.create(iri));

        return ontologyLoadingService.getReasoner().getSubClasses(parent);

    }


    public void identifyProperties(NodeSet<OWLClass> classes, OWLOntology ontology){
        List<OWLProperty> objectProperties = new ArrayList<>();

        List<OWLClass> owlcls = classes.entities().collect(Collectors.toList());

        List<OWLEntity> properties = new ArrayList<>();

        for (OWLClass cls : owlcls){
//            System.out.println("Child class " +  cls);

            RestrictionVisitor restrictionVisitor = new RestrictionVisitor(singleton(ontology));


            ontology.subClassAxiomsForSubClass(cls)
                    .forEach(ax -> ax.getSuperClass().accept(restrictionVisitor));

             System.out.println("Restricted properties for " + cls
             + ": " + restrictionVisitor.getRestrictedProperties().size());
             for (OWLObjectPropertyExpression prop : restrictionVisitor
             .getRestrictedProperties()) {
//               System.out.println(" " + prop);

                 if (!properties.contains(prop.getNamedProperty())){
                     properties.add(prop.getNamedProperty());
                 }
             }

//            ontologyLoadingService.getReasoner().getSuperClasses(cls).entities().collect(Collectors.toList()).forEach(
//                    axiom -> System.out.println(axiom)
//            );

        }


        OWLAnnotationProperty rdfsLabelAnnotationProperty = ontologyLoadingService.getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());

        System.out.println("Properties used in subclasses of cell:");
        for (OWLEntity prop : properties){

            EntitySearcher.getAnnotations(prop, ontology, rdfsLabelAnnotationProperty).forEach(
                a -> {
                    OWLAnnotationValue value = a.getValue();
                    if(value instanceof OWLLiteral) {
                        System.out.println(prop + " labelled " + ((OWLLiteral) value).getLiteral());
                    }
                }
            );
        }
    }


    /**
     * Visits existential restrictions and collects the properties which are
     * restricted.
     */
    private static class RestrictionVisitor implements OWLClassExpressionVisitor {

        private final @Nonnull
        Set<OWLClass> processedClasses;
        private final @Nonnull
        Set<OWLObjectPropertyExpression> restrictedProperties;
        private final Set<OWLOntology> onts;

        RestrictionVisitor(Set<OWLOntology> onts) {
            restrictedProperties = new HashSet<>();
            processedClasses = new HashSet<>();
            this.onts = onts;
        }

        public Set<OWLObjectPropertyExpression> getRestrictedProperties() {
            return restrictedProperties;
        }

        @Override
        public void visit(OWLClass ce) {
            // avoid cycles
            if (!processedClasses.contains(ce)) {
                // If we are processing inherited restrictions then
                // we recursively visit named supers.
                processedClasses.add(ce);
                for (OWLOntology ont : onts) {
                    ont.subClassAxiomsForSubClass(ce)
                            .forEach(ax -> ax.getSuperClass().accept(this));
                }
            }
        }

        @Override
        public void visit(OWLObjectSomeValuesFrom ce) {
            // This method gets called when a class expression is an
            // existential (someValuesFrom) restriction and it asks us to visit
            // it
            restrictedProperties.add(ce.getProperty());
        }
    }
    
    
}
