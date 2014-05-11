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

public class RemoteQueryService
{
    private final Queryable knowledgeBase;

    private final Agent agent;

    public RemoteQueryService(Agent agent, Queryable knowledgeBase)
    {
        this.agent = agent;
        this.knowledgeBase = knowledgeBase;

        this.agent.addBehaviour(new MessageReceiver());
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
                e.printStackTrace();
            }

            if (ACLMessage.QUERY_IF == message.getPerformative()) {
                ResponseToQueryIf responseHandler = new ResponseToQueryIf(message);

                try {
                    contentElement = agent.getContentManager().extractAbsContent(message);
                } catch (Codec.CodecException | OntologyException e) {
                    responseHandler.onMessageError(e.getMessage());
                }

                if (contentElement instanceof AbsPredicate) {
                    AbsPredicate query = (AbsPredicate) contentElement;
                    knowledgeBase.queryIf(query, responseHandler);
                } else {
                    responseHandler.onMessageError("Excepting predicate for query-if");
                }
            } else if (ACLMessage.QUERY_REF == message.getPerformative()) {
                ResponseToQueryRef responseHandler = new ResponseToQueryRef(message);

                try {
                    contentElement = agent.getContentManager().extractAbsContent(message);
                } catch (Codec.CodecException | OntologyException e) {
                    responseHandler.onMessageError(e.getMessage());
                }

                if (contentElement instanceof AbsIRE) {
                    AbsIRE query = (AbsIRE) contentElement;
                    knowledgeBase.queryRef(query, responseHandler);
                } else {
                    responseHandler.onMessageError("Excepting predicate for query-if");
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
            AbsObject resultTerm = null;
            try {
                resultTerm = knowledgeBase.getOntology().fromObject(result);
            } catch (OntologyException e) {
                e.printStackTrace();
            }

            AbsPredicate response = new AbsPredicate(SLVocabulary.EQUALS);
            response.set(SLVocabulary.EQUALS_LEFT, query);
            response.set(SLVocabulary.EQUALS_RIGHT, resultTerm);

            this.sendMessage(ACLMessage.QUERY_REF, response);
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

            this.sendMessage(ACLMessage.QUERY_IF, response);
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
                return;
            }

            agent.send(reply);
        }
    }
}
