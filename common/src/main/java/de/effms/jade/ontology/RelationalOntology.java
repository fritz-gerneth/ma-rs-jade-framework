package de.effms.jade.ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.*;

public class RelationalOntology extends Ontology implements RelationalVocabulary
{
    private static RelationalOntology instance = new RelationalOntology();

    public static RelationalOntology getInstance()
    {
        return RelationalOntology.instance;
    }

    private RelationalOntology()
    {
        super(RecommenderSystemVocabulary.NAME, BasicOntology.getInstance());

        try {
            PredicateSchema doesPredicate = new PredicateSchema(DOES);
            doesPredicate.add(DOES_WHO, getSchema(ConceptSchema.BASE_NAME));
            doesPredicate.add(DOES_WHAT, getSchema(ConceptSchema.BASE_NAME));

            PredicateSchema isPredicate = new PredicateSchema(IS);
            isPredicate.add(IS_WHO, getSchema(ConceptSchema.BASE_NAME));
            isPredicate.add(IS_WHAT, getSchema(ConceptSchema.BASE_NAME));

            PredicateSchema hasPredicate = new PredicateSchema(HAS);
            hasPredicate.add(HAS_WHO, getSchema(ConceptSchema.BASE_NAME));
            hasPredicate.add(HAS_WHAT, getSchema(ConceptSchema.BASE_NAME));

            PredicateSchema identifiedBy = new PredicateSchema(IDENTIFIED);
            identifiedBy.addSuperSchema(isPredicate);

            this.add(doesPredicate);
            this.add(isPredicate);
            this.add(hasPredicate);
            this.add(identifiedBy);
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
