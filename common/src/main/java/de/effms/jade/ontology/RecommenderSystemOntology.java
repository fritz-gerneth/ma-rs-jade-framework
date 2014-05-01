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
            ConceptSchema recommenderConcept = new ConceptSchema(RECOMMENDER);

            ConceptSchema userConcept = new ConceptSchema(USER);

            ConceptSchema userId = new ConceptSchema(USER_IDENTITY);
            userId.addSuperSchema(recommenderConcept);
            userId.add(USER_NAME, (PrimitiveSchema) getSchema(BasicOntology.STRING));
            userConcept.add(USER_UID, userId);

            ConceptSchema agentConcept = new ConceptSchema(AGENT);
            agentConcept.addSuperSchema(recommenderConcept);

            ConceptSchema itemConcept = new ConceptSchema(ITEM);

            final ConceptSchema actionConcept = new ConceptSchema(ACTION);
            actionConcept.addSuperSchema(itemConcept);
            actionConcept.add(ACTION_SUBJECT, new ConceptSchema(ConceptSchema.BASE_NAME));
            actionConcept.add(ACTION_OBJECT, new ConceptSchema(ConceptSchema.BASE_NAME), ObjectSchema.OPTIONAL);

            PredicateSchema doesPredicate = new PredicateSchema(DOES);
            doesPredicate.add(DOES_WHO, recommenderConcept);
            doesPredicate.add(DOES_WHAT, actionConcept);

            PredicateSchema isPredicate = new PredicateSchema(IS);
            isPredicate.add(IS_WHO, recommenderConcept);
            isPredicate.add(IS_WHAT, getSchema(ConceptSchema.BASE_NAME));

            PredicateSchema hasPredicate = new PredicateSchema(HAS);
            hasPredicate.add(HAS_WHO, recommenderConcept);
            hasPredicate.add(HAS_WHAT, getSchema(ConceptSchema.BASE_NAME));

            AgentActionSchema recommendsConcept = new AgentActionSchema(RECOMMENDS);
            recommendsConcept.addSuperSchema(actionConcept);
            recommendsConcept.addFacet(ACTION_OBJECT, new Facet()
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
            recommendsConcept.add(RECOMMENDS_REASON, new ConceptSchema(ConceptSchema.BASE_NAME), ObjectSchema.OPTIONAL);

            ConceptSchema preferenceConcept = new ConceptSchema(PREFERENCE);
            preferenceConcept.add(PREFERENCE_WHAT, (TermSchema) getSchema(TermSchema.BASE_NAME));
            preferenceConcept.add(PREFERENCE_RATING, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            userConcept.add(PREFERS, preferenceConcept, 0, ObjectSchema.UNLIMITED);

            ConceptSchema situationConcept = new ConceptSchema(SITUATION);
            userConcept.add(SITUATED, situationConcept, 0, ObjectSchema.UNLIMITED);

            this.add(recommenderConcept);
            this.add(userConcept);
            this.add(userId);
            this.add(agentConcept);

            this.add(itemConcept);
            this.add(actionConcept);

            this.add(doesPredicate);
            this.add(isPredicate);
            this.add(hasPredicate);

            this.add(recommendsConcept);
            this.add(preferenceConcept);
            this.add(situationConcept);
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
