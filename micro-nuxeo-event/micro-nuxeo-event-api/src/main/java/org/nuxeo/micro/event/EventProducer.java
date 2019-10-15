package org.nuxeo.micro.event;

public interface EventProducer {

    void fireEvent(Event event);

    void fireEventBundle(EventBundle event);

}
