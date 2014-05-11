package de.effms.jade.service.publish;

import de.effms.jade.agent.Agent;
import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class RemoteSubscriptionService
{
    private final Logger log = LoggerFactory.getLogger(RemoteSubscriptionService.class);

    private final Agent localAgent;

    private final Subscribable publisher;

    private final Hashtable<String, Subscription> subscriptions = new Hashtable<>();

    private final Codec language = new SLCodec();

    public RemoteSubscriptionService(Agent localAgent, Subscribable publisher)
    {
        this.localAgent = localAgent;
        this.publisher = publisher;

        Codec language = new SLCodec();
        MessageTemplate messageTemplate =  MessageTemplate.and(
            MessageTemplate.and(
                MessageTemplate.MatchLanguage(language.getName()),
                MessageTemplate.MatchOntology(this.publisher.getOntology().getName())
            ),
            MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE)
        );

        log.info("Starting RemoteSubscriptionService for " + publisher.getOntology().getName() + ". Messages have to match " + messageTemplate.toString());

        this.localAgent.addBehaviour(
            new SubscriptionResponder(this.localAgent.asJadeAgent(), messageTemplate, new SubscriptionManager())
        );
    }

    protected class SubscriptionManager implements SubscriptionResponder.SubscriptionManager
    {
        @Override
        public boolean register(final SubscriptionResponder.Subscription jadeSubscription) throws RefuseException, NotUnderstoodException
        {
            log.info("Received new subscriber", jadeSubscription);

            String conversationId = jadeSubscription.getMessage().getConversationId();
            if (subscriptions.containsKey(conversationId)) {
                throw new RefuseException("Duplicate jadeSubscription performative for  " + conversationId);
            }

            Subscription subscription = publisher.subscribe(this.extractIREFromMessage(jadeSubscription.getMessage()));
            subscription.setCallback(new SendToSubscriberCallback(this, jadeSubscription));

            subscriptions.put(conversationId, subscription);

            return true;
        }

        @Override
        public boolean deregister(SubscriptionResponder.Subscription jadeSubscription) throws FailureException
        {
            Subscription subscription = this.clearSubscription(jadeSubscription);
            publisher.cancel(subscription);

            return true;
        }

        protected Subscription clearSubscription(SubscriptionResponder.Subscription jadeSubscription) throws FailureException
        {
            String conversationId = jadeSubscription.getMessage().getConversationId();
            Subscription subscription = subscriptions.get(conversationId);
            if (null == subscription) {
                throw new FailureException("Could not locate active subscription for " + conversationId);
            }

            subscriptions.remove(conversationId);

            return subscription;
        }

        private AbsIRE extractIREFromMessage(ACLMessage message) throws NotUnderstoodException
        {
            AbsContentElement contentElement;
            try {
                contentElement = localAgent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                throw new NotUnderstoodException(message);
            }

            if (contentElement instanceof AbsIRE) {
                return (AbsIRE) contentElement;
            } else {
                throw new NotUnderstoodException("Excepting IRE for jadeSubscription. Got content " + contentElement);
            }
        }
    }

    private class SendToSubscriberCallback implements SubscriptionListener
    {
        private final SubscriptionManager subscriptionManager;

        private final SubscriptionResponder.Subscription jadeSubscription;

        private SendToSubscriberCallback(SubscriptionManager subscriptionManager, SubscriptionResponder.Subscription jadeSubscription)
        {
            this.subscriptionManager = subscriptionManager;
            this.jadeSubscription = jadeSubscription;
        }

        @Override
        public void onInform(AbsPredicate result)
        {
            log.debug("New value to subscription. Informing remote agent.");

            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setLanguage(language.getName());
            message.setOntology(publisher.getOntology().getName());
            try {
                localAgent.getContentManager().fillContent(message, result);
            } catch (Codec.CodecException | OntologyException e) {
                log.error("Could not create ACLMessage", e);
                return;
            }
            log.info("Sending message back to subscriber: " + message.toString());
            this.jadeSubscription.notify(message);
        }

        @Override
        public void onCancel()
        {
            // TODO: send cancel message to subscriber
            try {
                this.subscriptionManager.clearSubscription(this.jadeSubscription);
            } catch (FailureException e) {
                e.printStackTrace();
            }
        }
    }
}
