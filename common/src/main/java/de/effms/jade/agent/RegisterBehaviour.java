package de.effms.jade.agent;

import de.effms.jade.agent.lifecycle.LifecycleSubscriber;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class RegisterBehaviour implements LifecycleSubscriber
{
    private Agent agent;

    private Behaviour behaviour;

    public RegisterBehaviour(Agent agent, Behaviour behaviour)
    {
        this.agent = agent;
        this.behaviour = behaviour;
    }

    @Override
    public void onSetup()
    {
        agent.addBehaviour(behaviour);
    }

    @Override
    public void onTakeDown()
    {

    }
}
