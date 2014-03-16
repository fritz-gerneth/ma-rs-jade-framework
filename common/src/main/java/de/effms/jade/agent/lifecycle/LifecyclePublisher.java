package de.effms.jade.agent.lifecycle;

public interface LifecyclePublisher
{
    public void registerLifecycleSubscriber(LifecycleSubscriber subscriber);

    public void removeLifecycleSubscriber(LifecycleSubscriber subscriber);
}
