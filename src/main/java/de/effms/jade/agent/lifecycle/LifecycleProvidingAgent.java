package de.effms.jade.agent.lifecycle;

import de.effms.jade.agent.Agent;

public interface LifecycleProvidingAgent extends Agent
{
    public void registerLifecycleSubscriber(LifecycleSubscriber subscriber);

    public void removeLifecycleSubscriber(LifecycleSubscriber subscriber);
}
