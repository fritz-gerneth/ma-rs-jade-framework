package de.effms.jade.agent;

import de.effms.jade.agent.lifecycle.LifecyclePublisher;
import de.effms.jade.agent.lifecycle.LifecycleSubscriber;
import jade.core.Agent;

import java.util.HashSet;
import java.util.Set;

public class AbstractAgent extends Agent implements LifecyclePublisher
{
    private Set<LifecycleSubscriber> lifecycleSubscribers;

    public AbstractAgent()
    {
        this.lifecycleSubscribers = new HashSet<>();
    }

    @Override
    public void registerLifecycleSubscriber(LifecycleSubscriber subscriber)
    {
        this.lifecycleSubscribers.add(subscriber);
    }

    @Override
    public void removeLifecycleSubscriber(LifecycleSubscriber subscriber)
    {
        this.lifecycleSubscribers.remove(subscriber);
    }

    @Override
    public void setup()
    {
        for (LifecycleSubscriber subscriber : this.lifecycleSubscribers) {
            subscriber.onSetup();
        }
    }

    @Override
    public void takeDown()
    {
        for (LifecycleSubscriber subscriber : this.lifecycleSubscribers) {
            subscriber.onTakeDown();
        }
    }
}
