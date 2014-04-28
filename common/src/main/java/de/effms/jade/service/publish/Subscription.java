package de.effms.jade.service.publish;

import jade.content.abs.AbsIRE;

import java.util.UUID;

public class Subscription
{
    private final AbsIRE query;

    private final String subscriptionID = UUID.randomUUID().toString();

    private SubscriptionListener callback;

    public Subscription(AbsIRE query)
    {
        this.query = query;
    }

    public AbsIRE getQuery()
    {
        return this.query;
    }

    public String getSubscriptionID()
    {
        return this.subscriptionID;
    }

    public void setCallback(SubscriptionListener callback)
    {
        this.callback = callback;
    }

    public SubscriptionListener getCallback()
    {
        return this.callback;
    }
}
