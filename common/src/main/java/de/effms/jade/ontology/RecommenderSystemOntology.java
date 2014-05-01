package de.effms.jade.ontology;

import jade.content.abs.AbsObject;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.*;

public class RecommenderSystemOntology extends Ontology implements RecommenderSystemVocabulary
{
    private static RecommenderSystemOntology instance = new RecommenderSystemOntology();

    public static RecommenderSystemOntology getInstance()
    {
        return RecommenderSystemOntology.instance;
    }

    private RecommenderSystemOntology()
    {
        super(RecommenderSystemVocabulary.NAME, new Ontology[] {RelationalOntology.getInstance(), BasicOntology.getInstance()}, new ReflectiveIntrospector());

        try {
            ConceptSchema recommenderConcept = new ConceptSchema(RECOMMENDER);

            ConceptSchema userConcept = new ConceptSchema(USER);
            userConcept.addSuperSchema(recommenderConcept);

            ConceptSchema agentConcept = new ConceptSchema(AGENT);
            agentConcept.addSuperSchema(recommenderConcept);

            ConceptSchema itemConcept = new ConceptSchema(ITEM);

            final ConceptSchema actionConcept = new ConceptSchema(ACTION);
            actionConcept.addSuperSchema(itemConcept);
            actionConcept.add(ACTION_SUBJECT, (ConceptSchema) getSchema(ConceptSchema.BASE_NAME));
            actionConcept.add(ACTION_OBJECT, (ConceptSchema) getSchema(ConceptSchema.BASE_NAME), ObjectSchema.OPTIONAL);

            AgentActionSchema recommendationConcept = new AgentActionSchema(RECOMMENDATION);
            recommendationConcept.addSuperSchema(actionConcept);
            recommendationConcept.addFacet(ACTION_OBJECT, new Facet()
            {
                @Override
                public void validate(AbsObject value, Ontology onto) throws OntologyException
                {
                    ObjectSchema valueSchema = onto.getSchema(value.getTypeName());
                    if (!valueSchema.isCompatibleWith(actionConcept)) {
                        throw new OntologyException("Value " + value + " is not an " + ACTION);
                    }
                }
            });
            recommendationConcept.add(RECOMMENDATION_REASON, (ConceptSchema) getSchema(ConceptSchema.BASE_NAME), ObjectSchema.OPTIONAL);

            ConceptSchema preferenceConcept = new ConceptSchema(PREFERENCE);
            preferenceConcept.add(PREFERENCE_WHAT, (TermSchema) getSchema(TermSchema.BASE_NAME));
            preferenceConcept.add(PREFERENCE_RATING, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            userConcept.add(PREFERS, preferenceConcept, 0, ObjectSchema.UNLIMITED);

            ConceptSchema situationConcept = new ConceptSchema(SITUATION);
            userConcept.add(SITUATED, situationConcept, 0, ObjectSchema.UNLIMITED);

            this.add(recommenderConcept);
            this.add(userConcept);
            this.add(agentConcept);

            this.add(itemConcept);
            this.add(actionConcept);

            this.add(recommendationConcept);
            this.add(preferenceConcept);
            this.add(situationConcept);
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
