package de.effms.jade.agent.service;

import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public interface ServiceProvidingAgent
{
    public void registerService(ServiceDescription serviceDescription, Ontology ontology);

    public void registerService(ServiceDescription serviceDescription, Ontology ontology, Codec language);
}
