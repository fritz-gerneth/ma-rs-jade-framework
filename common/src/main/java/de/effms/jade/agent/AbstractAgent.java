package de.effms.jade.agent;

import de.effms.jade.agent.lifecycle.LifecycleProvidingAgent;
import de.effms.jade.agent.lifecycle.LifecycleSubscriber;
import de.effms.jade.agent.service.ServiceProvidingAgent;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAgent extends jade.core.Agent implements Agent, LifecycleProvidingAgent, ServiceProvidingAgent
{
    private Set<LifecycleSubscriber> lifecycleSubscribers = new HashSet<>();

    protected final DFAgentDescription agentDescription = new DFAgentDescription();

    public AbstractAgent()
    {
        super();

        this.registerLifecycleSubscriber(new DFRegistrationHandler());
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
            try {
                subscriber.onSetup();
            } catch (Exception e) {
                log().error(e.getMessage());
                log().debug(e.getStackTrace().toString());
            }
        }
    }

    @Override
    public void takeDown()
    {
        for (LifecycleSubscriber subscriber : this.lifecycleSubscribers) {
            try {
                subscriber.onTakeDown();
            } catch (Exception e) {
                log().error(e.getMessage());
                log().debug(e.getStackTrace().toString());
            }
        }
    }

    @Override
    public void registerService(ServiceDescription serviceDescription, Ontology ontology)
    {
        this.registerService(serviceDescription, ontology, new SLCodec());
    }

    @Override
    public void registerService(ServiceDescription serviceDescription, Ontology ontology, Codec language)
    {
        serviceDescription.addLanguages(language.getName());
        serviceDescription.addOntologies(ontology.getName());
        agentDescription.addServices(serviceDescription);

        log().info("Added service " + serviceDescription.getName());
        log().debug(serviceDescription.toString());

        this.getContentManager().registerLanguage(language);
        this.getContentManager().registerOntology(ontology);

        log().info("Registered language " + language);
        log().info("Registered ontology " + ontology);
    }

    protected abstract Logger log();

    private class DFRegistrationHandler implements LifecycleSubscriber
    {
        @Override
        public void onSetup()
        {
            agentDescription.setName(AbstractAgent.this.getAID());
            try {
                log().info("Registering Agent " + AbstractAgent.this.getName() + " at DF");
                DFService.register(AbstractAgent.this, agentDescription);
            } catch (FIPAException ex) {
                log().error(ex.getMessage());
                log().debug(ex.getStackTrace().toString());
            }
        }

        @Override
        public void onTakeDown()
        {
            try {
                log().info("DeRegistering Agent " + AbstractAgent.this.getName() + " at DF");
                DFService.deregister(AbstractAgent.this);
            } catch (FIPAException ex) {
                log().error(ex.getMessage());
                log().debug(ex.getStackTrace().toString());
            }
        }
    }
}
