package de.effms.jade.service.search;

import jade.domain.FIPAAgentManagement.DFAgentDescription;

public interface SearchService
{
    public void addSearchResultListener(SearchResultListener listener, DFAgentDescription query);
}
