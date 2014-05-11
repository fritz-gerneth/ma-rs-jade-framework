package de.effms.jade.service.query;

import de.effms.jade.agent.Agent;
import jade.content.Term;
import jade.content.abs.*;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.OntologyException;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteQueryService
{
    private final Logger log = LoggerFactory.getLogger(RemoteQueryService.class);

    private final Queryable knowledgeBase;

    private final Agent agent;

    public RemoteQueryService(Agent agent, Queryable knowledgeBase)
    {
        this.agent = agent;
        this.knowledgeBase = knowledgeBase;

        this.agent.addBehaviour(new MessageReceiver());

        log.info("Starting RemoteQueryService for ontology " + knowledgeBase.getOntology().getName());
    }

    private class MessageReceiver extends CyclicBehaviour
    {
        private final MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.and(
                MessageTemplate.MatchLanguage((new SLCodec()).getName()),
                MessageTemplate.MatchOntology(knowledgeBase.getOntology().getName())
            ),
            MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
            )
        );

        public MessageReceiver()
        {
            log.info("Starting RemoteQueryService MessageReceiver with template" + mt);
        }

        @Override
        public void action()
        {
            // Try to receive a message, and if not wait until the next message arrives
            ACLMessage message = agent.receive(mt);
            if (null == message) {
                this.block();
                return;
            }

            // A message matching out message template arrived, extract it
            AbsContentElement contentElement = null;
            try {
                contentElement = agent.getContentManager().extractAbsContent(message);
            } catch (Codec.CodecException | OntologyException e) {
                log.error("Could not extract content from message" + message, e);
            }

            log.debug("Received raw message", contentElement);
            if (ACLMessage.QUERY_IF == message.getPerformative()) {
                ResponseToQueryIf responseHandler = new ResponseToQueryIf(message);

                try {
                    contentElement = agent.getContentManager().extractAbsContent(message);
                } catch (Codec.CodecException | OntologyException e) {
                    responseHandler.onMessageError(e.getMessage());
                    log.info("Received invalid message" + message);
                }

                if (contentElement instanceof AbsPredicate) {
                    AbsPredicate query = (AbsPredicate) contentElement;
                    log.info("Delegating query to knowledge base"+ query);
                    knowledgeBase.queryIf(query, responseHandler);
                } else {
                    responseHandler.onMessageError("Excepting predicate for query-if");
                    log.info("Query was no predicate" + contentElement);
                }
            } else if (ACLMessage.QUERY_REF == message.getPerformative()) {
                ResponseToQueryRef responseHandler = new ResponseToQueryRef(message);

                try {
                    contentElement = agent.getContentManager().extractAbsContent(message);
                } catch (Codec.CodecException | OntologyException e) {
                    responseHandler.onMessageError(e.getMessage());
                    log.info("Received invalid message" + message);
                }

                if (contentElement instanceof AbsIRE) {
                    AbsIRE query = (AbsIRE) contentElement;
                    log.info("Delegating query to knowledge base" + query);
                    knowledgeBase.queryRef(query, responseHandler);
                } else {
                    responseHandler.onMessageError("Excepting predicate for query-if");
                    log.info("Query was no predicate" + contentElement);
                }
            } else {
                System.out.println("Received message but shouldn't be handling it: " + message.toString());
            }
        }
    }

    private class ResponseToQueryRef extends AbstractResponseHandler implements QueryRefCallback
    {
        public ResponseToQueryRef(ACLMessage sourceMessage)
        {
            super(sourceMessage);
        }

        @Override
        public void onQueryRefResult(AbsIRE query, AbsPredicate result)
        {
            /** AbsObject resultTerm = null;
            try {
                resultTerm = knowledgeBase.getOntology().fromObject(result);
            } catch (OntologyException e) {
                log.error("Could not serialize result", result);
            }

            AbsPredicate response = new AbsPredicate(SLVocabulary.EQUALS);
            response.set(SLVocabulary.EQUALS_LEFT, query);
            response.set(SLVocabulary.EQUALS_RIGHT, resultTerm); */

            this.sendMessage(ACLMessage.INFORM_REF, result);
        }
    }

    private class ResponseToQueryIf extends AbstractResponseHandler implements QueryIfCallback
    {
        public ResponseToQueryIf(ACLMessage sourceMessage)
        {
            super(sourceMessage);
        }

        @Override
        public void onQueryIfResult(AbsPredicate query, boolean result)
        {
            AbsPredicate response = new AbsPredicate(SLVocabulary.EQUALS);
            response.set(SLVocabulary.EQUALS_LEFT, query);
            response.set(SLVocabulary.EQUALS_RIGHT, AbsPrimitive.wrap(result));

            this.sendMessage(ACLMessage.INFORM_IF, response);
        }
    }

    abstract private class AbstractResponseHandler
    {
        protected final ACLMessage message;

        public AbstractResponseHandler(ACLMessage sourceMessage)
        {
            this.message = sourceMessage;
        }

        public void onMessageError(String error)
        {
            ACLMessage reply = message.createReply();
            reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
            reply.setContent(error);
            agent.send(reply);
        }

        public void onLogicError(AbsPredicate query, AbsContentElement response)
        {
            this.sendMessage(ACLMessage.FAILURE, response);
        }

        protected void sendMessage(int performative, AbsContentElement response)
        {
            ACLMessage reply = message.createReply();
            reply.setPerformative(performative);

            try {
                agent.getContentManager().fillContent(reply, response);
            } catch (Codec.CodecException | OntologyException e) {
                this.onMessageError(e.getMessage());
                log.error("Could not fill response into message " + response, e);
                return;
            }

            log.debug("Scheduling message for submission " + reply);
            agent.send(reply);
        }
    }
}
