package de.effms.jade.service.publish;

import jade.content.abs.AbsConcept;
import jade.content.abs.AbsIRE;

public interface SubscriptionListener
{
    public void onInform(AbsIRE query, AbsConcept result);

    public void onCancel();
}
