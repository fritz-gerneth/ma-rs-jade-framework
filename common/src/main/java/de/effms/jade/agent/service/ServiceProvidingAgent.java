package de.effms.jade.agent.service;

import de.effms.jade.agent.Agent;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public interface ServiceProvidingAgent extends Agent
{
    public void registerService(ServiceDescription serviceDescription, Ontology ontology);

    public void registerService(ServiceDescription serviceDescription, Ontology ontology, Codec language);
}
