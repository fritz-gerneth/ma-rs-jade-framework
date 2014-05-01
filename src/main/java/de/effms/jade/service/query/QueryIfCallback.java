package de.effms.jade.service.query;

import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsPredicate;

public interface QueryIfCallback
{
    public void onQueryIfResult(AbsPredicate query, boolean result);

    public void onLogicError(AbsPredicate query, AbsContentElement response);
}
