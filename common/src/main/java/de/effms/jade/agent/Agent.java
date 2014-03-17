package de.effms.jade.agent;

import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.Properties;
import jade.util.leap.Serializable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Extracted interface from jade.core.Agent
 */
public interface Agent extends Runnable, Serializable, TimerListener
{
    public jade.core.Agent asJadeAgent();

    void restartLater(Behaviour b, long millis);

    void doTimeOut(Timer t);

    void notifyRestarted(Behaviour b);

    void removeTimer(Behaviour b);

    jade.wrapper.AgentContainer getContainerController();

    void setArguments(Object args[]);

    Object[] getArguments();

    boolean isRestarting();

    AID getAMS();

    AID getDefaultDF();

    String getLocalName();

    String getName();

    String getHap();

    AID getAID();

    Location here();

    void join();

    void setQueueSize(int newSize) throws IllegalArgumentException;

    int getCurQueueSize();

    int getQueueSize();

    /////////////////////////////////
    // Agent state management
    /////////////////////////////////
    void changeStateTo(LifeCycle newLifeCycle);

    void restoreBufferedState();

    int getState();

    //#MIDP_EXCLUDE_BEGIN
    AgentState getAgentState();

    void doMove(Location destination);

    void doClone(Location destination, String newName);

    void doSuspend();

    void doActivate();

    void doWait();

    void doWait(long millis);

    void doWake();

    void doDelete();

    void write(OutputStream s) throws IOException;

    void restore(InputStream s) throws IOException;

    void putO2AObject(Object o, boolean blocking) throws InterruptedException;

    Object getO2AObject();

    void setEnabledO2ACommunication(boolean enabled, int queueSize);

    void setO2AManager(Behaviour b);

    @SuppressWarnings("unchecked")
    <T> T getO2AInterface(Class<T> theInterface);

    <T> void registerO2AInterface(Class<T> theInterface, T implementation);

    //#APIDOC_EXCLUDE_BEGIN
    void clean(boolean ok);

    void addBehaviour(Behaviour b);

    void removeBehaviour(Behaviour b);

    void send(ACLMessage msg);

    ACLMessage receive();

    ACLMessage receive(MessageTemplate pattern);

    ACLMessage blockingReceive();

    ACLMessage blockingReceive(long millis);

    ACLMessage blockingReceive(MessageTemplate pattern);

    ACLMessage blockingReceive(MessageTemplate pattern, long millis);

    void putBack(ACLMessage msg);

    void waitUntilStarted();

    // Notify the toolkit of the change in behaviour state
    // Public as it is called by the Scheduler and by the Behaviour class
    void notifyChangeBehaviourState(Behaviour b, String from, String to);

    void setGenerateBehaviourEvents(boolean b);

    void postMessage(ACLMessage msg);

    jade.content.ContentManager getContentManager();

    ServiceHelper getHelper(String serviceName) throws ServiceException;

    String getProperty(String key, String aDefault);

    Properties getBootProperties();
}
