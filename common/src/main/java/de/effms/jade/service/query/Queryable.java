package de.effms.jade.service.query;

import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;
import jade.content.onto.Ontology;

/**
 * Interface for querying a knowledge base. Every Queryable instance may only speak one ontology.
 */
public interface Queryable
{
    /**
     * Query if given predicate holds true
     */
    public void queryIf(AbsPredicate query, QueryIfCallback callback);

    /**
     * Query for the value of identifying reverential expression
     */
    public void queryRef(AbsIRE query, QueryRefCallback callback);

    /**
     * Get the
     */
    public Ontology getOntology();
}
