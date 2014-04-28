package de.effms.jade.service.search;

import jade.domain.FIPAAgentManagement.DFAgentDescription;

public interface SearchResultListener
{
    public void onSearchResults(DFAgentDescription[] searchResults, SearchService service);
}
