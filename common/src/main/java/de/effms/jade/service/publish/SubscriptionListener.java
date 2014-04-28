package de.effms.jade.service.publish;

import jade.content.abs.AbsPredicate;

public interface SubscriptionListener
{
    public void onInform(AbsPredicate result);

    public void onCancel();
}
