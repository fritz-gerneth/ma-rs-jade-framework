package de.effms.jade.ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
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
        super(RecommenderSystemVocabulary.NAME, RelationalOntology.getInstance());

        try {
            ConceptSchema conceptSchema = new ConceptSchema(ConceptSchema.BASE_NAME);

            ConceptSchema recommenderConcept = new ConceptSchema(RECOMMENDER);
            recommenderConcept.addSuperSchema((ConceptSchema) getSchema(IDENTITY));

            ConceptSchema userConcept = new ConceptSchema(USER);
            userConcept.addSuperSchema(recommenderConcept);

            ConceptSchema agentConcept = new ConceptSchema(AGENT);
            agentConcept.addSuperSchema(recommenderConcept);

            final ConceptSchema actionConcept = new ConceptSchema(ACTION);

            ConceptSchema recommendationConcept = new ConceptSchema(RECOMMENDATION);
            recommendationConcept.addSuperSchema(actionConcept);
            recommendationConcept.add(RECOMMENDATION_ITEM, conceptSchema);
            recommendationConcept.add(RECOMMENDATION_REASON, conceptSchema);

            ConceptSchema preferenceConcept = new ConceptSchema(PREFERENCE);
            preferenceConcept.add(PREFERENCE_WHAT, conceptSchema);
            preferenceConcept.add(PREFERENCE_RATING, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            ConceptSchema situationConcept = new ConceptSchema(SITUATION);

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
