package de.effms.jade.ontology.gps;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PrimitiveSchema;

public class CoordinateOntology extends Ontology implements CoordinateVocabulary
{
    private static String name = "gps-coordinate";

    private static CoordinateOntology instance = new CoordinateOntology();

    public static CoordinateOntology getInstance() {
        return CoordinateOntology.instance;
    }

    private CoordinateOntology() {
        super(CoordinateOntology.name, BasicOntology.getInstance());

        try {
            ConceptSchema coordinateConcept = new ConceptSchema(COORDINATE);
            coordinateConcept.add(LONGITUDE, (PrimitiveSchema) this.getSchema(BasicOntology.FLOAT));
            coordinateConcept.addFacet(LONGITUDE, new ValidLongitude());
            coordinateConcept.add(LATITUDE, (PrimitiveSchema) this.getSchema(BasicOntology.FLOAT));
            coordinateConcept.addFacet(LATITUDE, new ValidLatitude());
            this.add(coordinateConcept, Coordinate.class);
        } catch (OntologyException oe) {
            oe.printStackTrace();
        }
    }
}
