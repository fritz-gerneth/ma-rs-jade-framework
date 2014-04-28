package de.effms.jade.service.publish;

import jade.content.abs.AbsIRE;
import jade.content.onto.Ontology;

public interface Subscribable
{
    public Subscription subscribe(AbsIRE query);

    public void cancel(Subscription subscription);

    public Ontology getOntology();
}
