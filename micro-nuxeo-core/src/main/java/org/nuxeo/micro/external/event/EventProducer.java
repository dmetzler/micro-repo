package org.nuxeo.micro.external.event;

public interface EventProducer {

    void fireEvent(Event event);

    void fireEventBundle(EventBundle event);

}
