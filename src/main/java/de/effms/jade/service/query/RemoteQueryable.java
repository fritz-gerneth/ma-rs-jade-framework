package de.effms.jade.service.query;

import de.effms.jade.agent.Agent;
import jade.content.Predicate;
import jade.content.Term;
import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsIRE;
import jade.content.abs.AbsPredicate;
import jade.content.abs.AbsPrimitive;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class RemoteQueryable implements Queryable
{
    private final Logger log = LoggerFactory.getLogger(RemoteQueryable.class);

    private final Agent agent;

    private final AID remoteAgent;

    private final Ontology ontology;

    private final Codec language;

    public RemoteQueryable(Agent agent, AID remoteAgent, Ontology ontology)
    {
        log.info("Starting RemoteQueryable for ontology " + ontology.getName());

        this.agent = agent;
        this.remoteAgent = remoteAgent;
        this.ontology = ontology;
        this.language = new SLCodec();
    }

    @Override
    public void queryIf(AbsPredicate query, QueryIfCallback callback)
    {
        log.debug("Received query-if " + query.toString());

        ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF);
        message.setLanguage(this.language.getName());
        message.setOntology(this.ontology.getName());
        message.addReceiver(this.remoteAgent);
        message.setReplyWith(UUID.randomUUID().toString());

        try {
            agent.getContentManager().fillContent(message, query);
        } catch (Codec.CodecException | OntologyException e) {
            log.error("Could not fill query in message" + message, e);
            return;
        }

        this.agent.addBehaviour(new WaitForQueryRefResult(message, query, callback));
        this.agent.addBehaviour(new DelayedSend(message));
    }

    @Override
    public void queryRef(AbsIRE query, QueryRefCallback callback)
    {
        log.debug("Received query-ref " + query.toString());

        ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
        message.setLanguage(this.language.getName());
        message.setOntology(this.ontology.getName());
        message.addReceiver(this.remoteAgent);
        message.setReplyWith(UUID.randomUUID().toString());

        try {
            agent.getContentManager().fillContent(message, query);
        } catch (Codec.CodecException | OntologyException e) {
            log.error("Could not fill query in message" + message, e);
            return;
        }

        this.agent.addBehaviour(new WaitForQueryIfResult(message, query, callback));
        this.agent.addBehaviour(new DelayedSend(message));
    }

    @Override
    public Ontology getOntology()
    {
        return this.ontology;
    }

    private class WaitForQueryRefResult extends CyclicBehaviour
    {
        private final MessageTemplate messageTemplate;

        private final AbsPredicate query;

        private final QueryIfCallback callback;

        public WaitForQueryRefResult(ACLMessage queryMessage, AbsPredicate query, QueryIfCallback callback)
        {
            this.messageTemplate = MessageTemplate.and(
                MessageTemplate.and(
                    MessageTemplate.MatchEncoding(language.getName()),
                    MessageTemplate.MatchOntology(ontology.getName())
                ),
                MessageTemplate.and(
                    MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
                    ),
                    MessageTemplate.MatchInReplyTo(queryMessage.getReplyWith())
                )
            );

            this.query = query;
            this.callback = callback;

            log.info("Started waiting for query-reply on conversation " + queryMessage.getReplyWith());
            log.debug("Exact message filter is " + this.messageTemplate);
        }

        @Override
        public void action()
        {
            ACLMessage message = agent.receive(this.messageTemplate);
            if (null == message) {
                this.block();
                return;
            }

            AbsContentElement contentElement = null;
            try {
                contentElement = agent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                log.error("Could not extract message" + message, e);
            }

            if (contentElement instanceof AbsPrimitive) {
                AbsPrimitive returnValue = (AbsPrimitive) contentElement;
                log.debug("Extracted message content" + returnValue);
                if (BasicOntology.BOOLEAN.equals(returnValue.getTypeName())) {
                    this.callback.onQueryIfResult(this.query, returnValue.getBoolean());
                    return;
                }
            }

            log.error("Unhandled message content" + contentElement);
            agent.removeBehaviour(this);
        }
    }

    private class DelayedSend extends OneShotBehaviour
    {
        private final ACLMessage message;

        public DelayedSend(ACLMessage message)
        {
            log.debug("Scheduled submission of message for next cycle" +  message.toString());
            this.message = message;
        }

        @Override
        public void action()
        {
            log.debug("Sending message" + message);
            agent.send(this.message);
        }
    }

    private class WaitForQueryIfResult extends CyclicBehaviour
    {
        private final MessageTemplate messageTemplate;

        private final AbsIRE query;

        private final QueryRefCallback callback;

        public WaitForQueryIfResult(ACLMessage queryMessage, AbsIRE query, QueryRefCallback callback)
        {
            this.messageTemplate = MessageTemplate.and(
                MessageTemplate.and(
                    MessageTemplate.MatchLanguage(language.getName()),
                    MessageTemplate.MatchOntology(ontology.getName())
                ),
                MessageTemplate.and(
                    MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM_REF),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
                    ),
                    MessageTemplate.MatchInReplyTo(queryMessage.getReplyWith())
                )
            );

            this.query = query;
            this.callback = callback;

            log.info("Started waiting for query-reply on conversation " + queryMessage.getReplyWith());
            log.debug("Exact message filter is " + this.messageTemplate);
        }

        @Override
        public void action()
        {
            ACLMessage message = agent.receive(this.messageTemplate);
            if (null == message) {
                this.block();
                return;
            }

            AbsContentElement contentElement = null;
            try {
                contentElement = agent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                log.error("Could not extract message " + message, e);
            }

            if (contentElement instanceof AbsPredicate) {
                log.debug("Extracted message content" + contentElement);
                this.callback.onQueryRefResult(this.query, (AbsPredicate) contentElement);
                return;
            }

            log.error("Unhandled message content" + contentElement);
            agent.removeBehaviour(this);
        }
    }
}
