package de.effms.jade.service.query;

import jade.content.abs.AbsConcept;
import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;

public interface QueryRefCallback
{
    public void onQueryRefResult(AbsIRE query, AbsConcept result);

    public void onLogicError(AbsPredicate query, AbsContentElement response);
}
