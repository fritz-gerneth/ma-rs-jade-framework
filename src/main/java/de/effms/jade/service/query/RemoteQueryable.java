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
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.UUID;

public class RemoteQueryable implements Queryable
{
    private final Agent agent;

    private final AID remoteAgent;

    private final Ontology ontology;

    private final Codec language;

    public RemoteQueryable(Agent agent, AID remoteAgent, Ontology ontology)
    {
        this.agent = agent;
        this.remoteAgent = remoteAgent;
        this.ontology = ontology;
        this.language = new SLCodec();
    }

    @Override
    public void queryIf(AbsPredicate query, QueryIfCallback callback)
    {
        ACLMessage message = new ACLMessage(ACLMessage.QUERY_IF);
        message.setLanguage(this.language.getName());
        message.setOntology(this.ontology.getName());
        message.addReceiver(this.remoteAgent);
        message.setReplyWith(UUID.randomUUID().toString());

        try {
            agent.getContentManager().fillContent(message, query);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            return;
        }

        this.agent.addBehaviour(new WaitForQueryRefResult(message, query, callback));
        this.agent.addBehaviour(new DelayedSend(message));
    }

    @Override
    public void queryRef(AbsIRE query, QueryRefCallback callback)
    {
        ACLMessage message = new ACLMessage(ACLMessage.QUERY_REF);
        message.setLanguage(this.language.getName());
        message.setOntology(this.ontology.getName());
        message.addReceiver(this.remoteAgent);
        message.setReplyWith(UUID.randomUUID().toString());

        try {
            agent.getContentManager().fillContent(message, query);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
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

    private class WaitForQueryRefResult extends OneShotBehaviour
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
        }

        @Override
        public void action()
        {
            ACLMessage message = agent.blockingReceive(this.messageTemplate);
            AbsContentElement contentElement = null;

            try {
                contentElement = agent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }

            if (contentElement instanceof AbsPrimitive) {
                AbsPrimitive returnValue = (AbsPrimitive) contentElement;
                if (BasicOntology.BOOLEAN == returnValue.getTypeName()) {
                    this.callback.onQueryIfResult(this.query, returnValue.getBoolean());
                }
            }
        }
    }

    private class DelayedSend extends OneShotBehaviour
    {
        private final ACLMessage message;

        public DelayedSend(ACLMessage message)
        {
            this.message = message;
        }

        @Override
        public void action()
        {
            agent.send(this.message);
        }
    }

    private class WaitForQueryIfResult extends OneShotBehaviour
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
        }

        @Override
        public void action()
        {
            ACLMessage message = agent.blockingReceive(this.messageTemplate);
            AbsContentElement contentElement = null;

            try {
                contentElement = agent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
            }

            if (contentElement instanceof AbsPredicate) {
                this.callback.onQueryRefResult(this.query, (AbsPredicate) contentElement);
            }
        }
    }
}
