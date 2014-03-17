package de.effms.jade.agent.lifecycle;

public interface LifecycleProvidingAgent
{
    public void registerLifecycleSubscriber(LifecycleSubscriber subscriber);

    public void removeLifecycleSubscriber(LifecycleSubscriber subscriber);
}
