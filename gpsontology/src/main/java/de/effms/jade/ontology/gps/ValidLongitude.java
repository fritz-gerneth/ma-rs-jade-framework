package de.effms.jade.ontology.gps;

import jade.content.abs.AbsObject;
import jade.content.abs.AbsPrimitive;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.Facet;

public class ValidLongitude implements Facet
{
    @Override
    public void validate(AbsObject absObject, Ontology ontology) throws OntologyException
    {
        try {
            float coordinate = ((AbsPrimitive) absObject).getFloat();
            if (coordinate > 180 || coordinate < -180) {
                throw new OntologyException("Longitude not between [-180,180]");
            }
        } catch (Exception e) {
            throw new OntologyException("Not a valid float value", e);
        }
    }
}
