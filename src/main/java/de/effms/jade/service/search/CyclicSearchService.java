package de.effms.jade.service.search;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class CyclicSearchService extends TickerBehaviour implements SearchService
{
    private final List<ListenerEntry> listeners = new LinkedList<>();

    private final Logger log = LoggerFactory.getLogger(CyclicSearchService.class);

    public CyclicSearchService(Agent a, long period)
    {
        super(a, period);
    }

    @Override
    public void addSearchResultListener(SearchResultListener listener, DFAgentDescription query)
    {
        this.listeners.add(new ListenerEntry(listener, query));
        log.debug(listener + " listening on " + query);
    }

    @Override
    public void cancelQuery(SearchResultListener listener, DFAgentDescription query)
    {
        for (ListenerEntry listenerEntry : listeners) {
            if (listenerEntry.listener == listener && listenerEntry.query == query) {
                this.listeners.remove(listenerEntry);
            }
        }
    }

    @Override
    public void onTick()
    {
        for (ListenerEntry listenerEntry : listeners) {
            try {
                DFAgentDescription[] result = DFService.search(this.getAgent(), listenerEntry.query);
                if (0 < result.length) {
                    listenerEntry.listener.onSearchResults(result, this);
                    log.debug(listenerEntry.listener + " receives " + result);
                }
            } catch (FIPAException ex) {
                log.warn(ex.getStackTrace().toString());
            }
        }
    }

    private class ListenerEntry
    {
        public SearchResultListener listener;

        public DFAgentDescription query;

        public ListenerEntry(SearchResultListener listener, DFAgentDescription query)
        {
            this.listener = listener;
            this.query = query;
        }
    }
}
