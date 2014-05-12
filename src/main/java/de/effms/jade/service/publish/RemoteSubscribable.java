package de.effms.jade.service.publish;

import de.effms.jade.agent.Agent;
import jade.content.abs.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class RemoteSubscribable implements Subscribable
{
    private final Logger log = LoggerFactory.getLogger(RemoteSubscribable.class);

    private final Agent localAgent;

    private final AID remoteAgent;

    private final Ontology ontology;

    private HashMap<String, SubscriptionManager> subscriptions = new HashMap<>();

    public RemoteSubscribable(Agent localAgent, AID remoteAgent, Ontology ontology)
    {
        this.localAgent = localAgent;
        this.remoteAgent = remoteAgent;
        this.ontology = ontology;
    }

    @Override
    public Subscription subscribe(AbsIRE query)
    {
        ACLMessage subscriptionMessage = new ACLMessage(ACLMessage.SUBSCRIBE);
        subscriptionMessage.setLanguage((new SLCodec()).getName());
        subscriptionMessage.setOntology(this.ontology.getName());
        subscriptionMessage.addReceiver(this.remoteAgent);

        try {
            this.localAgent.getContentManager().fillContent(subscriptionMessage, query);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            return null;
        }

        log.info("Creating subscription", subscriptionMessage);

        Subscription subscription = new Subscription(query);
        SubscriptionManager subscriptionManager = new SubscriptionManager(subscriptionMessage, subscription);
        this.localAgent.addBehaviour(subscriptionManager);
        this.subscriptions.put(subscription.getSubscriptionID(), subscriptionManager);

        return subscription;
    }

    @Override
    public void cancel(Subscription subscription)
    {
        SubscriptionManager subscriptionManager = this.subscriptions.get(subscription.getSubscriptionID());
        if (null == subscriptionManager) {
            return;
        }

        subscriptionManager.cancel(remoteAgent, true);
        this.subscriptions.remove(subscription.getSubscriptionID());
    }

    @Override
    public Ontology getOntology()
    {
        return this.ontology;
    }

    private class SubscriptionManager extends SubscriptionInitiator
    {
        private final Subscription subscription;

        public SubscriptionManager(ACLMessage subscriptionMessage, Subscription subscription)
        {
            super(localAgent.asJadeAgent(), subscriptionMessage);

            this.subscription = subscription;
        }

        @Override
        protected void handleRefuse(ACLMessage refuse)
        {
            log.info("Subscription got cancelled");
            this.subscription.getCallback().onCancel();
        }

        @Override
        protected void handleInform(ACLMessage message)
        {
            log.info("Received inform for subscription", message.toString());
            AbsContentElement contentElement;
            try {
                contentElement = localAgent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                // We actually should do some error handling here
                try {
                    throw new NotUnderstoodException(message);
                } catch (NotUnderstoodException e1) {
                    log.error("Could not extract message", e1);
                }
                return;
            }

            if (!(contentElement instanceof AbsPredicate)) {
                log.error("Subscription answer is no predicate");
                return;
            }

            if (!contentElement.getTypeName().equals(SLVocabulary.EQUALS)) {
                log.error("Subscription answer is no equation");
                return;
            }

            AbsObject rValue = contentElement.getAbsObject(SLVocabulary.EQUALS_RIGHT);
            if (!(rValue instanceof AbsConcept)) {
                log.error("Right-Value of equation is not concept");
                return;
            }

            this.subscription.getCallback().onInform(this.subscription.getQuery(), (AbsConcept) rValue);
        }
    }
}
