package de.effms.jade.ontology;

import jade.content.abs.AbsObject;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.ReflectiveIntrospector;
import jade.content.schema.*;

public class RecommenderSystemOntology extends Ontology implements RecommenderSystemVocabulary, RelationalVocabulary
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
            recommenderConcept.addSuperSchema((ConceptSchema) getSchema(IDENTITY));

            ConceptSchema userConcept = new ConceptSchema(USER);
            userConcept.addSuperSchema(recommenderConcept);

            ConceptSchema agentConcept = new ConceptSchema(AGENT);
            agentConcept.addSuperSchema(recommenderConcept);

            final ConceptSchema actionConcept = new ConceptSchema(ACTION);

            ConceptSchema recommendationConcept = new AgentActionSchema(RECOMMENDATION);
            recommendationConcept.add(RECOMMENDATION_SUBJECT, (ConceptSchema) getSchema(ConceptSchema.BASE_NAME));
            recommendationConcept.add(RECOMMENDATION_REASON, (ConceptSchema) getSchema(ConceptSchema.BASE_NAME));

            ConceptSchema preferenceConcept = new ConceptSchema(PREFERENCE);
            preferenceConcept.add(PREFERENCE_WHAT, (TermSchema) getSchema(TermSchema.BASE_NAME));
            preferenceConcept.add(PREFERENCE_RATING, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            ConceptSchema situationConcept = new ConceptSchema(SITUATION);
            userConcept.add(SITUATED, situationConcept, 0, ObjectSchema.UNLIMITED);

            this.add(recommenderConcept);
            this.add(userConcept);
            this.add(agentConcept);

            this.add(actionConcept);

            this.add(recommendationConcept);
            this.add(preferenceConcept);
            this.add(situationConcept);
        } catch (OntologyException e) {
            e.printStackTrace();
        }

    }
}
