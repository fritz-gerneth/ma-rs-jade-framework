package de.effms.jade.agent.search;

import jade.domain.FIPAAgentManagement.DFAgentDescription;

public interface SearchCallback
{
    public void onSearchResult(DFAgentDescription[] searchResult, DFAgentDescription searchQuery);
}
