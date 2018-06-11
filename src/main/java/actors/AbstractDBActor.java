package actors;

import akka.actor.AbstractLoggingActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public abstract class AbstractDBActor extends AbstractLoggingActor {

    protected LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }
}
