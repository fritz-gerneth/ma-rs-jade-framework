package de.effms.jade.service.publish;

import de.effms.jade.agent.Agent;
import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

import java.util.HashMap;

public class RemoteSubscribable implements Subscribable
{
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
            this.subscription.getCallback().onCancel();
        }

        @Override
        protected void handleInform(ACLMessage message)
        {
            AbsContentElement contentElement;
            try {
                contentElement = localAgent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                try {
                    throw new NotUnderstoodException(message);
                } catch (NotUnderstoodException e1) {
                    e1.printStackTrace();
                }
                return;
            }

            if (contentElement instanceof AbsPredicate) {
                this.subscription.getCallback().onInform((AbsPredicate) contentElement);
            } else {
                try {
                    throw new NotUnderstoodException("Excepting IRE for jadeSubscription. Got content " + contentElement);
                } catch (NotUnderstoodException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
