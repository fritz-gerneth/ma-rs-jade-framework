package de.effms.jade.ontology;

import jade.content.abs.AbsObject;
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
        super(NAME, BasicOntology.getInstance());

        try {
            ConceptSchema conceptSchema = new ConceptSchema(ConceptSchema.BASE_NAME);

            PredicateSchema doesPredicate = new PredicateSchema(DOES);
            doesPredicate.add(DOES_WHO, conceptSchema);
            doesPredicate.add(DOES_WHAT, conceptSchema);

            PredicateSchema isPredicate = new PredicateSchema(IS);
            isPredicate.add(IS_WHO, conceptSchema);
            isPredicate.add(IS_WHAT, conceptSchema);

            PredicateSchema hasPredicate = new PredicateSchema(HAS);
            hasPredicate.add(HAS_WHO, conceptSchema);
            hasPredicate.add(HAS_WHAT, conceptSchema);

            final ConceptSchema identityConcept = new ConceptSchema(IDENTITY);
            identityConcept.add(IDENTITY_UID, (PrimitiveSchema) getSchema(BasicOntology.STRING));

            PredicateSchema identifiedBy = new PredicateSchema(IDENTIFIED);
            identifiedBy.addSuperSchema(isPredicate);
            identifiedBy.addFacet(IS_WHAT, new Facet()
            {
                @Override
                public void validate(AbsObject value, Ontology onto) throws OntologyException
                {
                    ObjectSchema valueSchema = onto.getSchema(value.getTypeName());
                    if (!valueSchema.isCompatibleWith(identityConcept)) {
                        throw new OntologyException("Value " + value + " is not an " + IDENTITY);
                    }
                }
            });

            this.add(doesPredicate);
            this.add(isPredicate);
            this.add(hasPredicate);
            this.add(identityConcept);
            this.add(identifiedBy);
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
