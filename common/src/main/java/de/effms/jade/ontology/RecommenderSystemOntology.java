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
        super(RecommenderSystemVocabulary.NAME, new Ontology[] {BasicOntology.getInstance()}, new ReflectiveIntrospector());

        try {
            ConceptSchema recommenderConcept = new ConceptSchema(RecommenderSystemVocabulary.RECOMMENDER);

            ConceptSchema userConcept = new ConceptSchema(RecommenderSystemVocabulary.USER);

            ConceptSchema userId = new ConceptSchema(RecommenderSystemVocabulary.USER_IDENTITY);
            userId.addSuperSchema(recommenderConcept);
            userId.add(RecommenderSystemVocabulary.USER_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            userConcept.add(RecommenderSystemVocabulary.USER_UID, userId);

            ConceptSchema agentConcept = new ConceptSchema(RecommenderSystemVocabulary.AGENT);
            agentConcept.addSuperSchema(recommenderConcept);

            ConceptSchema itemConcept = new ConceptSchema(RecommenderSystemVocabulary.ITEM);

            final ConceptSchema actionConcept = new ConceptSchema(RecommenderSystemVocabulary.ACTION);
            actionConcept.addSuperSchema(itemConcept);
            actionConcept.add(RecommenderSystemVocabulary.ACTION_SUBJECT, new ConceptSchema(ConceptSchema.BASE_NAME));
            actionConcept.add(RecommenderSystemVocabulary.ACTION_OBJECT, new ConceptSchema(ConceptSchema.BASE_NAME), ObjectSchema.OPTIONAL);

            PredicateSchema doesPredicate = new PredicateSchema(RecommenderSystemOntology.DOES);
            doesPredicate.add(RecommenderSystemVocabulary.DOES_WHO, recommenderConcept);
            doesPredicate.add(RecommenderSystemOntology.DOES_WHAT, actionConcept);

            AgentActionSchema recommendsConcept = new AgentActionSchema(RecommenderSystemVocabulary.RECOMMENDS);
            recommendsConcept.addSuperSchema(actionConcept);
            recommendsConcept.addFacet(RecommenderSystemVocabulary.ACTION_OBJECT, new Facet()
            {
                @Override
                public void validate(AbsObject value, Ontology onto) throws OntologyException
                {
                    ObjectSchema valueSchema = onto.getSchema(value.getTypeName());
                    if (!valueSchema.isCompatibleWith(actionConcept)) {
                        throw new OntologyException("Value " + value + " is not an " + RecommenderSystemVocabulary.ACTION);
                    }
                }
            });
            recommendsConcept.add(RecommenderSystemVocabulary.RECOMMENDS_REASON, new ConceptSchema(ConceptSchema.BASE_NAME), ObjectSchema.OPTIONAL);

            ConceptSchema preferenceConcept = new ConceptSchema(RecommenderSystemVocabulary.PREFERENCE);
            preferenceConcept.add(RecommenderSystemVocabulary.PREFERENCE_WHAT, (TermSchema) getSchema(TermSchema.BASE_NAME));
            preferenceConcept.add(RecommenderSystemVocabulary.PREFERENCE_RATING, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            userConcept.add(RecommenderSystemVocabulary.PREFERS, preferenceConcept, 0, ObjectSchema.UNLIMITED);

            ConceptSchema situationConcept = new ConceptSchema(RecommenderSystemVocabulary.SITUATION);
            userConcept.add(RecommenderSystemVocabulary.SITUATED, situationConcept, 0, ObjectSchema.UNLIMITED);

            this.add(recommenderConcept);
            this.add(userConcept);
            this.add(userId);
            this.add(agentConcept);

            this.add(itemConcept);
            this.add(actionConcept);

            this.add(doesPredicate);

            this.add(recommendsConcept);
            this.add(preferenceConcept);
            this.add(situationConcept);
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
