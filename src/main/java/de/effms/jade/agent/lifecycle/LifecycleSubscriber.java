package de.effms.jade.agent.lifecycle;

public interface LifecycleSubscriber
{
    public void onSetup();

    public void onTakeDown();
}
