package de.effms.jade.agent.publish;

import de.effms.jade.agent.lifecycle.LifecycleSubscriber;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.Subscription;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

abstract public class AbstractPublicationService implements PublicationService
{
    private final HashMap<String, Subscription> subscribers;

    private final PublishingAgent agent;

    private final Codec language;

    private final Ontology publicationOntology;

    public AbstractPublicationService(PublishingAgent agent, Ontology publicationOntology)
    {
        log().info("Starting");

        this.subscribers = new HashMap<>();
        this.agent = agent;
        this.language = new SLCodec();
        this.publicationOntology = publicationOntology;

        this.agent.registerService(this.getServiceDescription(), this.publicationOntology, this.language);
        this.agent.registerLifecycleSubscriber(new LifecycleHandler());
    }

    @Override
    public void publish(Predicate predicate)
    {
        log().info("Publishing: " + predicate);

        ACLMessage message = this.createSeededMessage(ACLMessage.INFORM);
        try {
            this.agent.getContentManager().fillContent(message, predicate);
        } catch (Codec.CodecException | OntologyException ex) {
            log().error(ex.getMessage());
            log().debug(ex.getStackTrace().toString());
        }
        log().debug(message.toString());

        for(Map.Entry<String, Subscription> subscriber : subscribers.entrySet()) {
            log().debug("Sending to: " + subscriber.getKey());
            subscriber.getValue().notify(message);
        }
    }

    protected ACLMessage createSeededMessage(int performative)
    {
        ACLMessage message = new ACLMessage(performative);
        message.setLanguage(this.language.getName());
        message.setOntology(this.publicationOntology.getName());
        return message;
    }

    abstract protected ServiceDescription getServiceDescription();

    abstract protected Logger log();

    private class LifecycleHandler implements LifecycleSubscriber
    {
        @Override
        public void onSetup()
        {
            log().info("LifecycleHandler: Setup");
            agent.addBehaviour(new SubscriptionBehaviour());
        }

        @Override
        public void onTakeDown()
        {
            log().info("LifecycleHandler: Taking down");
            ACLMessage message = new ACLMessage(ACLMessage.FAILURE);
            for(Map.Entry<String, Subscription> subscriber : subscribers.entrySet()) {
                log().debug("LifecycleHandler: Sending Failure to " + subscriber.getKey());
                subscriber.getValue().notify(message);
            }
        }
    }

    private class SubscriptionBehaviour extends SubscriptionResponder
    {
        public SubscriptionBehaviour()
        {
            super(agent.asJadeAgent(), MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE),
                MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE)
            ), new SubscriptionHandler());
            log().info("SubscriptionBehaviour: Started");
        }
    }

    private class SubscriptionHandler implements SubscriptionResponder.SubscriptionManager
    {
        @Override
        public boolean register(Subscription subscription) throws RefuseException, NotUnderstoodException
        {
            AID subscriber = subscription.getMessage().getSender();

            if (subscribers.containsKey(subscriber.getName())) {
                log().info("SubscriptionHandler: Refused subscription of " + subscriber.getName() + ". Already subscribed");
                return false;
            }

            subscribers.put(subscriber.getName(), subscription);
            log().info("SubscriptionHandler: Implicit subscription accept of " + subscriber.getName());

            return true;
        }

        @Override
        public boolean deregister(Subscription subscription) throws FailureException
        {
            AID subscriber = subscription.getMessage().getSender();

            if (!subscribers.containsKey(subscriber.getName())) {
                log().warn("SubscriptionHandler: Refused unsubscription of " + subscriber.getName() + ". Was not subscribed");
                return false;
            }

            subscribers.remove(subscriber.getName());
            log().info("SubscriptionHandler: Accepting unsubscription of " + subscriber.getName());

            return true;
        }
    }

}
